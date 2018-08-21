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

import org.simbrain.util.SimpleId;
import org.simbrain.util.math.SimbrainMath;
import org.simbrain.world.odorworld.effectors.Effector;
import org.simbrain.world.odorworld.effectors.StraightMovement;
import org.simbrain.world.odorworld.effectors.Turning;
import org.simbrain.world.odorworld.entities.OdorWorldEntity;
import org.simbrain.world.odorworld.entities.RotatingEntity;
import org.simbrain.world.odorworld.sensors.ObjectSensor;
import org.simbrain.world.odorworld.sensors.Sensor;
import org.simbrain.world.odorworld.sensors.SmellSensor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Core model class of Odor World, which contains a list of entities in the
 * world. Some code from Developing Games in Java, by David Brackeen.
 */
public class OdorWorld {

    /**
     * List of odor world entities.
     */
    private List<OdorWorldEntity> entityList = new CopyOnWriteArrayList<OdorWorldEntity>();

//    /**
//     * Listeners on this odor world.
//     */
//    private List<WorldListener> listenerList = new ArrayList<WorldListener>();

    /**
     * Sum of lengths of smell vectors for all smelly objects in the world.
     */
    private double totalSmellVectorLength;

    /**
     * Whether or not sprites wrap around or are halted at the borders
     */
    private boolean wrapAround = true;

    /**
     * If true, then objects block movements; otherwise agents can walk through
     * objects.
     */
    private boolean objectsBlockMovement = true;

    /**
     * Height of world.
     */
    private int height = 450;

    /**
     * Width of world.
     */
    private int width = 450;

    /**
     * Entity Id generator.
     */
    private SimpleId entityIDGenerator = new SimpleId("Entity", 1);

    /**
     * Sensor Id generator.
     */
    private SimpleId sensorIDGenerator = new SimpleId("Sensor", 1);

    /**
     * Effector Id generator.
     */
    private SimpleId effectorIDGenerator = new SimpleId("Effector", 1);

    /**
     * Agent Name generator.
     */
    private SimpleId agentNameGenerator = new SimpleId("Agent", 1);

    /**
     * Support for property change events.
     */
    private transient PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    /**
     * Default constructor.
     */
    OdorWorld() {
    }

    /**
     * Update world.
     *
     * @param time an integer representation of time.
     */
    public void update(int time) {
        for (OdorWorldEntity entity : entityList) {
            entity.updateSmellSource();
            entity.update();
        }
        changeSupport.firePropertyChange("worldUpdated", null, null);
    }

    /**
     * Add an Odor World Entity.
     *
     * @param entity the entity to add
     */
    public void addEntity(final OdorWorldEntity entity) {

        // Set the entity's id
        entity.setId(entityIDGenerator.getId());
        entity.setName(entity.getId());

        // Add entity to the map
        // map.addSprite(entity);
        entityList.add(entity);

        // Fire entity added event
        // fireEntityAdded(entity);

        changeSupport.firePropertyChange("entityAdded", null, entity);

        // Recompute max stimulus length
        recomputeMaxStimulusLength();

    }

    /**
     * Does the world contain this entity?
     *
     * @param entity the entity to check for
     * @return whether it is in this world or not.
     */
    public boolean containsEntity(final OdorWorldEntity entity) {
        return entityList.contains(entity);
    }

    /**
     * Adds an agent and by default adds several sensors and effectors to it.
     *
     * @param entity the entity corresponding to the agent
     */
    public void addAgent(final OdorWorldEntity entity) {

        entity.setEntityType(OdorWorldEntity.EntityType.MOUSE);
        entity.setName(agentNameGenerator.getId());

        if (entity instanceof RotatingEntity) {

            // Add default effectors
            entity.addEffector(new StraightMovement((RotatingEntity) entity, "Go-straight"));
            entity.addEffector(new Turning((RotatingEntity) entity, "Go-left", Turning.LEFT));
            entity.addEffector(new Turning((RotatingEntity) entity, "Go-right", Turning.RIGHT));

            // Add default sensors
            entity.addSensor(new SmellSensor(entity, "Smell-Left", Math.PI / 8, 50));
            entity.addSensor(new SmellSensor(entity, "Smell-Center", 0, 0));
            entity.addSensor(new SmellSensor(entity, "Smell-Right", -Math.PI / 8, 50));

            // Temp testing
            entity.addSensor(new ObjectSensor(entity, "Swiss", "Swiss.gif"));

        }
        addEntity(entity);
    }

