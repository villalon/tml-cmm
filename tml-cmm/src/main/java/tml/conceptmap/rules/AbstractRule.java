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
package tml.conceptmap.rules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import tml.conceptmap.TerminologicalConcept;
import tml.conceptmap.TerminologicalConceptMap;
import tml.conceptmap.TerminologicalRelationship;


/**
 * Abstract class for rules that will process a Terminological Map
 * and create a {@link TerminologicalConceptMap}.
 * 
 * @author Jorge Villalon
 *
 */
public abstract class AbstractRule<E> {

	/**
	 * Compares two rules by its order
	 * @author Jorge Villalon
	 *
	 */
	@SuppressWarnings("rawtypes")
	public static class AbstractRuleComparator implements Comparator<AbstractRule> {
		public int compare(AbstractRule o1, AbstractRule o2) {
			return o1.order - o2.order;
		}
	}

	public enum RULE_ACTION {
		/** The two vertices will merge with label from to */
		MERGE_VERTICES,
		/** The two vertices will merge with label to from */
		MERGE_VERTICES_REVERSE,
		/** The two vertices will merge with label from to and adding the linking word as infix */
		MERGE_VERTICES_INFIX,
		/** The two vertices will merge with label to from and adding the linking word as infix */
		MERGE_VERTICES_INFIX_REVERSE,
		/** Merges source and all the targets into one vertex */
		MERGE_ALL_TARGETS,
		/** Merges source and all the targets into one vertex and adds the linking word as infix */
		MERGE_ALL_TARGETS_INFIX,
		/** The target vertex will disappear */
		SUBDUE_TARGET,
		/** The source vertex will disappear */
		SUBDUE_SOURCE,
		/** The rule creates a vertex */
		CREATE_VERTEX,
		/** The rule creates a relationship */
		CREATE_RELATIONSHIP,
		/** The rule creates a relationship swapping the content of the linking word and the target */
		CREATE_RELATIONSHIP_SWAP_LINK_TARGET,
		/** Clean the text */
		CLEAN_TEXT,
		/** It doesn't do anything, useful to test new rules */
		DO_NOTHING
	};

	protected List<E> patterns = new ArrayList<E>();
	protected static Logger logger = Logger.getLogger(AbstractRule.class);
	protected String name = null;
	protected RULE_ACTION action = null;
	protected int order = Integer.MAX_VALUE;
	protected TerminologicalConceptMap conceptMap = null;
	protected TerminologicalConceptMap newConceptMap = null;
	protected boolean incomingOnly = false;

	protected TerminologicalRelationship<TerminologicalConcept> relationship = null;
	protected List<TerminologicalConcept> vertexSource = null;
	protected List<TerminologicalConcept> vertexTarget = null;
	protected String[] linkingWord = null;

	protected String infix = null;

	/**
	 * @return the infix
	 */
	public String getInfix() {
		return infix;
	}

	/**
	 * @param infix the infix to set
	 */
	public void setInfix(String infix) {
		this.infix = infix;
	}

	public void addPattern(E pattern) {
		this.patterns.add(pattern);
	}

	private void cleanTargets() {
		this.conceptMap = null;
		this.newConceptMap = null;
		this.linkingWord = null;
		this.vertexSource = null;
		this.vertexTarget = null;
	}

	private void createRelationship() throws Exception {
		for(int i=0; i<this.vertexSource.size(); i++) {
			logger.debug(this.name + ":" + this.vertexSource.get(i) + " ----" + this.linkingWord[i] + "----> " + this.vertexTarget.get(i));

			if(!this.newConceptMap.containsVertex(this.vertexSource.get(i)))
				this.newConceptMap.addVertex(this.vertexSource.get(i));

			if(!this.newConceptMap.containsVertex(this.vertexTarget.get(i)))
				this.newConceptMap.addVertex(this.vertexTarget.get(i));

			TerminologicalRelationship<TerminologicalConcept> newRelationship = null;
			try {
			newRelationship =
				new TerminologicalRelationship<TerminologicalConcept>(
						this.vertexSource.get(i), 
						this.linkingWord[i], 
						this.vertexTarget.get(i));
			} catch(Exception e) {
				logger.error(this.getClass().getName());
				throw new Exception(e);
			}

			boolean alreadyInConceptMap = false;
			for(TerminologicalRelationship<TerminologicalConcept> rel : this.newConceptMap.edgeSet()) {
				if(rel.equals(newRelationship)) {
					alreadyInConceptMap = true;
				}
			}

			if(!alreadyInConceptMap)
				this.newConceptMap.addEdge(
						this.vertexSource.get(i), 
						this.vertexTarget.get(i), 
						newRelationship);
		}
	}

