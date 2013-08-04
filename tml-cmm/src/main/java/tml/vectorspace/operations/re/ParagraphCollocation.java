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

import java.util.List;

import tml.conceptmap.Concept;
import tml.conceptmap.Relationship;
import tml.conceptmap.utils.RelationshipList;
import tml.corpus.SearchResultsCorpus;
import tml.utils.RegexUtils;


public class ParagraphCollocation extends AbstractRelationshipExtraction {

	public ParagraphCollocation() {
		this.name = "ParColl";
	}
	
	@Override
	public void start() throws Exception {
		super.start();

		String sentenceId = this.corpus.getPassages()[0];
		String documentId = this.corpus.getRepository().getDocumentField(sentenceId, 
				this.corpus.getRepository().getLuceneParentDocumentField());
		
		SearchResultsCorpus newCorpus = new SearchResultsCorpus("type:paragraph AND reference:" + documentId);
		newCorpus.setParameters(this.corpus.getParameters());
		newCorpus.load(this.corpus.getRepository());

		RelationshipList potentialRelationships = new RelationshipList();
		List<Concept> concepts = this.conceptsOperation.getResults();

		int total = 0;
		for(String passageId : newCorpus.getPassages()) {
			String content = newCorpus.getRepository().getDocumentField(passageId, 
					newCorpus.getRepository().getLuceneContentField()).toLowerCase();

			for(int i=0; i<concepts.size(); i++) {
				for(int j=i+1; j < concepts.size(); j++) {
					Concept conceptA = concepts.get(i);
					Concept conceptB = concepts.get(j);

					if(!RegexUtils.stringContained(content, conceptA.getTerm())
							|| !RegexUtils.stringContained(content, conceptB.getTerm()))
						continue;

					Relationship<Concept> rel = new Relationship<Concept>(
							conceptA,
							"?",
							conceptB);

					if(!potentialRelationships.contains(rel))
						potentialRelationships.add(rel);
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
