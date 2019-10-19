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
package org.simbrain.network.gui;

import org.simbrain.network.NetworkModel;
import org.simbrain.network.core.*;
import org.simbrain.network.dl4j.NeuronArray;
import org.simbrain.network.groups.NeuronGroup;
import org.simbrain.network.util.CopyPaste;
import org.simbrain.network.util.SimnetUtils;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Buffer which holds network objects for cutting and pasting.
 */
public class Clipboard {

    // To add new copy-pastable items, must update:
    // 1) all elseifs here,
    // 2) CopyPaste.getCopy()
    // 3) Network.addObjects
    // 4) NetworkPanel.getSelectedModels()

    /**
     * Static list of cut or copied objects.
     */
    private static ArrayList copiedObjects = new ArrayList();

    /**
     * List of components which listen for changes to this clipboard.
     */
    private static HashSet listenerList = new HashSet();

    /**
     * Distance between pasted elemeents.
     */
    private static final double PASTE_INCREMENT = 15;

    /**
     * Clear the clipboard.
     */
    public static void clear() {
        copiedObjects = new ArrayList();
        fireClipboardChanged();
    }

    /**
     * Add objects to the clipboard.  This happens with cut and copy.
     *
     * @param objects objects to add
     */
    public static void add(final ArrayList objects) {
        copiedObjects = objects;
        //System.out.println("add-->"+ Arrays.asList(objects));
        fireClipboardChanged();
    }

    /**
     * Paste objects into the netPanel.
     *
     * @param net the network to paste into
     */
    public static void paste(final NetworkPanel net) {
        if (isEmpty()) {
            return;
        }

        // Create a copy of the clipboard objects.
        ArrayList copy = CopyPaste.getCopy(net.getNetwork(), copiedObjects);

        // Gather data for translating the object then add the objects to the
        // network.
        Point2D upperLeft = SimnetUtils.getUpperLeft(copiedObjects);

        translate(copy, net.getPlacementManager().getPasteOffset());
        net.getNetwork().addObjects(copy);

        // Select pasted items
        net.setSelection(getPostPasteSelectionObjects(net, copy));
        net.repaint();
    }

    /**
     * Returns those objects that should be selected after a paste.
     *
     * @param net  reference to network panel.
     * @param list list of objects.
     * @return list of objects to be selected after pasting.
     */
    private static ArrayList getPostPasteSelectionObjects(final NetworkPanel net, final ArrayList list) {
        ArrayList<Object> ret = new ArrayList<Object>();
        for (Object object : list) {
            if (object instanceof Neuron) {
                ret.add(net.getObjectNodeMap().get(object));
            } else if (object instanceof Synapse) {
                ret.add(net.getObjectNodeMap().get(object));
            } else if (object instanceof NetworkTextObject) {
                ret.add(net.getObjectNodeMap().get(object));
            } else if (object instanceof NeuronGroup) {
                ret.add(net.getObjectNodeMap().get(object));
            } else if (object instanceof NeuronArray) {
                ret.add(net.getObjectNodeMap().get(object));
            }
        }
        return ret;
    }

    /**
     * @return true if there's nothing in the clipboard, false otherwise
     */
    public static boolean isEmpty() {
        return copiedObjects.isEmpty();
    }

    /**
     * Add the specified clipboard listener.
     *
     * @param l listener to add
     */
    public static void addClipboardListener(final ClipboardListener l) {
        listenerList.add(l);
    }

    /**
     * Fire a clipboard changed event to all registered model listeners.
     */
    public static void fireClipboardChanged() {
        for (Iterator i = listenerList.iterator(); i.hasNext(); ) {
            ClipboardListener listener = (ClipboardListener) i.next();
            listener.clipboardChanged();
        }
    }

    /**
     * Translate a set of network model object.
     */
    public static void translate(final List<NetworkModel> networkObjects, final Point2D pasteOffset) {
        System.out.println("networkObjects = [" + networkObjects + "], pasteOffset = [" + pasteOffset + "]");
        for (NetworkModel model: networkObjects) {
            model.setCenterX(model.getCenterX() + pasteOffset.getX());
            model.setCenterY(model.getCenterY() + pasteOffset.getY());
        }
    }
}
