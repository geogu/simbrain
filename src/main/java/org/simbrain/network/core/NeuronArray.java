package org.simbrain.network.core;


import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * High performance immutable array backed by ND4J Array.
 */
//TOOD: Name? More like layer?  Or tensor?
public class NeuronArray {

    /**
     * Reference to network this neuron is part of.
     */
    private final Network parent;

    /**
     * ND4J Array backing this object
     */
    private INDArray neuronArray = Nd4j.rand(1000,1000).subi(0.5).mul(2);

    /**
     * x-coordinate of this neuron in 2-space.
     */
    private double x;

    /**
     * y-coordinate of this neuron in 2-space.
     */
    private double y;

    /**
     * z-coordinate of this neuron in 3-space. Currently no GUI implementation,
     * but fully useable for scripting. Like polarity this will get a full
     * implementation in the next development cycle... probably by 4.0.
     */
    private double z;

    /**
     * Render an image showing each activation when true.
     */
    private boolean renderActivations = true;

    /**
     * Support for property change events.
     */
    private transient PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    public NeuronArray(Network net) {
        parent = net;
    }

    public void update() {

        // TODO: This is just a place holder. Do something useful.
        neuronArray = Nd4j.rand(1000,1000).subi(0.5).mul(2);

        changeSupport.firePropertyChange("updated", null, null);
    }

    public int getRows() {
        return neuronArray.rows();
    }

    public int getCols() {
        return neuronArray.columns();
    }

    public INDArray getNeuronArray() {
        return neuronArray;
    }



    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public boolean isRenderActivations() {
        return renderActivations;
    }

    public void setRenderActivations(boolean renderActivations) {
        this.renderActivations = renderActivations;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }
}