    /**
     * Returns the entity with the given id, or, failing that, a given name. If
     * no entity is found return null.
     *
     * @param id name of entity
     * @return matching entity, if any
     */
    public OdorWorldEntity getEntity(final String id) {
        // Search by id
        for (OdorWorldEntity entity : entityList) {
            if (entity.getId().equalsIgnoreCase(id)) {
                return entity;
            }
        }
        // Search for label if no matching id found
        for (OdorWorldEntity entity : entityList) {
            if (entity.getName().equalsIgnoreCase(id)) {
                return entity;
            }
        }
        // Matching entity not found
        return null;
    }

    /**
     * Return sensor with matching id or null if none found.
     */
    public Object getSensor(String id) {
        for (OdorWorldEntity entity : entityList) {
            for (Sensor sensor : entity.getSensors()) {
                if (sensor.getId().equalsIgnoreCase(id)) {
                    return sensor;
                }
            }
        }
        return null;
    }

    /**
     * Return effector with matching id or null if none found.
     */
    public Object getEffector(String id) {
        for (OdorWorldEntity entity : entityList) {
            for (Effector effector : entity.getEffectors()) {
                if (effector.getId().equalsIgnoreCase(id)) {
                    return effector;
                }
            }
        }
        return null;
    }

    /**
     * Returns the sensor with the given id, or null if none is found.
     *
     * @param entityId entity id
     * @param sensorId sensor id
     * @return sensor if found
     */
    public Sensor getSensor(final String entityId, final String sensorId) {
        OdorWorldEntity entity = getEntity(entityId);
        if (entity == null) {
            return null;
        }
        for (Sensor sensor : entity.getSensors()) {
            if (sensor.getId().equalsIgnoreCase(sensorId)) {
                return sensor;
            }
        }
        return null;
    }

    /**
     * Returns the effector with the given id, or null if none is found.
     *
     * @param entityId   entity id
     * @param effectorId sensor id
     * @return effector if found
     */
    public Effector getEffector(final String entityId,
                                final String effectorId) {
        OdorWorldEntity entity = getEntity(entityId);
        if (entity == null) {
            return null;
        }
        for (Effector effector : entity.getEffectors()) {
            if (effector.getId().equalsIgnoreCase(effectorId)) {
                return effector;
            }
        }
        return null;

    }

    /**
     * Delete entity.
     *
     * @param entity the entity to delete
     */
    public void deleteEntity(OdorWorldEntity entity) {
        // map.removeSprite(entity);
        if (entityList.contains(entity)) {
            entityList.remove(entity);
            entity.delete();
            for (Sensor sensor : entity.getSensors()) {
//                fireSensorRemoved(sensor);
            }
            for (Effector effector : entity.getEffectors()) {
//                fireEffectorRemoved(effector);
            }
            recomputeMaxStimulusLength();
//            fireEntityRemoved(entity);
        }
    }

    /**
     * Delete all entities.
     */
    public void deleteAllEntities() {
        for (OdorWorldEntity entity : entityList) {
            deleteEntity(entity);
        }
    }

    /**
     * Computes maximum stimulus length. This is used for scaling the color in
     * the graphical display of the agent sensors.
     */
    private void recomputeMaxStimulusLength() {
        totalSmellVectorLength = 0;
        for (OdorWorldEntity entity : entityList) {
            if (entity.getSmellSource() != null) {
                totalSmellVectorLength += SimbrainMath.getVectorNorm(entity.getSmellSource().getStimulusVector());
            }
        }
    }

    /**
     * Standard method call made to objects after they are deserialized. See:
     * http://java.sun.com/developer/JDCTechTips/2002/tt0205.html#tip2
     * http://xstream.codehaus.org/faq.html
     *
     * @return Initialized object.
     */
    private Object readResolve() {
        if (agentNameGenerator == null) {
            agentNameGenerator = new SimpleId("Agent", 1);
        }

        changeSupport = new PropertyChangeSupport(this);

        for (OdorWorldEntity entity : entityList) {
            entity.postSerializationInit();
        }
        recomputeMaxStimulusLength();
        return this;
    }

