package org.simbrain.network.gui.nodes;

import org.nd4j.linalg.factory.Nd4j;
import org.piccolo2d.nodes.PImage;
import org.piccolo2d.nodes.PPath;
import org.piccolo2d.util.PPaintContext;
import org.simbrain.network.dl4j.ArrayConnectable;
import org.simbrain.network.dl4j.WeightMatrix;
import org.simbrain.network.gui.NetworkPanel;
import org.simbrain.network.gui.actions.edit.CopyAction;
import org.simbrain.network.gui.actions.edit.CutAction;
import org.simbrain.network.gui.actions.edit.DeleteAction;
import org.simbrain.network.gui.actions.edit.PasteAction;
import org.simbrain.util.ResourceManager;
import org.simbrain.util.StandardDialog;
import org.simbrain.util.math.SimbrainMath;
import org.simbrain.util.propertyeditor.AnnotatedPropertyEditor;
import org.simbrain.util.table.NumericTable;
import org.simbrain.util.table.SimbrainJTable;
import org.simbrain.util.table.SimbrainJTableScrollPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.image.*;
import java.util.stream.Collectors;

/**
 * A visual representation of a weight matrix
 */
public class WeightMatrixNode extends ScreenElement {

    /**
     * The weight matrix this node represents
     */
    private WeightMatrix weightMatrix;

    /**
     * The line model connecting the source and the target
     */
    private QuadCurve2D line2D;

    /**
     * The visual component of the {@link #line2D}.
     */
    private PPath line;

    /**
     * An image showing individuals weightMatrix strength
     */
    private PImage weightsImage = new PImage();

    /**
     * Width of the {@link #weightsImage}
     */
    private int imageWidth = 100;

    /**
     * Height of the {@link #weightsImage}
     */
    private int imageHeight = 100;

    /**
     * A box around the {@link #weightsImage}
     */
    private PPath box;

    /**
     * Parent network panel.
     */
    private final NetworkPanel networkPanel;

    /**
     * Construct the weight matrix node.
     *
     * @param np parent panel
     * @param wm the weight matrix being represented
     */
    public WeightMatrixNode(NetworkPanel np, WeightMatrix wm) {
        super(np);
        networkPanel = np;
        box = PPath.createRectangle(-1, -1, imageHeight + 2, imageWidth + 2);
        box.setStroke(new BasicStroke(2));
        addChild(box);

        this.weightMatrix = wm;
        ArrayConnectable source = wm.getSource();
        ArrayConnectable target = wm.getTarget();

        Point2D sourceLocation = source.getAttachmentPoint();
        Point2D targetLocation = target.getAttachmentPoint();
        Point2D midLocation = SimbrainMath.midpoint(sourceLocation, targetLocation);

        line2D = new QuadCurve2D.Double(
                sourceLocation.getX(),
                sourceLocation.getY(),
                midLocation.getX(),
                midLocation.getY(),
                targetLocation.getX(),
                targetLocation.getY()
                );
        line = new PPath.Double(line2D);
        source.onLocationChange(() -> {
            updateImageBoxLocation();
            updateLine();
        });
        target.onLocationChange(() -> {
            updateImageBoxLocation();
            updateLine();
        });


        target.getOutgoingWeightMatrices().stream()
                .filter(m -> m.getTarget() == this.weightMatrix.getSource())
                .forEach(m -> { // Even though this is for each but there should only be one m.
                    m.setCurve(true);
                    this.getWeightMatrix().setCurve(true);
                });


        updateLine();

        renderMatrixToImage();
        addChild(weightsImage);
        updateImageBoxLocation();

        setPickable(true);

        setBounds(box.getFullBounds());

        weightMatrix.addPropertyChangeListener(evt -> {
            if ("updated".equals(evt.getPropertyName())) {
                renderMatrixToImage();
            } else if("delete".equals(evt.getPropertyName())) {
                WeightMatrixNode.this.removeFromParent();
            } else if("lineUpdated".equals(evt.getPropertyName())) {
                updateLine();
                updateImageBoxLocation();
            }
        });


    }

    /**
     * Redraw the {@link #line} based on the new source and target location.
     */
    private void updateLine() {
        Point2D source = weightMatrix.getSource().getAttachmentPoint();
        Point2D target = weightMatrix.getTarget().getAttachmentPoint();
        Point2D mid = SimbrainMath.add(
                SimbrainMath.midpoint(source, target),
                getCurveControlVector()
                );
        removeChild(line);
        line2D.setCurve(source, mid, target);
        line = new PPath.Double(line2D);
        line.setStroke(new BasicStroke(3.0f));
        line.setPaint(null);
        addChild(line);
        line.lowerToBottom();
    }

    private Point2D getCurveControlVector() {
        float offset = weightMatrix.isCurve() ? 150 : 0;
        Point2D source = weightMatrix.getSource().getAttachmentPoint();
        Point2D target = weightMatrix.getTarget().getAttachmentPoint();
        Point2D unitNormal = SimbrainMath.unitNormal(source, target);
        return SimbrainMath.scale(unitNormal, offset);
    }

