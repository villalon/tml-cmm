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

import java.util.Properties;

import org.apache.log4j.Logger;

import tml.Configuration;
import tml.conceptmap.ConceptMap;
import tml.conceptmap.TerminologicalConceptMap;
import tml.corpus.SearchResultsCorpus;
import tml.corpus.TextDocument;
import tml.corpus.CorpusParameters.TermSelection;
import tml.storage.Repository;
import tml.vectorspace.operations.CmmProcess;
import tml.vectorspace.operations.TerminologicalMap;
import tml.vectorspace.operations.ce.TypedDependenciesConceptExtraction;
import tml.vectorspace.operations.re.TypedDependenciesRelationshipExtraction;
import tml.vectorspace.operations.summarization.LatentSemanticAnalysisSummarization;


public class CMMgenerateDocumentSubMaps {

	private static Logger logger = Logger.getLogger(CMMgenerateDocumentSubMaps.class);
	private static Repository repository;
	private static String documentId = "Diagnostic03";

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Properties prop = Configuration.getTmlProperties(true);
		repository = new Repository(prop.getProperty("tml.tests.resourcespath") + "lucene/DiagnosticPSD");

		TextDocument document = repository.getTextDocument(documentId);
		document.load(repository);

		for(String sentenceId : document.getSentenceCorpus().getPassages()) {
			if(!sentenceId.startsWith("s7"))
				continue;
			logger.info("" + sentenceId);
			SearchResultsCorpus corpus = new SearchResultsCorpus("type:sentence AND externalid:" + sentenceId);
			corpus.getParameters().setTermSelectionCriterion(TermSelection.DF);
			corpus.getParameters().setTermSelectionThreshold(0);

			try {
				corpus.load(repository);
			} catch(Exception e) {
				logger.error("Couldn't process sentence " + sentenceId);
				continue;
			}

			TerminologicalMap tmap = new TerminologicalMap();
			tmap.setCorpus(corpus);
			tmap.start();
			
			for(TerminologicalConceptMap cmap : tmap.getResults()) {
				cmap.writeToXML("target/annotations/TerminologicalMap-" + sentenceId + ".xml");
			}
			
			TerminologicalMap tmap2 = new TerminologicalMap();
			tmap2.setCorpus(corpus);
			tmap2.setApplyRules(false);
			tmap2.start();
			
			for(TerminologicalConceptMap cmap : tmap2.getResults()) {
				cmap.writeToXML("target/annotations/TerminologicalMapRaw-" + sentenceId + ".xml");
			}
			
			CmmProcess cmm = new CmmProcess(
					corpus, 
					new TypedDependenciesConceptExtraction(), 
					new TypedDependenciesRelationshipExtraction(), 
					new LatentSemanticAnalysisSummarization());

			cmm.start();
			ConceptMap cmap = cmm.getConceptMap();
			cmap.writeToXML("target/annotations/" + sentenceId + ".xml");
		}
	}
}
