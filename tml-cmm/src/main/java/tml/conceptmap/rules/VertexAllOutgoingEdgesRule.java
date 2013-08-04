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


public class VertexAllOutgoingEdgesRule extends VertexOutgoingEdgesRule {

	@Override
	protected String[] getRuleTargets() throws Exception {
		this.validateParameters();

		logger.trace("Pivoting on " + vertex);

		List<TerminologicalConcept> sources = new ArrayList<TerminologicalConcept>();
		List<TerminologicalConcept> targets = new ArrayList<TerminologicalConcept>();
		List<String> links = new ArrayList<String>();
		List<String> patternsMatched = new ArrayList<String>();

		for(TerminologicalRelationship<TerminologicalConcept> outgoingRelationship : this.conceptMap.outgoingEdgesOf(vertex)) {
			for(String[] relationshipPatterns : this.patterns) {
				String outgoingPattern = relationshipPatterns[0];
				String secondOutgoingPattern = relationshipPatterns[1];

				if(outgoingRelationship.getLinkingWord().matches(outgoingPattern)) {
					boolean allMatch = true;
					for(TerminologicalRelationship<TerminologicalConcept> secondOutgoingRelationship : this.conceptMap.outgoingEdgesOf(vertex)) {
						if(secondOutgoingRelationship == outgoingRelationship)
							continue;
						if(!secondOutgoingRelationship.getLinkingWord().matches(secondOutgoingPattern)) {
							allMatch = false;
						}
					}

					if(allMatch) {
						for(TerminologicalRelationship<TerminologicalConcept> secondOutgoingRelationship : this.conceptMap.outgoingEdgesOf(vertex)) {
							if(secondOutgoingRelationship.equals(outgoingRelationship))
								continue;
							sources.add(outgoingRelationship.getSource());
							if(secondOutgoingPattern.matches(infix)) {
								links.add(getInfixFromString(secondOutgoingPattern));								
							} else
								links.add(null);
							targets.add(secondOutgoingRelationship.getTarget());
							patternsMatched.add(outgoingPattern + "/" + secondOutgoingPattern);							
						}						
						sources.add(outgoingRelationship.getSource());
						if(outgoingPattern.matches(infix)) {
							links.add(getInfixFromString(outgoingPattern));								
						} else 
							links.add(null);
						targets.add(outgoingRelationship.getTarget());
						patternsMatched.add(outgoingPattern + "/" + secondOutgoingPattern);							
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
}
