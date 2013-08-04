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
import tml.conceptmap.ConceptMap;
import tml.conceptmap.utils.CMMExperiment;
import tml.conceptmap.utils.ConceptComparator;
import tml.storage.Repository;


public class CMMexp {

	private static final String DOCUMENT_MATCH = "Diagnostic.*";
//	private static final String DOCUMENT_MATCH = "Diagnostic(01|03|04|05|06|07|08|09).*";
//	private static final String JUDGE_MATCH = "(stephen|villalon)";
//	private static final String JUDGE_MATCH = "(stephen|villalon|TypeDepCETypeDepRELSA.*)";
//	private static final String JUDGE_MATCH = "(stephen|villalon|CompoundNouns|TypedDep|TypeDepCETypeDepREVectLength)";
	private static final String JUDGE_MATCH = "wegmarker.*";
	
	private static Logger logger = Logger.getLogger(CMMexp.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Properties prop = Configuration.getTmlProperties(true);
		Repository repo = new Repository(prop.getProperty("tml.tests.resourcespath") + "lucene/DiagnosticPSD");

		List<File> directories = new ArrayList<File>();

		directories.add(new File(prop.getProperty("tml.tests.resourcespath") + "/annotations/"));
		directories.add(new File("target/annotations/"));

		CMMExperiment experiment = new CMMExperiment();
		experiment.setIncludeRelationships(true);
		experiment.setTotalUnitsAreNouns(true);
		experiment.setDocumentMatch(DOCUMENT_MATCH);
		experiment.setJudgeMatch(JUDGE_MATCH);

//		experiment.addComparator(new ConceptComparator(true, true, true));
		// No WordNet
		experiment.addComparator(new ConceptComparator(false, true, true));
//		experiment.addComparator(new ConceptComparator(true, false, true));
//		experiment.addComparator(new ConceptComparator(false, true, false));
//		experiment.addComparator(new ConceptComparator(false, true, true));
//		experiment.addComparator(new ConceptComparator(false, false, false));

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
					experiment.addEssay(essay);
					experiment.addJudge(judge);
					logger.debug("Reading concept map from " + f1.getName());
					ConceptMap map = ConceptMap.getFromXML(f1.getAbsolutePath());
					experiment.addObservation(judge, essay, map);
				} catch (Exception e) {
					logger.error(f1.getName());
					continue;
				}
			}
		}

		experiment.process(repo);
		experiment.printSummaryLong(30);
	}
}