	private void createVertex() {
		for(int i=0; i<this.vertexSource.size(); i++) {
			logger.debug(this.name + ":" + this.vertexSource);

			this.newConceptMap.addVertex(this.vertexSource.get(i));
		}
	}

	protected void doNothing() {
		for(int i=0; i<this.vertexSource.size(); i++)
			logger.debug(this.name + ":" + this.vertexSource.get(i) + "---" + this.linkingWord[i] + "--->" + this.vertexTarget.get(i));
	}

	/**
	 * @return the action
	 */
	public RULE_ACTION getAction() {
		return action;
	}

	/**
	 * @return the conceptMap
	 */
	public TerminologicalConceptMap getConceptMap() {
		return conceptMap;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the newConceptMap
	 */
	public TerminologicalConceptMap getNewConceptMap() {
		return newConceptMap;
	}

	/**
	 * @return the order
	 */
	public int getOrder() {
		return order;
	}

	protected abstract String[] getRuleTargets() throws Exception;

	/**
	 * @return the incomingOnly
	 */
	public boolean isIncomingOnly() {
		return incomingOnly;
	};

	private boolean isValidRule() {
		if(this.conceptMap == null)
			return false;

		switch(this.action) {
		case MERGE_VERTICES:
		case MERGE_VERTICES_REVERSE:
		case MERGE_ALL_TARGETS:
		case SUBDUE_SOURCE:
		case SUBDUE_TARGET:
			if(this.vertexSource == null
					|| this.vertexTarget == null)
				return false;
			break;
		case MERGE_ALL_TARGETS_INFIX:
		case MERGE_VERTICES_INFIX:
		case MERGE_VERTICES_INFIX_REVERSE:
			if(this.vertexSource == null
					|| this.linkingWord == null
					|| this.vertexTarget == null)
				return false;
			break;
		case CREATE_VERTEX:
			if(this.newConceptMap == null
					|| this.vertexSource == null)
				return false;
		case CREATE_RELATIONSHIP:
		case CREATE_RELATIONSHIP_SWAP_LINK_TARGET:
			if(this.newConceptMap == null
					|| this.vertexSource == null
					|| this.vertexTarget == null
					|| this.linkingWord == null)
				return false;
			break;
		case DO_NOTHING:
		case CLEAN_TEXT:
			break;
		}
		return true;
	}

	private void mergeVertices(TerminologicalConcept[] newVertices) throws Exception {

		for(int i=0; i<this.vertexSource.size(); i++) {
			logger.debug(this.name + ":" + this.vertexSource.get(i) + " -> " + this.vertexTarget.get(i) + " : " + newVertices[i]);

			mergeTwoVertices(this.vertexSource.get(i), this.vertexTarget.get(i), newVertices[i]);
		}
	}

	private void mergeTwoVertices(TerminologicalConcept vertexSource, TerminologicalConcept vertexTarget, TerminologicalConcept newVertex) throws Exception {
		if(!this.conceptMap.containsVertex(newVertex))
			this.conceptMap.addVertex(newVertex);
		
		List<TerminologicalRelationship<TerminologicalConcept>> newRelationships = new ArrayList<TerminologicalRelationship<TerminologicalConcept>>();
		for(TerminologicalRelationship<TerminologicalConcept> rel : this.conceptMap.incomingEdgesOf(vertexSource)) {
			if(!rel.getSource().equals(vertexTarget))
			newRelationships.add(
					new TerminologicalRelationship<TerminologicalConcept>(rel.getSource(), rel.getLinkingWord(), newVertex));
		}
		for(TerminologicalRelationship<TerminologicalConcept> rel : this.conceptMap.incomingEdgesOf(vertexTarget)) {
			if(!rel.getSource().equals(vertexSource))
			newRelationships.add( 
					new TerminologicalRelationship<TerminologicalConcept>(rel.getSource(), rel.getLinkingWord(), newVertex));
		}
		for(TerminologicalRelationship<TerminologicalConcept> rel : this.conceptMap.outgoingEdgesOf(vertexSource)) {
			if(!rel.getTarget().equals(vertexTarget))
			newRelationships.add( 
					new TerminologicalRelationship<TerminologicalConcept>(newVertex, rel.getLinkingWord(), rel.getTarget()));
		}
		for(TerminologicalRelationship<TerminologicalConcept> rel : this.conceptMap.outgoingEdgesOf(vertexTarget)) {
			if(!rel.getTarget().equals(vertexSource))
			newRelationships.add( 
					new TerminologicalRelationship<TerminologicalConcept>(newVertex, rel.getLinkingWord(), rel.getTarget()));
		}

		for(TerminologicalRelationship<TerminologicalConcept> rel : newRelationships) {
			if(!rel.getSource().equals(vertexSource) && !rel.getSource().equals(vertexTarget)
					&& !rel.getTarget().equals(vertexSource) && !rel.getTarget().equals(vertexTarget)) {
				
				if(!this.conceptMap.containsEdge(rel)) {
					this.conceptMap.addEdge(rel.getSource(), rel.getTarget(), rel);
				}
				else
					logger.debug("Ignoring " + rel + " because it already exist");
			} else {
				logger.debug("Ignoring " + rel + " because it is a loop");
			}
		}
		
		this.conceptMap.removeVertex(vertexSource);
		this.conceptMap.removeVertex(vertexTarget);

		this.conceptMap.removeAllEdges(this.conceptMap.getAllEdges(newVertex, newVertex));
	}
	
	private void mergeAllTargets(String[] patterns, String infix) throws Exception {
		for(int i=0; i<this.vertexTarget.size(); i++) {

			if(this.conceptMap.inDegreeOf(this.vertexTarget.get(i)) <= 1)
				this.conceptMap.removeVertex(vertexTarget.get(i));
			else
				this.conceptMap.removeAllEdges(this.vertexSource.get(i), this.vertexTarget.get(i));
		}

		TerminologicalConcept newConcept = TerminologicalConcept.mergeManyConcepts(this.vertexTarget, this.linkingWord);

		this.conceptMap.addVertex(newConcept);

		logger.debug(this.name + ":" + this.vertexSource.get(0) + " -> " + newConcept);

		TerminologicalConcept finalConcept = TerminologicalConcept.mergeTwoConcepts(this.vertexSource.get(0), newConcept);
		mergeTwoVertices(this.vertexSource.get(0), newConcept, finalConcept);
	}

	/**
	 * Processes a rule in a concept map
	 * @throws Exception
	 */
	public boolean process() throws Exception {
		String[] pattern = this.getRuleTargets(); 
		if(pattern == null) {
			return false;
		}

		if(!this.isValidRule()) {
			logger.warn("Rule " + this.name + " is not valid!");
		}
		TerminologicalConcept[] newVertices = null;
		switch (this.action) {
		case CREATE_RELATIONSHIP:
			createRelationship();
			return false;
		case CREATE_RELATIONSHIP_SWAP_LINK_TARGET:
			for(int i=0;i<this.vertexSource.size();i++) {
				String temp = this.linkingWord[i];
				TerminologicalConcept tmpConcept = new TerminologicalConcept(temp);
				this.linkingWord[i] = this.vertexTarget.get(i).getTerm();
				this.vertexTarget.set(i, tmpConcept);
			}
			createRelationship();
			return false;
		case CREATE_VERTEX:
			createVertex();
			return false;
		case MERGE_VERTICES:
			newVertices = new TerminologicalConcept[this.vertexSource.size()];
			for(int i=0; i<this.vertexSource.size(); i++)
				newVertices[i] = TerminologicalConcept.mergeTwoConcepts(this.vertexTarget.get(i), this.vertexSource.get(i)); 
			mergeVertices(newVertices);
			break;
		case MERGE_VERTICES_REVERSE:
			newVertices = new TerminologicalConcept[this.vertexSource.size()];
			for(int i=0; i<this.vertexSource.size(); i++)
				newVertices[i] = TerminologicalConcept.mergeTwoConcepts(this.vertexSource.get(i),this.vertexTarget.get(i)); 
			mergeVertices(newVertices);
			break;
		case MERGE_VERTICES_INFIX:
			newVertices = new TerminologicalConcept[this.vertexSource.size()];
			for(int i=0; i<this.vertexSource.size(); i++)
				newVertices[i] = TerminologicalConcept.mergeTwoConcepts(this.vertexTarget.get(i) , this.vertexSource.get(i), this.linkingWord[i]); 
			mergeVertices(newVertices);
			break;
		case MERGE_VERTICES_INFIX_REVERSE:
			newVertices = new TerminologicalConcept[this.vertexSource.size()];
			for(int i=0; i<this.vertexSource.size(); i++)
				newVertices[i] = new TerminologicalConcept(this.vertexSource.get(i) + " " + this.linkingWord[i] + " " + this.vertexTarget.get(i)); 
			mergeVertices(newVertices);
			break;
		case MERGE_ALL_TARGETS:
			mergeAllTargets(pattern, null);
			break;
		case MERGE_ALL_TARGETS_INFIX:
			mergeAllTargets(pattern, this.infix);
			break;
		case SUBDUE_SOURCE:
			newVertices = new TerminologicalConcept[this.vertexSource.size()];
			for(int i=0; i<this.vertexSource.size(); i++)
				newVertices[i] = TerminologicalConcept.mergeTwoConcepts(
						this.vertexSource.get(i),
						this.vertexTarget.get(i),
						this.vertexTarget.get(i).getTerm(),
						this.vertexTarget.get(i).getStemmedTerm()); 
			mergeVertices(newVertices);
			break;
		case SUBDUE_TARGET:
			newVertices = new TerminologicalConcept[this.vertexSource.size()];
			for(int i=0; i<this.vertexSource.size(); i++)
				newVertices[i] = TerminologicalConcept.mergeTwoConcepts(
						this.vertexTarget.get(i),
						this.vertexSource.get(i),
						this.vertexSource.get(i).getTerm(),
						this.vertexSource.get(i).getStemmedTerm()); 
			mergeVertices(newVertices);
			break;
		case DO_NOTHING:
			doNothing();
			return false;
		default:
			break;
		}

		cleanTargets();

		return true;
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(RULE_ACTION action) {
		this.action = action;
	}

	public void setActionByName(String actionName) {
		for(RULE_ACTION ac : RULE_ACTION.values()) {
			if(ac.name().equals(actionName)) {
				this.action = RULE_ACTION.valueOf(actionName);
			}
		}
	}

	/**
	 * @param conceptMap the conceptMap to set
	 */
	public void setConceptMap(TerminologicalConceptMap conceptMap) {
		this.conceptMap = conceptMap;
	}

	/**
	 * @param incomingOnly the incomingOnly to set
	 */
	public void setIncomingOnly(boolean incomingOnly) {
		this.incomingOnly = incomingOnly;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param newConceptMap the newConceptMap to set
	 */
	public void setNewConceptMap(TerminologicalConceptMap newConceptMap) {
		this.newConceptMap = newConceptMap;
	}

	/**
	 * @param order the order to set
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	protected String simpleString() {
		StringBuffer buff = new StringBuffer();
		if(this.vertexSource != null)
			for(int i=0; i<this.vertexSource.size(); i++) {
				buff.append(this.vertexSource.get(i));
				if(this.linkingWord != null) {
					buff.append("-");
					buff.append(this.linkingWord[i]);
					if(this.vertexTarget != null) {
						buff.append("->");
						buff.append(this.vertexTarget.get(i));
					}
				}
			}
		return buff.toString();
	}

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append(this.name);
		buff.append("\t\t Action:");
		buff.append(this.action);
		buff.append(" Order:");
		buff.append(this.order);
		buff.append(" Patterns:");
		for(E e : patterns) {
			if(e instanceof String)
				buff.append(e);
			else if(e instanceof String[])
				for(String pattern : (String[]) e)
					buff.append(pattern + " ");
			buff.append(" ");
		}
		return buff.toString();
	}

	protected String getInfixFromString(String patternToMatch) {		
		Pattern pattern = Pattern.compile(infix);
		Matcher matcher = pattern.matcher(patternToMatch);
		if(matcher.matches())
			return matcher.group(1);
		else
			return null;
	}
}
