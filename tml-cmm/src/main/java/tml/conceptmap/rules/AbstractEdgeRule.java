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

import tml.conceptmap.TerminologicalConcept;
import tml.conceptmap.TerminologicalRelationship;

public abstract class AbstractEdgeRule extends AbstractRule<String> {

	protected TerminologicalRelationship<TerminologicalConcept> relationship;
	protected boolean singleEdge = false;
	protected int positionDifference = 0;
	protected String leftPOS = null;
	/**
	 * @return the leftPOS
	 */
	public String getLeftPOS() {
		return leftPOS;
	}

	/**
	 * @param leftPOS the leftPOS to set
	 */
	public void setLeftPOS(String leftPOS) {
		this.leftPOS = leftPOS;
	}

	/**
	 * @return the rightPOS
	 */
	public String getRightPOS() {
		return rightPOS;
	}

	/**
	 * @param rightPOS the rightPOS to set
	 */
	public void setRightPOS(String rightPOS) {
		this.rightPOS = rightPOS;
	}

	protected String rightPOS = null;
	
	/**
	 * @return the positionDifference
	 */
	public int getPositionDifference() {
		return positionDifference;
	}

	/**
	 * @param positionDifference the positionDifference to set
	 */
	public void setPositionDifference(int positionDifference) {
		this.positionDifference = positionDifference;
	}

	/**
	 * @return the singleEdge
	 */
	public boolean isSingleEdge() {
		return singleEdge;
	}

	/**
	 * @param singleEdge the singleEdge to set
	 */
	public void setSingleEdge(boolean singleEdge) {
		this.singleEdge = singleEdge;
	}

	/**
	 * @return the relationship
	 */
	public TerminologicalRelationship<TerminologicalConcept> getRelationship() {
		return relationship;
	}

	/**
	 * @param relationship the relationship to set
	 */
	public void setRelationship(TerminologicalRelationship<TerminologicalConcept> relationship) {
		this.relationship = relationship;
	}

	/**
	 * Adds a pattern from a string
	 * @param pattern
	 */
	public void addPatternFromString(String pattern) {
		this.patterns.add(pattern);
	}
	
	@Override
	public String toString() {
		String base = super.toString(); 
		return base + " SingleEdge:" + isSingleEdge() + " leftPOS:" + leftPOS + " rightPOS:" + rightPOS + " posDiff:" + positionDifference;
	}
}
