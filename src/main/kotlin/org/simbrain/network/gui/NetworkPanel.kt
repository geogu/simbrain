package org.simbrain.network.gui

import org.piccolo2d.PCanvas
import org.piccolo2d.util.PBounds
import org.simbrain.network.connections.QuickConnectionManager
import org.simbrain.network.core.Network
import org.simbrain.network.desktop.NetworkDesktopComponent
import org.simbrain.network.gui.actions.ShowLayoutDialogAction
import org.simbrain.network.gui.actions.TestInputAction
import org.simbrain.network.gui.actions.connection.ClearSourceNeurons
import org.simbrain.network.gui.actions.connection.SetSourceNeurons
import org.simbrain.network.gui.actions.dl4j.AddMultiLayerNet
import org.simbrain.network.gui.actions.dl4j.AddNeuronArrayAction
import org.simbrain.network.gui.actions.edit.*
import org.simbrain.network.gui.actions.modelgroups.NeuronCollectionAction
import org.simbrain.network.gui.actions.network.IterateNetworkAction
import org.simbrain.network.gui.actions.network.ShowNetworkPreferencesAction
import org.simbrain.network.gui.actions.neuron.AddNeuronsAction
import org.simbrain.network.gui.actions.neuron.NewNeuronAction
import org.simbrain.network.gui.actions.neuron.SetNeuronPropertiesAction
import org.simbrain.network.gui.actions.selection.*
import org.simbrain.network.gui.actions.synapse.AddSynapseGroupAction
import org.simbrain.network.gui.actions.synapse.SetSynapsePropertiesAction
import org.simbrain.network.gui.actions.synapse.ShowAdjustSynapsesDialog
import org.simbrain.network.gui.actions.synapse.ShowWeightMatrixAction
import org.simbrain.network.gui.actions.toolbar.ShowEditToolBarAction
import org.simbrain.network.gui.actions.toolbar.ShowMainToolBarAction
import org.simbrain.network.gui.actions.toolbar.ShowRunToolBarAction
import org.simbrain.network.gui.nodes.NeuronNode
import org.simbrain.network.gui.nodes.ScreenElement
import org.simbrain.util.widgets.ShowHelpAction
import org.simbrain.util.widgets.ToggleButton
import java.awt.BorderLayout
import java.awt.Color
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.*

/**
 * Should eventually replace NetworkPanel and NetworkPanelDesktop
 */
class NetworkPanel(networkDesktop: NetworkDesktopComponent, val network: Network) : JPanel() {

    /**
     * The Piccolo PCanvas.
     */
    val canvas = PCanvas()

    val selectedNodes get() = selectionModel.selection

    /**
     * Build mode.
     */
    var editMode: EditMode = EditMode.SELECTION
        private set

    /**
     * Selection model.
     */
    val selectionModel = NetworkSelectionModel(this)

    /**
     * Cached context menu.
     */
    var contextMenu: JPopupMenu = createNetworkContextMenu()

    /**
     * Cached alternate context menu.
     */
    val contextMenuAlt: JPopupMenu? = null

    /**
     * Last selected Neuron.
     */
    val lastSelectedNeuron: NeuronNode? = null

    /**
     * Label which displays current time.
     */
    val timeLabel: TimeLabel? = null

    /**
     * Reference to bottom NetworkPanelToolBar.
     */
    val southBar: CustomToolBar? = null

    /**
     * Show input labels.
     */
    val inOutMode = true

    /**
     * Use auto zoom.
     */
    val autoZoomMode = true

    /**
     * Show subnet outline.
     */
    val showSubnetOutline = false

    /**
     * Show time.
     */
    val showTime = true

    /**
     * Main tool bar.
     */
    val mainToolBar: CustomToolBar = createMainToolBar()

    /**
     * Run tool bar.
     */
    val runToolBar: CustomToolBar = createRunToolBar()

    /**
     * Edit tool bar.
     */
    val editToolBar: CustomToolBar = createEditToolBar()

    /**
     * Color of background.
     */
    val backgroundColor = Color.white

    /**
     * How much to nudge objects per key click.
     */
    private val nudgeAmount = 2.0

    /**
     * Source elements (when setting a source node or group and then connecting to a target).
     */
    private val sourceElements: List<ScreenElement> = ArrayList()

    /**
     * Toggle button for neuron clamping.
     */
    var neuronClampButton = JToggleButton()

    /**
     * Toggle button for weight clamping.
     */
    var synapseClampButton = JToggleButton()

    /**
     * Menu item for neuron clamping.
     */
    var neuronClampMenuItem = JCheckBoxMenuItem()

    /**
     * Menu item for weight clamping.
     */
    private var synapseClampMenuItem = JCheckBoxMenuItem()

    /**
     * Whether loose synapses are visible or not.
     */
    val looseWeightsVisible = true

    /**
     * Whether to display update priorities.
     */
    val prioritiesVisible = false

    /**
     * Text object event handler.
     */
    val textHandle: TextEventHandler = TextEventHandler(this)

