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
/**
 * 
 */
package tml.vectorspace.operations.ce;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tml.annotators.PennTreeAnnotator;
import tml.conceptmap.Concept;
import tml.utils.RegexUtils;
import tml.utils.StanfordUtils;

import edu.stanford.nlp.trees.Tree;

/**
 * @author Jorge Villalon
 *
 */
public class CompoundNouns extends AbstractConceptExtraction {

	public CompoundNouns() {
		this.name = "CompCE";
	}
	@Override
	public void start() throws Exception {
		super.start();
		
		List<String> allNouns = new ArrayList<String>();
		int total = 0;
		for(String sentenceId : this.corpus.getPassages()) {
			total++;
			Tree tree = null;
			String pennTreeString = null;
			try {
				pennTreeString = this.repository.getDocumentField(sentenceId, PennTreeAnnotator.FIELD_NAME);
			} catch (IOException e) {
				e.printStackTrace();
				logger.error(e);
			} 
			if(pennTreeString==null) {
				logger.error("Corpus does not contain required annotations from PennTreeAnnotator");
				return;
			}
			tree = StanfordUtils.getTreeFromString(sentenceId, pennTreeString);
			
			List<String> nouns = StanfordUtils.extractNouns(tree);
			if(nouns != null) {
				for(String noun : nouns) {
					if(!RegexUtils.stringIsContainedInList(allNouns, noun))
						allNouns.add(noun);
				}
			}

			if(this.maxResults > 0 && total >= this.maxResults)
				break;
		}

		for(String noun : allNouns) {
			Concept concept = new Concept(noun);
			this.results.add(concept);
		}
		super.end();
	}
}