    /**
     * Update the {@link #weightsImage} and its {@link #box} to the updated location (center of the {@link #line}.
     */
    private void updateImageBoxLocation() {

        Point2D source = weightMatrix.getSource().getAttachmentPoint();
        Point2D target = weightMatrix.getTarget().getAttachmentPoint();

        Point2D midPoint = SimbrainMath.midpoint(source, target);

        Point2D offset = new Point2D.Double(-50.0, -50.0);

        Point2D curveOffset = getCurveControlVector();

        curveOffset = SimbrainMath.scale(curveOffset, 0.5);

        offset = SimbrainMath.add(offset, curveOffset);

        weightsImage.setOffset(
                SimbrainMath.add(midPoint, offset)
        );
        box.setOffset(
                SimbrainMath.add(midPoint, offset)
        );
        this.setBounds(box.getFullBounds());
    }

    /**
     * Render the weight matrix to the {@link #weightsImage}.
     */
    private void renderMatrixToImage() {

        if (!weightMatrix.isEnableRendering()) {
            // TODO: Remove existing
            return;
        }

        ColorModel colorModel = new DirectColorModel(24, 0xff << 16, 0xff << 8, 0xff);
        SampleModel sampleModel = colorModel.createCompatibleSampleModel(weightMatrix.getWeightMatrix().columns(),
                weightMatrix.getWeightMatrix().rows());

        float[] activations = Nd4j.toFlattened(weightMatrix.getWeightMatrix()).toFloatVector();

        int[] raster = new int[(int) weightMatrix.getWeightMatrix().length()];

        // TODO: Use standardized Simbrain library for these color scalings
        for (int i = 0; i < activations.length; i++) {
            float saturation = activations[i];
            saturation = SimbrainMath.clip(saturation, -1.0f, 1.0f);
            if (saturation < 0) {
                raster[i] = Color.HSBtoRGB(2/3f, -saturation, 1.0f);
            } else {
                raster[i] = Color.HSBtoRGB(0.0f, saturation, 1.0f);
            }
        }

        // ref: https://stackoverflow.com/questions/33460365/what-the-fastest-way-to-draw-pixels-buffer-in-java
        BufferedImage img = new BufferedImage(
                colorModel,
                Raster.createWritableRaster(
                        sampleModel,
                        new DataBufferInt(raster, raster.length),
                        null
                ),
                false,
                null
        );

        SwingUtilities.invokeLater(() -> {
            this.weightsImage.setImage(img);
            this.weightsImage.setBounds(0, 0, imageWidth, imageHeight);
        });
    }

    @Override
    protected void paint(PPaintContext paintContext) {
        paintContext.getGraphics().setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        super.paint(paintContext);
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    @Override
    public boolean showSelectionHandle() {
        return true;
    }

    @Override
    public boolean isDraggable() {
        return false;
    }

    @Override
    protected boolean hasToolTipText() {
        return true;
    }

    @Override
    protected String getToolTipText() {
        return weightMatrix.toString();
    }

    @Override
    protected boolean hasContextMenu() {
        return true;
    }

    @Override
    protected JPopupMenu getContextMenu() {
        JPopupMenu contextMenu = new JPopupMenu();

        contextMenu.add(new CutAction(getNetworkPanel()));
        contextMenu.add(new CopyAction(getNetworkPanel()));
        contextMenu.add(new PasteAction(getNetworkPanel()));
        contextMenu.addSeparator();

        // Edit Submenu
        Action editArray = new AbstractAction("Edit...") {
            @Override
            public void actionPerformed(final ActionEvent event) {
                StandardDialog dialog = getMatrixDialog();
                dialog.setVisible(true);
            }
        };
        contextMenu.add(editArray);
        contextMenu.add(new DeleteAction(getNetworkPanel()));

        contextMenu.addSeparator();
        Action randomizeAction = new AbstractAction("Randomize") {

            {
                putValue(SMALL_ICON, ResourceManager.getImageIcon("Rand.png"));
                putValue(SHORT_DESCRIPTION, "Randomize neuro naarray");
            }

            @Override
            public void actionPerformed(final ActionEvent event) {
                weightMatrix.randomize();
            }
        };
        contextMenu.add(randomizeAction);

        contextMenu.addSeparator();
        Action editComponents= new AbstractAction("Edit Components...") {
            @Override
            public void actionPerformed(final ActionEvent event) {
                StandardDialog dialog = new StandardDialog();
                NumericTable table = new NumericTable(weightMatrix.getWeightMatrix().toDoubleMatrix());
                SimbrainJTable st = SimbrainJTable.createTable(table);
                dialog.setContentPane(new SimbrainJTableScrollPanel(st));

                dialog.addClosingTask(() -> {
                    weightMatrix.setWeights(table.getFlattenedData());
                });
                dialog.pack();
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);

            }
        };
        contextMenu.add(editComponents);

        // Coupling menu
        contextMenu.addSeparator();
        JMenu couplingMenu = networkPanel.getCouplingMenu(weightMatrix);
        if (couplingMenu != null) {
            contextMenu.add(couplingMenu);
        }

        return contextMenu;

    }

    /**
     * Returns the dialog for editing this weight matrix
     */
    private StandardDialog getMatrixDialog() {
        StandardDialog dialog = new AnnotatedPropertyEditor(weightMatrix).getDialog();
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        //dialog.addClosingTask(() -> {
        //    NeuronArrayNode.this.updateInfoText();
        //});
        return dialog;
    }

    @Override
    protected boolean hasPropertyDialog() {
        return true;
    }

    @Override
    protected JDialog getPropertyDialog() {
        return getMatrixDialog();
    }

    @Override
    public void resetColors() {
    }

    public WeightMatrix getWeightMatrix() {
        return weightMatrix;
    }
}