    //TODO: Collision detection should probably be handled by piccolo for now.
    // It does make this a weird "model" with incomplete information about the virtual
    // physics, etc...

//    /**
//     * Updates all entities.
//     */
//    private void updateEntity(final OdorWorldEntity entity, final int time) {
//
//        // Collision detection
//        double dx = entity.getVelocityX();
//        double oldX = entity.getX();
//        double dy = entity.getVelocityY();
//        double oldY = entity.getY();
//
//        // Very simple motion
//        if (dx != 0) {
//            entity.setX(entity.getX() + dx);
//        }
//        if (dy != 0) {
//            entity.setY(entity.getY() + dy);
//        }
//
//        // Behavior
//        // entity.getBehavior().apply(time);
//
//        // Handle sprite collisions
//        //        entity.setHasCollided(false);
//        //        for (OdorWorldEntity otherEntity : entityList) {
//        //            if (entity == otherEntity) {
//        //                continue;
//        //            }
//        //            if (otherEntity.getReducedBounds().intersects(entity.getReducedBounds())) {
//        //                otherEntity.setHasCollided(true);
//        //            }
//        //        }
//        //
//        // // Handle sprite collisions
//        // if (xCollission(entity, newX)) {
//        // entity.collideHorizontal();
//        // } else {
//        // // sprite.setX(newX);
//        // }
//        // if (yCollission(entity, newY)) {
//        // entity.collideVertical();
//        // } else {
//        // // sprite.setY(newY);
//        // }
//
//        // Update creature
//        entity.update();
//
//        // System.out.println(sprite.getId() + " new - x: " + sprite.getX() +
//        // " y:" + sprite.getY());
//    }

    /**
     * Handle collisions in x directions.
     *
     * @param entityToCheck
     * @param xCheck        position to check
     * @return whether or not a collision occurred.
     */
    private boolean xCollission(OdorWorldEntity entityToCheck, float xCheck) {

        // Hit a wall
        // if ((entityToCheck.getX() < 0) || (entityToCheck.getX() >
        // getWidth())) {
        // return true;
        // }

        // Check for collisions with sprites
        for (OdorWorldEntity entity : entityList) {
            if (entity == entityToCheck) {
                continue;
            }
//            if ((entityToCheck.getX() > entity.getX()) && (entityToCheck.getX() < (entity.getX() + entity.getWidth()))) {
//                return true;
//            }
        }
        return false;
    }

    /**
     * Handle collisions in y directions.
     *
     * @param entityToCheck
     * @param yCheck        position to check
     * @return whether or not a collision occurred.
     */
    private boolean yCollission(OdorWorldEntity entityToCheck, float yCheck) {
        // Hit a wall
        // if ((entityToCheck.getY() < 0) || (entityToCheck.getY() >
        // getHeight())) {
        // return true;
        // }

        // Check for collisions with sprites
        for (OdorWorldEntity sprite : entityList) {

            if (sprite == entityToCheck) {
                continue;
            }

//            if ((entityToCheck.getY() > sprite.getY()) && (entityToCheck.getY() < (sprite.getY() + sprite.getHeight()))) {
//                return true;
//            }
        }
        return false;
    }

    /**
     * Returns the list of entities.
     *
     * @return the entity list
     */
    public List<OdorWorldEntity> getEntityList() {
        return entityList;
    }


    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    /**
     * @return the wrapAround
     */
    public boolean getWrapAround() {
        return wrapAround;
    }

    /**
     * @param wrapAround the wrapAround to set
     */
    public void setWrapAround(boolean wrapAround) {
        this.wrapAround = wrapAround;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(int height) {
        // TODO;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(int width) {
        // TODO;
    }

    /**
     * Returns width of world in pixels.
     *
     * @return width in pixels.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns height of world in pixels.
     *
     * @return height of world
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return the objectsBlockMovement
     */
    public boolean isObjectsBlockMovement() {
        return objectsBlockMovement;
    }

    /**
     * @param objectsBlockMovement the objectsBlockMovement to set
     */
    public void setObjectsBlockMovement(boolean objectsBlockMovement) {
        this.objectsBlockMovement = objectsBlockMovement;
    }

    /**
     * @return the maxSmellVectorLength
     */
    public double getTotalSmellVectorLength() {
        return totalSmellVectorLength;
    }

    /**
     * Use the provided set of vectors (stored as a 2-d array of doubles, one
     * vector per row) to set the stimulus vectors on all odor world entities,
     * in the order in which they are stored in the internal list (which should
     * match the order in which they were added to the world).
     *
     * @param stimulusVecs the 2d matrix of stimulus vectors
     */
    public void loadStimulusVectors(double[][] stimulusVecs) {
        Iterator<OdorWorldEntity> entityIterator = getEntityList().iterator();
        for (int i = 0; i < stimulusVecs.length; i++) {
            if (entityIterator.hasNext()) {
                OdorWorldEntity entity = entityIterator.next();
                if (entity.getSmellSource() != null) {
                    //System.out.println(entity);
                    entity.getSmellSource().setStimulusVector(stimulusVecs[i]);
                }
            }
        }
    }

    public SimpleId getSensorIDGenerator() {
        return sensorIDGenerator;
    }

    public SimpleId getEffectorIDGenerator() {
        return effectorIDGenerator;
    }

}
