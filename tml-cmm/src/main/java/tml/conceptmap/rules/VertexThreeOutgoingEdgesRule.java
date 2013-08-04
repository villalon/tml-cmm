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
import tml.conceptmap.TerminologicalRelationship;


public class VertexThreeOutgoingEdgesRule extends AbstractVertexRule<String[]> {

	@Override
	protected String[] getRuleTargets() throws Exception {
		this.validateParameters();

		List<TerminologicalConcept> sources = new ArrayList<TerminologicalConcept>();
		List<TerminologicalConcept> targets = new ArrayList<TerminologicalConcept>();
		List<String> links = new ArrayList<String>();
		List<String> patternsMatched = new ArrayList<String>();

		for(TerminologicalRelationship<TerminologicalConcept> outgoingRelationship : this.conceptMap.outgoingEdgesOf(vertex)) {
			for(String[] relationshipPatterns : this.patterns) {
				String outgoingPattern = relationshipPatterns[0];
				String secondOutgoingPattern = relationshipPatterns[1];
				String thirdOutgoingPattern = relationshipPatterns[2];

				if(outgoingRelationship.getLinkingWord().matches(outgoingPattern)) {
					for(TerminologicalRelationship<TerminologicalConcept> secondOutgoingRelationship : this.conceptMap.outgoingEdgesOf(vertex)) {
						if(secondOutgoingRelationship.equals(outgoingRelationship))
							continue;
						if(secondOutgoingRelationship.getLinkingWord().matches(secondOutgoingPattern)) {
							for(TerminologicalRelationship<TerminologicalConcept> thirdOutgoingRelationship : this.conceptMap.outgoingEdgesOf(vertex)) {
								if(thirdOutgoingRelationship.equals(outgoingRelationship)
										|| thirdOutgoingRelationship.equals(secondOutgoingRelationship))
									continue;
								if(thirdOutgoingRelationship.getLinkingWord().matches(thirdOutgoingPattern)
										&& secondOutgoingRelationship.getTarget().getPositionInSentenceLeft() 
										< thirdOutgoingRelationship.getTarget().getPositionInSentenceLeft()) {
									sources.add(outgoingRelationship.getTarget());
									links.add(vertex.getTerm());
									targets.add(secondOutgoingRelationship.getTarget());
									sources.add(secondOutgoingRelationship.getTarget());
									links.add(getInfixFromString(thirdOutgoingRelationship.getLinkingWord()));
									targets.add(thirdOutgoingRelationship.getTarget());
									patternsMatched.add(outgoingPattern + "/" + secondOutgoingPattern + "/" + thirdOutgoingPattern);
								}
							}
						}
					}
				}
			}
		}

		if(sources.size() == 0)
			return null;

		this.vertexSource = sources;
		this.vertexTarget = targets;
		this.linkingWord = new String[links.size()];
		this.linkingWord = links.toArray(this.linkingWord);

		return patternsMatched.toArray(new String[patternsMatched.size()]);
	}

	@Override
	public void addPatternFromString(String pattern) {
		String[] patterns = pattern.split(",");
		if(patterns.length != 3)
			return;
		this.patterns.add(patterns);
	}
}
