/*******************************************************************************
 *  Copyright 2007, 2009 Jorge Villalon (jorge.villalon@uai.cl)
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at 
 *  
 *  	http://www.apache.org/licenses/LICENSE-2.0 
 *  	
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License.
 *******************************************************************************/
package tml.conceptmap.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Iterator;

import org.apache.log4j.Logger;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.assignment.FontAction;
import prefuse.action.assignment.StrokeAction;
import prefuse.action.layout.Layout;
import prefuse.action.layout.graph.FruchtermanReingoldLayout;
import prefuse.activity.Activity;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.PrefuseLib;
import prefuse.visual.DecoratorItem;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import tml.conceptmap.ConceptMap;

/**
 * 
 * @author Jorge Villalon
 *
 */
public class PrefuseMap {
	
	private static Logger logger = Logger.getLogger(PrefuseMap.class);
	private ConceptMap cmap = null;
	private Visualization vis = null;
	private Display display = null;
        private int height = 480;
        private int width = 640;
	
    /**
	 * @return the vis
	 */
	public Visualization getVis() {
		return vis;
	}

	/**
	 * @return the display
	 */
	public Display getDisplay() {
		return display;
	}

	/**
	 * @return the cmap
	 */
	public ConceptMap getCmap() {
		return cmap;
	}

	private static final Schema DECORATOR_SCHEMA = PrefuseLib.getVisualItemSchema(); 
    static { 
    	DECORATOR_SCHEMA.setDefault(VisualItem.INTERACTIVE, true); 
    	DECORATOR_SCHEMA.setDefault(VisualItem.TEXTCOLOR, ColorLib.color(Color.black)); 
    	DECORATOR_SCHEMA.setDefault(VisualItem.FONT, FontLib.getFont("Tahoma", Font.BOLD, 10));
    	DECORATOR_SCHEMA.setDefault(VisualItem.FILLCOLOR, ColorLib.color(Color.white));
    }

    public PrefuseMap(ConceptMap map) throws Exception {
        this(map, 640, 480);
    }
    
    public PrefuseMap(String graphml, int w, int h) {
        this.width = w;
        this.height = h;
    	
		Graph graph = null;
		try {			
			InputStream stream = new ByteArrayInputStream(graphml.getBytes(Charset.forName("UTF-8")));
			graph = new GraphMLReader().readGraph(stream);
		} catch ( DataIOException e ) {
			logger.error("Error loading concept map in Prefuse.");
		}

		// add the graph to the visualization as the data group "graph"
		// nodes and edges are accessible as "graph.nodes" and "graph.edges"
		vis = createVisualization(graph);
		
		DragControl dc = new CmDragControl();
		// create a new Display that pull from our Visualization
		display = new Display(vis);
		display.setSize(this.width, this.height); // set display size
		display.addControlListener(dc); // drag items around
		display.addControlListener(new PanControl());  // pan with background left-drag
		display.addControlListener(new ZoomControl()); // zoom with vertical right-drag
		display.pan(this.width/2, this.height/2);
//
		vis.run("color");
		vis.run("layout");		
    }

    public PrefuseMap(ConceptMap map, int w, int h) throws Exception {
		this(map.exportGraphML(), w, h);
		
	}
    
	public void saveImage(File file) throws Exception {
		display.saveImage(new FileOutputStream(file), "JPG", 1.0f);
		logger.info("Concept Map image file saved: " + file.getAbsolutePath());
	}


	class LabelLayout2 extends Layout {
	    public LabelLayout2(String group) {
	        super(group);
	    }
		@Override
		@SuppressWarnings("rawtypes")
		public void run(double frac) {
	        Iterator iter = m_vis.items(m_group);
	        while ( iter.hasNext() ) {
	            DecoratorItem decorator = (DecoratorItem) iter.next();
	            VisualItem decoratedItem = decorator.getDecoratedItem();
	            Rectangle2D bounds = decoratedItem.getBounds();
	            
	            double x = bounds.getCenterX();
	            double y = bounds.getCenterY();

	            /* modification to move edge labels more to the arrow head
	            double x2 = 0, y2 = 0;
	            if (decoratedItem instanceof EdgeItem){
	            	VisualItem dest = ((EdgeItem)decoratedItem).getTargetItem(); 
	            	x2 = dest.getX();
	            	y2 = dest.getY();
	            	x = (x + x2) / 2;
	            	y = (y + y2) / 2;
	            }
	            */
	            
	            setX(decorator, null, x);
	            setY(decorator, null, y);
	        }
	    }
	}
	
	class CmDragControl extends DragControl {
		@Override
		public void itemDragged(VisualItem arg0, MouseEvent arg1) {
			super.itemDragged(arg0, arg1);
			((LabelLayout2)((ActionList)vis.getAction("layout")).get(2)).run();
		}
	}
	
	private Visualization createVisualization(Graph graph) {
		// add the graph to the visualization as the data group "graph"
		// nodes and edges are accessible as "graph.nodes" and "graph.edges"
		Visualization visualization = new Visualization();
		visualization.add("graph", graph);	
		// draw the "name" label for NodeItems

		LabelRenderer r = new LabelRenderer("name");
		r.setRoundedCorner(8, 8); // round the corners
		r.setImageTextPadding(2);

		// create a new default renderer factory
		// return our name label renderer as the default for all non-EdgeItems
		// includes straight line edges for EdgeItems by default
		LabelRenderer edgesRenderer = new LabelRenderer("linking");
		edgesRenderer.setRoundedCorner(8, 8);
		edgesRenderer.setImageTextPadding(2);

		
		DefaultRendererFactory drf = new DefaultRendererFactory(r);
        drf.add(new InGroupPredicate("edgeDeco"), edgesRenderer);		
		visualization.setRendererFactory(drf);
		
        visualization.addDecorators("edgeDeco", "graph.edges", DECORATOR_SCHEMA);

		FontAction font = new FontAction("graph.nodes", FontLib.getFont("Tahoma", Font.BOLD, 12));
		ColorAction fill = new ColorAction("graph.nodes", 
				VisualItem.FILLCOLOR, ColorLib.color(Color.blue));
		// use black for node text
		ColorAction text = new ColorAction("graph.nodes",
				VisualItem.TEXTCOLOR, ColorLib.color(Color.white));
		// use light grey for edges
		int[] pallete = new int[] {ColorLib.color(Color.black), Color.TRANSLUCENT};
		DataColorAction edges = new DataColorAction("graph.edges", "invisible", Constants.NOMINAL,
				VisualItem.STROKECOLOR, pallete);
		StrokeAction edgesLines = new StrokeAction("graph.edges", new BasicStroke(2));

		// create an action list containing all color assignments
		ActionList color = new ActionList();
		color.add(font);
		color.add(fill);
		color.add(text);
		color.add(edges);	
		color.add(edgesLines);

		// create an action list with an animated layout
		// the INFINITY parameter tells the action list to run indefinitely
		logger.info("Default step time:" + Activity.DEFAULT_STEP_TIME);
		ActionList layout = new ActionList(1);
		FruchtermanReingoldLayout fl = new FruchtermanReingoldLayout("graph");
		//ForceDirectedLayout fl = new ForceDirectedLayout("graph");
/*		NodeLinkTreeLayout nl = new NodeLinkTreeLayout(
				"graph", 
				Constants.ORIENT_TOP_BOTTOM, 
				100, 
				50, 
				50);
*/		layout.add(fl);
		layout.add(new RepaintAction());
        layout.add(new LabelLayout2("edgeDeco"));

		// add the actions to the visualization
		visualization.putAction("color", color);
		visualization.putAction("layout", layout);
		
		return visualization;
	}
}
