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
package tml.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import tml.Configuration;
import tml.annotators.PennTreeAnnotator;
import tml.corpus.SearchResultsCorpus;
import tml.storage.Repository;
import tml.utils.StanfordUtils;

import edu.stanford.nlp.trees.Tree;

public class GenerateTypedDependencies {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Properties prop = Configuration.getTmlProperties(true);
		Repository repository = new Repository(prop.getProperty("tml.tests.resourcespath") + "/lucene/DiagnosticPSD");
		SearchResultsCorpus corpus = new SearchResultsCorpus("type:sentence AND parent:PSD19");

		corpus.load(repository);

		List<String> allDependencies = new ArrayList<String>();
		String lastParent = null;
		for(String passageId : corpus.getPassages()) {

			String pennstring = repository.getDocumentField(passageId, PennTreeAnnotator.FIELD_NAME);
			String parent = repository.getDocumentField(passageId, repository.getLuceneParentField());
			if(!parent.equals(lastParent)) {
				Tree tree = StanfordUtils.getTreeFromString(passageId, pennstring);
				List<String> deps = StanfordUtils.calculateTypedDependencies(tree);
				if(deps != null) {
					allDependencies.add(parent);
					allDependencies.addAll(deps);
				}
				lastParent = parent;
			}
		}

		for(String dep : allDependencies) {
			System.out.println(dep);
		}
	}

}
