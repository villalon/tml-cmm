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
package tml.conceptmap;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DirectedMultigraph;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ConceptMap extends DirectedMultigraph<Concept, Relationship<Concept>> {

	private static Logger logger = Logger.getLogger(ConceptMap.class);
	private String text;
	private String name;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	@SuppressWarnings("unchecked")
	public ConceptMap() throws Exception {
		super(new ClassBasedEdgeFactory<Concept, Relationship<Concept>>((Class<? extends Relationship<Concept>>) Relationship.class));
	}

	public void printMap() {
		logger.debug("-----------------" + this.name + "--------------------");
		if(this.text != null)
			logger.debug(this.text);
		else
			logger.debug("No text available");
		logger.debug("---Isolated Concepts---");
		int vertexNumber = 0;
		for(Concept vertex : this.vertexSet()) {
			if(this.outDegreeOf(vertex) + this.inDegreeOf(vertex) == 0) {
				vertexNumber++;
				logger.debug(vertexNumber + ":" + vertex);				
			}
		}
		logger.debug("---Relationships---");
		int relationship = 0;
		for(Relationship<Concept> rel : this.edgeSet()) {
			relationship++;
			logger.debug(relationship + ":" + rel.getSource() + " ---" + rel.getLinkingWord() + "---> " + rel.getTarget());			
		}
		logger.debug("-------------------------------------");
	}

	@Override
	public boolean addVertex(Concept arg0) {
		if(arg0.getTerm().matches("^[\\.,;-_].*")
				|| arg0.getTerm().matches(".*[\\.,;-_]$"))
			logger.error(arg0.getTerm() + " contains invalid characters!");
		return super.addVertex(arg0);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -3770863677314251953L;

	public String exportGraphML() {
		List<Concept> vertices = new ArrayList<Concept>();
		vertices.addAll(this.vertexSet());

		StringBuffer buff = new StringBuffer();
		buff.append(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
				"<!--  An excerpt of an egocentric social network  -->\n"+
				"<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">\n"+
				"<graph edgedefault=\"directed\">\n"+
				"<!-- data schema -->\n"+
				"<key id=\"name\" for=\"node\" attr.name=\"name\" attr.type=\"string\"/>\n"+
				"<key id=\"linking\" for=\"edge\" attr.name=\"linking\" attr.type=\"string\"/>\n"+
				"<key id=\"invisible\" for=\"edge\" attr.name=\"invisible\" attr.type=\"string\"/>\n"+
				"<!-- nodes -->\n"
		);

		List<Integer> verticesInMap = new ArrayList<Integer>();
		for(Concept node : this.vertexSet()) {
			int id = vertices.indexOf(node);
			if(id < 0)
				logger.error("Big problem, couldn't find " + node);

			// Ignore isolated nodes. Necessary?
			if(this.outDegreeOf(node) == 0
					&& this.inDegreeOf(node) == 0)
				continue;

			verticesInMap.add(id);
			buff.append(
					"<node id=\"n" + id +	"\">" +
					"<data key=\"name\">" + StringEscapeUtils.escapeXml(node.getTerm()) + "</data>" +
			"</node>\n");
		}

		//                for(int i=0; i < verticesInMap.size(); i++) {
		//                    for(int j=i+1; j < verticesInMap.size(); j++) {
		//                        int node1 = verticesInMap.get(i);
		//                        int node2 = verticesInMap.get(j);
		//			buff.append("<edge source=\"n" + node1 +
		//					"\" target=\"n" + node2 + "\">" +
		//					"<data key=\"linking\">" + "?" + "</data>" +
		//					"</edge>\n");
		//                    }
		//                }
		for(Relationship<Concept> edge : this.edgeSet()) {
			int idSource = vertices.indexOf(edge.getSource());
			int idTarget = vertices.indexOf(edge.getTarget());
			buff.append("<edge source=\"n" + idSource +
					"\" target=\"n" + idTarget + "\">" +
					"<data key=\"linking\">" + edge.getLinkingWord() + "</data>" +
					"<data key=\"invisible\">" + "false" + "</data>" +
			"</edge>\n");
		}
		
		ConnectivityInspector<Concept, Relationship<Concept>> inspector = new ConnectivityInspector<Concept, Relationship<Concept>>(new AsUndirectedGraph<Concept, Relationship<Concept>>(this));

		List<Set<Concept>> sets = new ArrayList<Set<Concept>>();
		sets.addAll(inspector.connectedSets());
		Collections.sort(sets,new Comparator<Set<Concept>>() {
			public int compare(Set<Concept> o1, Set<Concept> o2) {
				return o2.size()-o1.size();
			}
		});
		
		ArrayList<Concept> centers = new ArrayList<Concept>();
		for(Set<Concept> set : sets) {
			int max = -1;
			Concept maxConcept = null;
			for(Concept c : set) {
				int degree = this.inDegreeOf(c) + this.outDegreeOf(c); 
				if(degree > max) {
					max = degree;
					maxConcept = c;
				}
			}
			logger.debug("Center is " + maxConcept + " with " + max);
			if(max > 0)
			centers.add(maxConcept);
		}
		
		int type = 2;
		if(centers.size() > 1) {
			switch(type) {
			case 1:
				for(int i=0; i<centers.size();i++) {
					for(int j=i+1; j<centers.size();j++) {
						int idSource = vertices.indexOf(centers.get(i));
						int idTarget = vertices.indexOf(centers.get(j));
						if(idSource >= 0 && idTarget >= 0)
						buff.append("<edge source=\"n" + idSource +
								"\" target=\"n" + idTarget + "\">" +
								"<data key=\"linking\"></data>" +
								"<data key=\"invisible\">" + "true" + "</data>" +
						"</edge>\n");
					}
				}
				break;
			case 2:
				int idSource = vertices.indexOf(centers.get(0));
				int idTarget = vertices.indexOf(centers.get(centers.size()-1));
				if(idSource >= 0 && idTarget >= 0)
				buff.append("<edge source=\"n" + idSource +
						"\" target=\"n" + idTarget + "\">" +
						"<data key=\"linking\"></data>" +
						"<data key=\"invisible\">" + "true" + "</data>" +
				"</edge>\n");
				for(int i=0; i<centers.size()-1;i++) {
						idSource = vertices.indexOf(centers.get(i));
						idTarget = vertices.indexOf(centers.get(i+1));
						if(idSource >= 0 && idTarget >= 0)
						buff.append("<edge source=\"n" + idSource +
								"\" target=\"n" + idTarget + "\">" +
								"<data key=\"linking\"></data>" +
								"<data key=\"invisible\">" + "true" + "</data>" +
						"</edge>\n");
				}
				break;
				default:
					buff.append(
							"<node id=\"n" + vertices.size() +	"\">" +
							"<data key=\"name\">" + "-" + "</data>" +
					"</node>\n");
					for(int i=0; i<centers.size();i++) {
							idSource = vertices.indexOf(centers.get(i));
							if(idSource >= 0)
							buff.append("<edge source=\"n" + idSource +
									"\" target=\"n" + vertices.size() + "\">" +
									"<data key=\"linking\"></data>" +
									"<data key=\"invisible\">" + "true" + "</data>" +
							"</edge>\n");
					}
			}
		}

		buff.append(
				"</graph>\n"+
				"</graphml>"
		);

		return buff.toString();
	}

	public String exportPropositionsAsText() {

		StringBuffer buff = new StringBuffer();
		for(Relationship<Concept> edge : this.edgeSet()) {
			buff.append(edge.getSource().getTerm() + "\t" +
					edge.getLinkingWord() + "\t" +
					edge.getTarget().getTerm() +
			"\n");
		}

		return buff.toString();
	}

	public void summarizeUsingCentrality(int maxConcepts) {
		ConnectivityInspector<Concept, Relationship<Concept>> inspector = new ConnectivityInspector<Concept, Relationship<Concept>>(new AsUndirectedGraph<Concept, Relationship<Concept>>(this));

		List<Set<Concept>> sets = new ArrayList<Set<Concept>>();
		sets.addAll(inspector.connectedSets());
		Collections.sort(sets,new Comparator<Set<Concept>>() {
			public int compare(Set<Concept> o1, Set<Concept> o2) {
				return o2.size()-o1.size();
			}
		});

		int totalConcepts = 0;
		for(Set<Concept> set : sets) {
			if(totalConcepts > maxConcepts)
			for(Concept c : set) {
				this.removeVertex(c);
			}
			else
				totalConcepts+=set.size();
		}		
	}
	@Override
	public String toString() {
		String txt = "";
		if(this.name != null)
			txt += this.name;
		if(this.text != null)
			txt += this.text;
		txt+= "[" + this.vertexSet().size() + "," + this.edgeSet().size() + "]";
		return txt;
	}

	public static ConceptMap getFromXML(String filename) throws Exception {
		File f = new File(filename);
		if(!f.exists())
			throw new Exception("Concept Map file doesn't exist");
		logger.debug("Loading Concept Map from file " + f.getName());
		ConceptMap map = new ConceptMap();
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(f, map.new ConceptMapHandler(map));
		} catch (Throwable e) {
			e.printStackTrace ();
			throw new Exception("Error parsing XML for Concept Map file " + f.getName(), e);
		}
		logger.debug("Concept Map loaded from file " + filename);
		map.setName(f.getName());
		if(map.vertexSet().size() == 0)
			logger.warn("Concept Map " + map + " is empty!");
		return map;
	}

	public void writeToXML(String filename) throws Exception {
		FileWriter writer = new FileWriter(filename);
		writer.append("<?xml version=\"1.0\"?>\n");
		writer.append("<conceptmap>\n");
		for(Concept concept : this.vertexSet()) {
			writer.append("  <concept label=\"");
			writer.append(StringEscapeUtils.escapeXml(concept.getTerm()));
			writer.append("\"/>\n");
		}
		for(Relationship<Concept> relationship : this.edgeSet()) {
			writer.append("  <relationship label=\"");
			writer.append(StringEscapeUtils.escapeXml(relationship.getLinkingWord()));
			writer.append("\">\n");
			writer.append("    <concept label=\"");
			writer.append(StringEscapeUtils.escapeXml(relationship.getSource().getTerm()));
			writer.append("\"/>\n");
			writer.append("    <concept label=\"");
			writer.append(StringEscapeUtils.escapeXml(relationship.getTarget().getTerm()));
			writer.append("\"/>\n");			
			writer.append("  </relationship>\n");			
		}
		writer.append("</conceptmap>\n");
		writer.close();
	}
	
	public String getCmapWebXML() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<?xml version=\"1.0\"?>\n");
		buffer.append("<conceptmap>\n");
		List<Concept> concepts = new ArrayList<Concept>();
		concepts.addAll(this.vertexSet());
		for(Concept concept : concepts) {
			buffer.append("  <concept id=\"");
			buffer.append(concepts.indexOf(concept)+1);
			buffer.append("\" label=\"");
			buffer.append(StringEscapeUtils.escapeXml(concept.getTerm()));
			buffer.append("\"/>\n");
		}
		int relationshipId = 1;
		for(Relationship<Concept> relationship : this.edgeSet()) {
			buffer.append("  <relationship id=\"");
			buffer.append(relationshipId);
			buffer.append("\" linkingWord=\"");
			buffer.append(StringEscapeUtils.escapeXml(relationship.getLinkingWord()));
			buffer.append("\" source=\"");
			buffer.append(concepts.indexOf(relationship.getSource())+1);
			buffer.append("\" target=\"");
			buffer.append(concepts.indexOf(relationship.getTarget())+1);
			buffer.append("\"/>\n");
			relationshipId++;
		}
		buffer.append("</conceptmap>\n");
		return buffer.toString();
	}

	private class ConceptMapHandler extends DefaultHandler {
		private boolean inRelationship = false;
		private ConceptMap map = null;
		private String currentLinkingWord = null;
		private List<Concept> concepts = null;
		private int currentPosX = 0;
		private int currentPosY = 0;

		public ConceptMapHandler(ConceptMap cm) {
			this.map = cm;
		}

		@Override
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException {
			super.startElement(uri, localName, name, attributes);
			if(name.equals("concept")) {
				String label = attributes.getValue("label");
				if(!inRelationship) {
					Concept c = new Concept(label);
					if(attributes.getValue("posx")!=null && attributes.getValue("posy")!=null) {
						c.setPositionX(Integer.parseInt(attributes.getValue("posx")));
						c.setPositionY(Integer.parseInt(attributes.getValue("posy")));
					}
					logger.debug("Adding new concept " + c);
					map.addVertex(c);
				}
				else {
					Concept concept = map.getConceptFromTerm(label).get(0);
					if(concept == null) {
						logger.error("Couldn't find concept " + label + " in CM for relationship " + currentLinkingWord);
					} else
						concepts.add(concept);
				}
			}
			else if(name.equals("relationship")) {
				inRelationship = true;
				String label = attributes.getValue("label");
				label = label.replaceAll("\"", "");
				currentLinkingWord = label;
				if(attributes.getValue("posx") != null && attributes.getValue("posy") != null ) {
					currentPosX = Integer.parseInt(attributes.getValue("posx"));
					currentPosY = Integer.parseInt(attributes.getValue("posy"));
				}
				concepts = new ArrayList<Concept>();
			}
		}
		@Override
		public void endElement(String uri, String localName, String name)
		throws SAXException {
			super.endElement(uri, localName, name);
			if(name.equals("relationship")) {
				if(concepts.size() == 2 && currentLinkingWord != null) {
					try {
						Relationship<Concept> relationship = null;
						if(concepts.get(0).getPositionY() < concepts.get(1).getPositionY())
							relationship = new Relationship<Concept>(
									concepts.get(0),
									currentLinkingWord,
									concepts.get(1));
						else
							relationship = new Relationship<Concept>(
									concepts.get(1),
									currentLinkingWord,
									concepts.get(0));
						relationship.setPositionX(currentPosX);
						relationship.setPositionY(currentPosY);
						logger.debug("Adding new relationship " + relationship);
						map.addEdge(relationship.getSource(), relationship.getTarget(), relationship);
					} catch (Exception e) {
						logger.error(e);
					}
				}
				inRelationship = false;
				currentLinkingWord = null;
				concepts = new ArrayList<Concept>();
			}
		}
	}

	public List<Concept> getConceptFromTerm(String term) {
		List<Concept> concepts = new ArrayList<Concept>();
		for(Concept concept : this.vertexSet()) {
			if(concept.getTerm().equals(term))
				concepts.add(concept);
		}
		return concepts;
	}

	public ConceptMap merge(ConceptMap map) throws Exception {
		ConceptMap newMap = new ConceptMap();
		for(Concept concept : this.vertexSet()) {
			newMap.addVertex(concept);
		}
		for(Relationship<Concept> relationship : this.edgeSet()) {
			newMap.addEdge(relationship.getSource(), relationship.getTarget(), relationship);
		}
		for(Concept concept : map.vertexSet()) {
			newMap.addVertex(concept);
		}
		for(Relationship<Concept> relationship : map.edgeSet()) {
			newMap.addEdge(relationship.getSource(), relationship.getTarget(), relationship);
		}
		return newMap;
	}

	public ArrayList<ConceptMap> calculateSubmaps() throws Exception {
		
		ArrayList<ConceptMap> submaps = new ArrayList<ConceptMap>();
		
		ConnectivityInspector<Concept, Relationship<Concept>> inspector = new ConnectivityInspector<Concept, Relationship<Concept>>(new AsUndirectedGraph<Concept, Relationship<Concept>>(this));
		
		int smap = 0;
		for(Set<Concept> set : inspector.connectedSets()) {
			smap++;
			logger.debug("Submap " + smap);
			int max = 0;
			Concept maxConcept = null;
			for(Concept c : set) {
				logger.debug(c);
				int degree = this.inDegreeOf(c) + this.outDegreeOf(c); 
				if(degree > max) {
					max = degree;
					maxConcept = c;
				}
			}
			submaps.add(mapFromConcepts(set));
			logger.debug("Submap center: " + maxConcept);
		}
		return submaps;
	}
	
	private ConceptMap mapFromConcepts(Set<Concept> concepts) throws Exception {
		ConceptMap output = new ConceptMap();
		for(Concept c : concepts) {
			output.addVertex(new Concept(c.getTerm()));
		}
		for(Concept c : concepts) {
			for(Relationship<Concept> rel : this.outgoingEdgesOf(c)) {
				Concept ca = output.getConceptFromTerm(rel.getSource().getTerm()).get(0);
				Concept cb = output.getConceptFromTerm(rel.getTarget().getTerm()).get(0);
				Relationship<Concept> newRel = new Relationship<Concept>(ca, rel.getLinkingWord(), cb);
				if(!output.containsEdge(newRel))
					output.addEdge(ca, cb, newRel);
			}
			for(Relationship<Concept> rel : this.incomingEdgesOf(c)) {
				Concept ca = output.getConceptFromTerm(rel.getSource().getTerm()).get(0);
				Concept cb = output.getConceptFromTerm(rel.getTarget().getTerm()).get(0);
				Relationship<Concept> newRel = new Relationship<Concept>(ca, rel.getLinkingWord(), cb);
				if(!output.containsEdge(newRel))
					output.addEdge(ca, cb, newRel);
			}
		}
		return output;
	}
	
	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return this.text;
	}
}
