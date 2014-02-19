package gov.epa.nrmrl.std.lca.ht.compartment.mgr;

/* Modified from:
 * 
 */

/*******************************************************************************
 * Copyright (c) 2006 - 2013 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 414565
 *******************************************************************************/

//package org.eclipse.jface.snippets.viewers;

import gov.epa.nrmrl.std.lca.ht.compartment.mgr.TreeNode;
import harmonizationtool.ColumnLabelProvider;
import harmonizationtool.tree.Node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A simple TreeViewer to demonstrate usage
 * 
 */

public class TreeSnippet {
	private class MyContentProvider implements ITreeContentProvider {

		public Object[] getElements(Object inputElement) {
			Iterator<Node> iter = ((TreeNode) inputElement).getChildIterator();
			List<Node> l = new ArrayList<Node>();
			while (iter.hasNext()) {
				l.add(iter.next());
			}
			return l.toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public Object[] getChildren(Object parentElement) {
			return getElements(parentElement);
		}

		public Object getParent(Object treeNode) {
			if (treeNode == null) {
				return null;
			}
			return ((TreeNode) treeNode).getParent();
		}

		public boolean hasChildren(Object treeNode) {
			return ((Node) treeNode).getChildIterator().hasNext();
		}
	}

	public TreeSnippet(Shell shell) {
		final TreeViewer v = new TreeViewer(shell);
		v.getTree().setLinesVisible(true);
		v.setLabelProvider(new ColumnLabelProvider() {
			// private Color currentColor = null;

			// @Override
			public String getText(Object treeNode) {
				return ((TreeNode) treeNode).nodeName;
			}
		});
		v.setContentProvider(new MyContentProvider());
		v.setInput(createHarmonizeCompartments());
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new TreeSnippet(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();
	}

	private TreeNode createHarmonizeCompartments() {
		TreeNode masterCompartmentTree = new TreeNode(null);

		TreeNode release = new TreeNode(masterCompartmentTree);
		release.nodeName = "Release";

		TreeNode air = new TreeNode(release);
		air.nodeName = "air";
		TreeNode lowPop = new TreeNode(air);
		lowPop.nodeName = "low population density";
		TreeNode airUnspec = new TreeNode(air);
		airUnspec.nodeName = "unspecified";
		TreeNode airHighPop = new TreeNode(air);
		airHighPop.nodeName = "high population density";
		TreeNode airLowPopLongTerm = new TreeNode(air);
		airLowPopLongTerm.nodeName = "low population density, long-term";
		TreeNode airLowerStratPlusUpperTrop = new TreeNode(air);
		airLowerStratPlusUpperTrop.nodeName = "lower stratosphere + upper troposphere";

		TreeNode water = new TreeNode(release);
		water.nodeName = "water";
		TreeNode waterFossil = new TreeNode(water);
		waterFossil.nodeName = "fossil-";
		TreeNode waterFresh = new TreeNode(water);
		waterFresh.nodeName = "fresh-";
		TreeNode waterFreshLongTerm = new TreeNode(water);
		waterFreshLongTerm.nodeName = "fresh-, long-term";
		TreeNode waterGround = new TreeNode(water);
		waterGround.nodeName = "ground-";
		TreeNode waterGroundLongTerm = new TreeNode(water);
		waterGroundLongTerm.nodeName = "ground-, long-term";
		TreeNode waterLake = new TreeNode(water);
		waterLake.nodeName = "lake";
		TreeNode waterOcean = new TreeNode(water);
		waterOcean.nodeName = "ocean";
		TreeNode waterRiver = new TreeNode(water);
		waterRiver.nodeName = "river";
		TreeNode waterRiverLongTerm = new TreeNode(water);
		waterRiverLongTerm.nodeName = "river, long-term";
		TreeNode waterSurface = new TreeNode(water);
		waterSurface.nodeName = "surface water";
		TreeNode waterUnspec = new TreeNode(water);
		waterUnspec.nodeName = "unspecified";

		TreeNode soil = new TreeNode(release);
		soil.nodeName = "soil";
		TreeNode soilAgricultural = new TreeNode(soil);
		soilAgricultural.nodeName = "agricultural";
		TreeNode soilForestry = new TreeNode(soil);
		soilForestry.nodeName = "forestry";
		TreeNode soilIndustrial = new TreeNode(soil);
		soilIndustrial.nodeName = "industrial";
		TreeNode soilUnspec = new TreeNode(soil);
		soilUnspec.nodeName = "unspecified";

		TreeNode resource = new TreeNode(masterCompartmentTree);
		resource.nodeName = "Resource";

		TreeNode resourceBiotic = new TreeNode(resource);
		resourceBiotic.nodeName = "biotic";
		TreeNode resourceInAir = new TreeNode(resource);
		resourceInAir.nodeName = "in air";
		TreeNode resourceInGround = new TreeNode(resource);
		resourceInGround.nodeName = "in ground";
		TreeNode resourceInLand = new TreeNode(resource);
		resourceInLand.nodeName = "in land";
		TreeNode resourceInWater = new TreeNode(resource);
		resourceInWater.nodeName = "in water";
		TreeNode resourceUnspec = new TreeNode(resource);
		resourceUnspec.nodeName = "unspecified";
		return masterCompartmentTree;
	}
}
