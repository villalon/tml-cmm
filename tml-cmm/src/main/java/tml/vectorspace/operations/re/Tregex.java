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
package tml.vectorspace.operations.re;

import java.util.ArrayList;
import java.util.List;

import tml.annotators.PennTreeAnnotator;
import tml.conceptmap.Concept;
import tml.conceptmap.Relationship;
import tml.utils.StanfordUtils;

import edu.stanford.nlp.trees.Tree;

public class Tregex extends AbstractRelationshipExtraction {

	@Override
	public void start() throws Exception {
		super.start();

		List<Relationship<Concept>> potentialRelationships = new ArrayList<Relationship<Concept>>();
		List<Concept> concepts = this.conceptsOperation.getResults();

		int total = 0;
		for(String passageId : this.corpus.getPassages()) {
			String pennString = this.corpus.getRepository().getDocumentField(passageId, 
					PennTreeAnnotator.FIELD_NAME);
			String content = this.corpus.getRepository().getDocumentField(passageId, 
					this.corpus.getRepository().getLuceneContentField()).toLowerCase();
			
			Tree tree = StanfordUtils.getTreeFromString(passageId, pennString);
			List<String> verbs = StanfordUtils.extractVerbs(tree);

			for(int i=0; i<concepts.size(); i++) {
				for(int j=i+1; j < concepts.size(); j++) {
					Concept conceptA = concepts.get(i);
					Concept conceptB = concepts.get(j);
					
					int indexA = content.indexOf(conceptA.getTerm());
					int indexB = content.indexOf(conceptB.getTerm());
					
					if(indexA < 0 || indexB < 0)
						continue;

					
					for(int v=0; v<verbs.size(); v++) {
						String verb = verbs.get(v);
						int indexVerb = content.indexOf(verb);
						if(indexVerb < 0)
							continue;
						
						int indexNextVerb = Integer.MAX_VALUE;
						int indexPrevVerb = Integer.MIN_VALUE;
						if(v<verbs.size()-1)
							indexNextVerb = content.indexOf(verbs.get(v+1));
						if(v>0)
							indexPrevVerb = content.indexOf(verbs.get(v-1));
						
						if(indexA < indexVerb &&
								indexB > indexVerb
								&& indexB < indexNextVerb
								&& indexA > indexPrevVerb) {
							potentialRelationships.add(new Relationship<Concept>(
									conceptA,
									verb,
									conceptB));
						} else if(indexA > indexVerb
								&& indexB < indexVerb
								&& indexA < indexNextVerb
								&& indexB > indexPrevVerb) {
							potentialRelationships.add(new Relationship<Concept>(
									conceptB,
									verb,
									conceptA));		
						}
					}
				}
			}
			total++;
			if(this.maxResults > 0 && total >= this.maxResults)
				break;
		}
		
		for(Relationship<Concept> rel : potentialRelationships) {
			this.results.add(rel);
		}
		super.end();
	}
}