    /**
     * Toolbar panel.
     */
    private val toolbars: JPanel = JPanel(BorderLayout())

    /**
     * Manages keyboard-based connections.
     */
    private val quickConnector = QuickConnectionManager()

    /**
     * Manages placement of new nodes, groups, etc.
     */
    val placementManager = PlacementManager()

    /**
     * Action manager.
     */
    val actionManager: NetworkActionManager = NetworkActionManager(this) // TODO


    /**
     * Set to 3 since update neurons, synapses, and groups each decrement it by 1. If 0, update is complete.
     */
    private val updateComplete = AtomicInteger(0)

    /**
     * Turn GUI on or off.
     */
    var guiOn = true
        set(guiOn) {
            if (guiOn) {
                this.setUpdateComplete(false)
                //this.updateSynapseNodes()
                updateComplete.decrementAndGet()
            }
            field = guiOn
        }


    fun setUpdateComplete(updateComplete: Boolean) {
        if (!updateComplete && this.updateComplete.get() != 0) {
            return
        }
        this.updateComplete.set(if (updateComplete) 0 else 3)
    }


    /**
     * Create and return a new Edit menu for this Network panel.
     *
     * @return a new Edit menu for this Network panel
     */
    fun createEditMenu() = with(actionManager) {
        JMenu("Edit").apply {
            add(getAction<CutAction>())
            add(getAction<CopyAction>())
            add(getAction<PasteAction>())
            add(getAction<DeleteAction>())
            addSeparator()
            add(getAction<ClearSourceNeurons>())
            add(getAction<SetSourceNeurons>())
            //add(connectionMenu) // TODO
            add(getAction<AddSynapseGroupAction>())
            addSeparator()
            add(getAction<RandomizeObjectsAction>())
            add(getAction<ShowAdjustSynapsesDialog>())
            addSeparator()
            add(getAction<ShowLayoutDialogAction>())
            addSeparator()
            add(getAction<NeuronCollectionAction>())
            addSeparator()
            add(createAlignMenu())
            add(createSpacingMenu())
            addSeparator()
            add(getAction<SetNeuronPropertiesAction>())
            add(getAction<SetSynapsePropertiesAction>())
            addSeparator()
            add(createSelectionMenu())
        }
    }

    /**
     * Create and return a new Insert menu for this Network panel.
     *
     * @return a new Insert menu for this Network panel
     */
    fun createInsertMenu(): JMenu? {
        val insertMenu = JMenu("Insert")
        insertMenu.add(actionManager.getAction<NewNeuronAction>())
        insertMenu.add(actionManager.neuronGroupAction)
        insertMenu.addSeparator()
        insertMenu.add(AddNeuronsAction(this))
        insertMenu.add(AddNeuronArrayAction(this))
        insertMenu.add(AddMultiLayerNet(this))
        insertMenu.addSeparator()
        insertMenu.add(actionManager.newNetworkMenu)
        insertMenu.addSeparator()
        insertMenu.add(actionManager.getAction<TestInputAction>())
        insertMenu.add(actionManager.getAction<ShowWeightMatrixAction>())
        return insertMenu
    }

    /**
     * Create and return a new View menu for this Network panel.
     *
     * @return a new View menu for this Network panel
     */
    fun createViewMenu(): JMenu? {
        val viewMenu = JMenu("View")
        val toolbarMenu = JMenu("Toolbars")
        toolbarMenu.add(actionManager.getMenuItem<ShowRunToolBarAction>(
                runToolBar.isVisible()))
        toolbarMenu.add(actionManager.getMenuItem<ShowMainToolBarAction>(
                mainToolBar.isVisible()))
        toolbarMenu.add(actionManager.getMenuItem<ShowEditToolBarAction>(
                editToolBar.isVisible()))
        viewMenu.add(toolbarMenu)
        viewMenu.addSeparator()
        // TODO
        //viewMenu.add(actionManager.getMenuItem(ShowPrioritiesAction::class.java,
        //        getPrioritiesVisible()))
        //viewMenu.add(actionManager.getMenuItem(ShowWeightsAction::class.java,
        //        getWeightsVisible()))
        return viewMenu
    }

    /**
     * Create a selection JMenu.
     *
     * @return the selection menu.
     */
    fun createSelectionMenu(): JMenu? {
        val selectionMenu = JMenu("Select")
        selectionMenu.add(actionManager.getAction<SelectAllAction>())
        selectionMenu.add(actionManager.getAction<SelectAllWeightsAction>())
        selectionMenu.add(actionManager.getAction<SelectAllNeuronsAction>())
        selectionMenu.add(actionManager.getAction<SelectIncomingWeightsAction>())
        selectionMenu.add(actionManager.getAction<SelectOutgoingWeightsAction>())
        return selectionMenu
    }

    /**
     * Return the align sub menu.
     *
     * @return the align sub menu
     */
    fun createAlignMenu(): JMenu? {
        val alignSubMenu = JMenu("Align")
        alignSubMenu.add(actionManager.getAction<AlignHorizontalAction>())
        alignSubMenu.add(actionManager.getAction<AlignVerticalAction>())
        return alignSubMenu
    }

