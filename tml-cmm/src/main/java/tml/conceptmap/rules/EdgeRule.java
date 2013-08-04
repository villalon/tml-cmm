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
import java.util.List;

import tml.conceptmap.TerminologicalConcept;


public class EdgeRule extends AbstractEdgeRule {

	@Override
	protected String[] getRuleTargets() throws Exception {
		if(relationship == null)
			throw new Exception("Relationship to process is null!");

		if(this.singleEdge &&
				(this.conceptMap.outDegreeOf(relationship.getSource()) != 1
						|| this.conceptMap.inDegreeOf(relationship.getTarget()) != 1))
			return null;

		if(this.positionDifference > 0) {
			if(Math.abs(relationship.getSource().getPositionInSentenceRight()
					- relationship.getTarget().getPositionInSentenceLeft()) 
					== this.positionDifference
					|| Math.abs(relationship.getSource().getPositionInSentenceLeft()
							- relationship.getTarget().getPositionInSentenceRight()) 
							== this.positionDifference) {
				
			} else
					return null;
		}
		
		if(this.leftPOS != null && 
				!this.leftPOS.matches(relationship.getSource().getPartOfSpeech())) {
			return null;
		}

		if(this.rightPOS != null && 
				!this.rightPOS.matches(relationship.getTarget().getPartOfSpeech())) {
			return null;
		}

		List<TerminologicalConcept> sources = new ArrayList<TerminologicalConcept>();
		List<TerminologicalConcept> targets = new ArrayList<TerminologicalConcept>();
		List<String> links = new ArrayList<String>();
		List<String> patternsMatched = new ArrayList<String>();

		for(String pattern : this.patterns) {
			if(!relationship.getLinkingWord().matches(pattern))
				continue;

			sources.add(relationship.getSource());
			if(this.infix != null)
				links.add(getInfixFromString(pattern));
			else
				links.add(null);
			targets.add(relationship.getTarget());
			patternsMatched.add(pattern);
			break;
		}

		if(sources.size() == 0)
			return null;

		this.vertexSource = sources;
		this.vertexTarget = targets;
		this.linkingWord = new String[links.size()];
		this.linkingWord = links.toArray(this.linkingWord);

		return patternsMatched.toArray(new String[patternsMatched.size()]);
	}

	/**
	 * Adds a pattern from a string
	 * @param pattern
	 */
	@Override
	public void addPatternFromString(String pattern) {
		this.addPattern(pattern);
	}
}
