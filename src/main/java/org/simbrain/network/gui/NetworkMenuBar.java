// TODO: This should be removed since we're getting rid of the NetworkDesktopPanel vs. NetworkPanel distinction
///*
// * Part of Simbrain--a java-based neural network kit
// * Copyright (C) 2005,2007 The Authors.  See http://www.simbrain.net/credits
// *
// * This program is free software; you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation; either version 2 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program; if not, write to the Free Software
// * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
// */
//package org.simbrain.network.gui;
//
//import org.simbrain.network.groups.NeuronGroup;
//import org.simbrain.network.gui.actions.edit.*;
//import org.simbrain.network.gui.actions.neuron.NewNeuronAction;
//import org.simbrain.network.gui.actions.neuron.SetNeuronPropertiesAction;
//import org.simbrain.network.gui.actions.neuron.ShowPrioritiesAction;
//import org.simbrain.network.gui.actions.selection.*;
//import org.simbrain.network.gui.actions.synapse.SetSynapsePropertiesAction;
//import org.simbrain.network.gui.actions.synapse.ShowWeightsAction;
//import org.simbrain.network.gui.actions.toolbar.ShowEditToolBarAction;
//import org.simbrain.network.gui.actions.toolbar.ShowMainToolBarAction;
//import org.simbrain.network.gui.actions.toolbar.ShowRunToolBarAction;
//import org.simbrain.util.widgets.ShowHelpAction;
//
//import javax.swing.*;
//import java.util.ArrayList;
//
///**
// * Menu bar that appears inside of JPanel; for use in Applets.
// * <p>
// * TODO: Add file menu; fix help menu. TODO: Reduce duplicated code between this
// * and NetworkPanelDesktop. TODO: Rename so that the fact that this is only used
// * in applets is clear.
// */
//public class NetworkMenuBar {
//
//    public static JMenuBar getAppletMenuBar(final NetworkPanel networkPanel) {
//
//        ArrayList<JCheckBoxMenuItem> checkBoxes = new ArrayList<JCheckBoxMenuItem>();
//        NetworkActionManager actionManager = networkPanel.getActionManager();
//
//        JMenuBar returnMenu = new JMenuBar();
//
//        // TODO: The code below works locally but won't work in an unsigned
//        // applet.
//        // I have not figured out how to sign applets properly yet so I'm not
//        // adding this for now
//
//        // JMenu fileMenu = new JMenu("File");
//        // JMenuItem openItem = new JMenuItem("Open");
//        // openItem.addActionListener(new ActionListener() {
//        // public void actionPerformed(ActionEvent e) {
//        // SFileChooser chooser = new SFileChooser(".", "Open Network");
//        // chooser.addExtension("xml");
//        // File theFile = chooser.showOpenDialog();
//        // if (theFile != null) {
//        // Network newNetwork;
//        // try {
//        // newNetwork = (Network) Network.getXStream().fromXML(new
//        // FileInputStream(theFile));
//        // newNetwork.setParent(networkPanel.getNetwork().getParent());
//        // networkPanel.clearPanel();
//        // networkPanel.setNetwork(newNetwork);
//        // networkPanel.syncToModel();
//        // networkPanel.repaint();
//        // } catch (FileNotFoundException e1) {
//        // e1.printStackTrace();
//        // }
//        // }
//        // }
//        // });
//        // fileMenu.add(openItem);
//        // JMenuItem saveItem = new JMenuItem("Save");
//        // saveItem.addActionListener(new ActionListener() {
//        // public void actionPerformed(ActionEvent e) {
//        // SFileChooser chooser = new SFileChooser(".", "Save Network");
//        // chooser.addExtension("xml");
//        // File theFile = chooser.showSaveDialog();
//        // if (theFile != null) {
//        // try {
//        // Network.getXStream().toXML(networkPanel.getNetwork(), new
//        // FileOutputStream(theFile));
//        // } catch (FileNotFoundException e1) {
//        // e1.printStackTrace();
//        // }
//        // }
//        // }
//        // });
//        // fileMenu.add(saveItem);
//        // returnMenu.add(fileMenu);
//
//        JMenu editMenu = new JMenu("Edit");
//        editMenu.addSeparator();
//        editMenu.add(actionManager.getAction<DeleteAction.class>());
//        editMenu.add(actionManager.getAction<DeleteAction.class>());
//        JMenu selectionMenu = new JMenu("Select");
//        selectionMenu.add(actionManager.getAction(SelectAllAction.class));
//        selectionMenu.add(actionManager.getAction(SelectAllWeightsAction.class));
//        selectionMenu.add(actionManager.getAction(SelectAllNeuronsAction.class));
//        selectionMenu.add(actionManager.getAction(SelectIncomingWeightsAction.class));
//        selectionMenu.add(actionManager.getAction(SelectOutgoingWeightsAction.class));
//        editMenu.add(selectionMenu);
//        editMenu.addSeparator();
//        editMenu.add(actionManager.getAction(ClearNodeActivationsAction.class));
//        editMenu.addSeparator();
//        editMenu.addSeparator();
//        //editMenu.add(actionManager.getGroupAction());
//        //editMenu.add(actionManager.getUngroupAction());
//        editMenu.addSeparator();
//        editMenu.add(networkPanel.createAlignMenu());
//        editMenu.add(networkPanel.createSpacingMenu());
//        editMenu.addSeparator();
//        // editMenu.add(actionManager.getShowIOInfoMenuItem());
//        // TODO
//        //editMenu.add(actionManager.getSetAutoZoomToggleButton());
//        editMenu.addSeparator();
//        editMenu.add(actionManager.getAction(SetNeuronPropertiesAction.class));
//        editMenu.add(actionManager.getAction(SetSynapsePropertiesAction.class));
//        returnMenu.add(editMenu);
//
//        JMenu insertMenu = new JMenu("Insert");
//        insertMenu.add(actionManager.getAction(NewNeuronAction.class));
//        insertMenu.add(actionManager.getAction(NeuronGroup.class));
//        insertMenu.add(actionManager.getNewNetworkMenu());
//        returnMenu.add(insertMenu);
//
//        JMenu viewMenu = new JMenu("View");
//        JMenu toolbarMenu = new JMenu("Toolbars");
//        toolbarMenu.add(actionManager.getMenuItem(ShowMainToolBarAction.class,
//                networkPanel.getMainToolBar().isVisible()));
//        toolbarMenu.add(actionManager.getMenuItem(ShowRunToolBarAction.class,
//                networkPanel.getRunToolBar().isVisible()));
//        toolbarMenu.add(actionManager.getMenuItem(ShowEditToolBarAction.class,
//                networkPanel.getEditToolBar().isVisible()));
//        viewMenu.add(toolbarMenu);
//        viewMenu.addSeparator();
//        viewMenu.add(actionManager.getMenuItem(ShowPrioritiesAction.class,
//                networkPanel.getPrioritiesVisible()));
//        viewMenu.add(actionManager.getMenuItem(ShowWeightsAction.class,
//                networkPanel.getLooseWeightsVisible()));
//        returnMenu.add(viewMenu);
//
//        JMenu helpMenu = new JMenu("Help");
//        helpMenu.add(new ShowHelpAction("Pages/Network.html"));
//        returnMenu.add(helpMenu);
//
//        return returnMenu;
//    }
//
//}