    /**
     * Return the space sub menu.
     *
     * @return the space sub menu
     */
    fun createSpacingMenu(): JMenu? {
        val spaceSubMenu = JMenu("Space")
        spaceSubMenu.add(actionManager.getAction<SpaceHorizontalAction>())
        spaceSubMenu.add(actionManager.getAction<SpaceVerticalAction>())
        return spaceSubMenu
    }

    /**
     * Create and return a new Help menu for this Network panel.
     *
     * @return a new Help menu for this Network panel
     */
    fun createHelpMenu(): JMenu? {
        val helpAction = ShowHelpAction("Pages/Network.html")
        val helpMenu = JMenu("Help")
        helpMenu.add(helpAction)
        return helpMenu
    }

    /**
     * Create a new context menu for this Network panel.
     *
     * @return the newly constructed context menu
     */
    fun createNetworkContextMenu(): JPopupMenu {
        contextMenu = JPopupMenu()

        // Insert actions
        contextMenu.add(actionManager.getAction<NewNeuronAction>())
        contextMenu.add(AddNeuronsAction(null)) // todo
        contextMenu.add(AddNeuronArrayAction(null)) // todo
        contextMenu.add(AddMultiLayerNet(null))  // todo
        contextMenu.add(actionManager.newNetworkMenu)

        // Clipboard actions
        contextMenu.addSeparator()
        for (action in actionManager.clipboardActions) {
            contextMenu.add(action)
        }
        contextMenu.addSeparator()

        // Connection actions
        contextMenu.add(actionManager.getAction<ClearSourceNeurons>())
        contextMenu.add(actionManager.getAction<SetSourceNeurons>())
        contextMenu.addSeparator()

        // Preferences
        contextMenu.add(actionManager.getAction<ShowNetworkPreferencesAction>())
        return contextMenu
    }


    /**
     * Create the iteration tool bar.
     *
     * @return the toolbar.
     */
    protected fun createRunToolBar(): CustomToolBar {
        val runTools = CustomToolBar()
        runTools.add(actionManager.getAction<IterateNetworkAction>())
        runTools.add(ToggleButton(actionManager.networkControlActions))
        return runTools
    }

    /**
     * Create the main tool bar.
     *
     * @return the toolbar.
     */
    protected fun createMainToolBar(): CustomToolBar {
        val mainTools = CustomToolBar()
        for (action in actionManager.networkModeActions) {
            mainTools.add(action)
        }
        mainTools.addSeparator()
        mainTools.add(ToggleAutoZoom(null)) // todo
        return mainTools
    }

    /**
     * Create the edit tool bar.
     *
     * @return the toolbar.
     */
    protected fun createEditToolBar(): CustomToolBar {
        val editTools = CustomToolBar()
        for (action in actionManager.networkEditingActions) {
            editTools.add(action)
        }
        editTools.add(actionManager.getAction<ClearNodeActivationsAction>())
        editTools.add(actionManager.getAction<RandomizeObjectsAction>())
        return editTools
    }

    fun setSelection(screenElement: ScreenElement) {
        selectionModel.setSelection(screenElement)
    }

    fun setSelection(screenElements: Collection<ScreenElement>) {
        selectionModel.setSelection(screenElements)
    }

    fun toggleSelection(screenElement: ScreenElement) {
        if (screenElement in selectionModel) {
            selectionModel.remove(screenElement)
        } else {
            selectionModel.add(screenElement)
        }
    }


    fun clearSelection() {
        selectionModel.clear()
    }

    /**
     * Rescales the camera so that all objects in the canvas can be seen. Compare "zoom to fit page" in draw programs.
     *
     * @param forceZoom if true force the zoom to happen
     */
    fun zoomToFitPage(forceZoom: Boolean) {
        // TODO: Add a check to see if network is running
        if (autoZoomMode && editMode.isSelection || forceZoom) {
            val filtered = canvas.layer.getUnionOfChildrenBounds(null)
            val adjustedFiltered = PBounds(filtered.getX() - 10, filtered.getY() - 10,
                    filtered.getWidth() + 20, filtered.getHeight() + 20)
            canvas.camera.setViewBounds(adjustedFiltered)
        }
    }

    /**
     * Display the provided network in a dialog
     *
     * @param network the model network to show
     */
    companion object showNetwork {
        //val np = NetworkPanel(, net); // TODO: How to do this? Mock up a Desktop Component
        //np.initScreenElements();
        val frame = JFrame()
        //frame.setContentPane(np)
        //frame.setPreferredSize(new Dimension(500, 500));
        //frame.pack();
        //frame.setVisible(true);
        //frame.addWindowListener(new WindowAdapter() {
        //    public void windowClosing(WindowEvent we) {
        //        System.exit(0);
        //    }
        //});
        //System.out.println(np.debugString());
    }

}