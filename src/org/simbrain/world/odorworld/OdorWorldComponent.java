/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2005,2007 The Authors.  See http://www.simbrain.net/credits
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.simbrain.world.odorworld;

import java.io.InputStream;
import java.io.OutputStream;

import org.simbrain.workspace.WorkspaceComponent;
import org.simbrain.world.odorworld.effectors.Effector;
import org.simbrain.world.odorworld.entities.OdorWorldEntity;
import org.simbrain.world.odorworld.sensors.Sensor;

/**
 * <b>WorldPanel</b> is the container for the world component. Handles toolbar
 * buttons, and serializing of world data. The main environment codes is in
 * {@link OdorWorldPanel}.
 */
public class OdorWorldComponent extends WorkspaceComponent {

    /** Reference to model world. */
    private OdorWorld world = new OdorWorld();

    /**
     * Default constructor.
     */
    public OdorWorldComponent(final String name) {
        super(name);
        addListener();
    }

    /**
     * Constructor used in deserializing.
     *
     * @param name name of world
     * @param world model world
     */
    public OdorWorldComponent(final String name, final OdorWorld world) {
        super(name);
        this.world = world;
        initializeAttributes();
        addListener();
    }

    /**
     * Initialize odor world attributes.
     */
    private void initializeAttributes() {
        //REDO
//        getConsumers().clear();
//        getProducers().clear();
        for (OdorWorldEntity entity : world.getObjectList()) {
            addEntityAttributes(entity);
            for (Sensor sensor : entity.getSensors()) {
                addSensorAttributes(sensor);
            }
            for (Effector effector : entity.getEffectors()) {
                addEffectorAttributes(effector);
            }
        }
    }

    /**
     * Add attributes associated with this sensor.
     *
     * @param sensor the sensor
     */
    private void addSensorAttributes(final Sensor sensor) {
        
        //REDO
        
//        if (sensor instanceof SmellSensor) {
//            addProducer(new SmellProducer(OdorWorldComponent.this,
//                    (SmellSensor) sensor));
//        }
    }

    /**
     * Add attributes associated with this effector.
     *
     * @param effector the effector
     */
    private void addEffectorAttributes(final Effector effector) {
        
        //REDO
        
//        if (effector instanceof RotationEffector) {
//            addConsumer(new LeftTurn(OdorWorldComponent.this,
//                    (RotationEffector) effector));
//            addConsumer(new RightTurn(OdorWorldComponent.this,
//                    (RotationEffector) effector));
//        } else if (effector instanceof StraightMovementEffector) {
//            addConsumer(new Straight(OdorWorldComponent.this,
//                    (StraightMovementEffector) effector));
//        }
    }

    /**
     * Add attributes associated with this entity.
     *
     * @param entity the entity
     */
    private void addEntityAttributes(final OdorWorldEntity entity) {
//        addConsumer(new EntityWrapper(this, entity));
//        addProducer(new EntityWrapper(this, entity));
    }

    /**
     * Initialize this component.
     */
    private void addListener() {
        world.addListener(new WorldListener() {

            public void updated() {
                fireUpdateEvent();
            }
            public void effectorAdded(final Effector effector) {
                addEffectorAttributes(effector);
            }

            public void effectorRemoved(final Effector effector) {
                
                //TODO: Below from NetworkComponent.  There must be an easier way!
                
//                for (Consumer consumer : getConsumers()) {
//                    if (consumer instanceof NeuronWrapper) {
//                        if (((NeuronWrapper)consumer).getNeuron() == e.getObject()) {
//                            removeConsumer(consumer);
//                            break;
//                        }
//                    }
//                }
//                for (Producer producer : getProducers()) {
//                    if (producer instanceof NeuronWrapper) {
//                        if (((NeuronWrapper)producer).getNeuron() == e.getObject()) {
//                            removeProducer(producer);
//                        }
//                    }
//                }
            }

            public void entityAdded(final OdorWorldEntity entity) {
                addEntityAttributes(entity);
            }

            public void entityRemoved(final OdorWorldEntity entity) {
                // TODO Auto-generated method stub
            }

            public void sensorAdded(final Sensor sensor) {
                addSensorAttributes(sensor);
            }

            public void sensorRemoved(Sensor sensor) {
                // TODO Auto-generated method stub
            }
        });
    }

    /**
     * Recreates an instance of this class from a saved component.
     * 
     * @param input
     * @param name
     * @param format
     * @return
     */
    public static OdorWorldComponent open(InputStream input, String name, String format) {
        OdorWorld newWorld = (OdorWorld) OdorWorld.getXStream().fromXML(input);
        return new OdorWorldComponent(name, newWorld);
    }

    @Override
    public String getXML() {
        return OdorWorld.getXStream().toXML(world);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(final OutputStream output, final String format) {
        OdorWorld.getXStream().toXML(world, output);
    }

    @Override
    public void closing() {
        // TODO Auto-generated method stub
    }

    @Override
    public void update() {
        world.update();
    }

    @Override
    public void setCurrentDirectory(final String currentDirectory) { 
        super.setCurrentDirectory(currentDirectory);
        OdorWorldPreferences.setCurrentDirectory(currentDirectory);
    }

    @Override
    public String getCurrentDirectory() {
       return OdorWorldPreferences.getCurrentDirectory();
    }

    /**
     * Returns a reference to the odor world.
     *
     * @return the odor world object.
     */
    public OdorWorld getWorld() {
        return world;
    }
}