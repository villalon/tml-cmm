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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import tml.Configuration;
import tml.conceptmap.Concept;
import tml.conceptmap.ConceptMap;
import tml.conceptmap.Relationship;
import tml.utils.StanfordUtils;

import edu.stanford.nlp.trees.Tree;


public class HumanMapsAnalysis {

	private static final String DOCUMENT_MATCH = "Diagnostic.*";
	private static final String JUDGE_MATCH = "(stephen)";
	
	private static Logger logger = Logger.getLogger(CMMexp.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Properties prop = Configuration.getTmlProperties(true);
//		Repository repo = new Repository(prop.getProperty("tml.tests.resourcespath") + "lucene/DiagnosticPSD");

		List<File> directories = new ArrayList<File>();

		directories.add(new File(prop.getProperty("tml.tests.resourcespath") + "annotations/"));

		List<String> concepts = new ArrayList<String>();
		List<String> linkingWords = new ArrayList<String>();
		
		for(File dir : directories) {
			if(!dir.exists())
				continue;
			
			for(File f1 : dir.listFiles()) {
				if(f1.isDirectory())
					continue;
				try {
					String judge = f1.getName().split("-")[0];
					String essay = f1.getName().split("-")[1].split("\\.")[0];
					if(!judge.matches(JUDGE_MATCH))
						continue;
					if(!essay.matches(DOCUMENT_MATCH))
						continue;
					//logger.debug("Reading concept map from " + f1.getName());
					ConceptMap map = ConceptMap.getFromXML(f1.getAbsolutePath());
					for(Concept concept: map.vertexSet()) {
						if(!concepts.contains(concept.getTerm()))
							concepts.add(concept.getTerm());
					}
					for(Relationship<Concept> relationship: map.edgeSet()) {						
						if(!linkingWords.contains(relationship.getLinkingWord()))
							linkingWords.add(relationship.getLinkingWord());
					}
				} catch (Exception e) {
					logger.error(f1.getName());
					logger.error(e);
					continue;
				}
			}
		}
		
		for(String concept : concepts) {
			Tree tree = StanfordUtils.getPennTree(concept);
			String tag = StanfordUtils.getPennTagMinimalPhrase(tree);
			int nouns = StanfordUtils.extractNouns(tree).size();
			if(tag.equals("LEAF")) {
				tag = StanfordUtils.getPennTagFirstBranch(tree, tree, null);
//				logger.debug(StanfordUtils.getPennString(tree));
			}
			logger.debug("#concept#" + concept + "#" + tag + "#" + nouns);
		}
		
		for(String linkingWord : linkingWords) {
			Tree tree = StanfordUtils.getPennTree(linkingWord);
			String tag = StanfordUtils.getPennTagMinimalPhrase(tree);
			int verbs = StanfordUtils.extractVerbs(tree).size();
			if(tag.equals("LEAF")) {
				tag = StanfordUtils.getPennTagFirstBranch(tree, tree, null);
//				logger.debug(StanfordUtils.getPennString(tree));
			}
			logger.debug("#linking word#" + linkingWord + "#" + tag + "#" + verbs);
		}
	}
}
