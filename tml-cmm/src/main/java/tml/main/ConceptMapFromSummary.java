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
import tml.corpus.SearchResultsCorpus;
import tml.corpus.TextDocument;
import tml.corpus.CorpusParameters.DimensionalityReduction;
import tml.corpus.CorpusParameters.TermSelection;
import tml.storage.Repository;
import tml.vectorspace.TermWeighting.GlobalWeight;
import tml.vectorspace.TermWeighting.LocalWeight;
import tml.vectorspace.operations.TerminologicalMap;
import tml.vectorspace.operations.ce.CompoundNouns;
import tml.vectorspace.operations.ce.TypedDependenciesConceptExtraction;
import tml.vectorspace.operations.re.TypedDependenciesRelationshipExtraction;
import tml.vectorspace.operations.summarization.LatentSemanticAnalysisSummarization;


public class ConceptMapFromSummary {

	private static Logger logger = Logger.getLogger(ConceptMapFromSummary.class);
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Properties prop = Configuration.getTmlProperties(true);
                String resourcesPath = prop.getProperty("tml.tests.resourcespath");
		Repository repository = new Repository( resourcesPath + "lucene/DiagnosticPSD");
		TextDocument document = repository.getTextDocument("Diagnostic01");
		document.getParameters().setDimensionalityReduction(DimensionalityReduction.PCT);
		document.getParameters().setDimensionalityReductionThreshold(70);
		document.getParameters().setTermSelectionCriterion(TermSelection.DF);
		document.getParameters().setTermSelectionThreshold(0);
		document.getParameters().setTermWeightLocal(LocalWeight.TF);
		document.getParameters().setTermWeightGlobal(GlobalWeight.None);
		document.load(repository);

		logger.debug("Starting summarization step");
		
		LatentSemanticAnalysisSummarization op = new LatentSemanticAnalysisSummarization();
		op.setCorpus(document.getSentenceCorpus());
		op.start();

		int sentenceNumber = 1;
		
		StringBuffer buff = new StringBuffer();
		for(int i=sentenceNumber-1; i<sentenceNumber;i++) {
//			String passageId = op.getResults().get(0).getPassages()[i];
//			buff.append("externalid:");
//			buff.append(passageId);
//			if(i<op.getResults().get(0).getPassages().length-1)
//				buff.append(" OR ");
		}

		SearchResultsCorpus newCorpus = new SearchResultsCorpus(buff.toString());
		newCorpus.setParameters(document.getParameters());
		newCorpus.load(repository);

		CompoundNouns nounsOp = new CompoundNouns();
		nounsOp.setCorpus(newCorpus);
		nounsOp.start();

		nounsOp.printResults();

		TerminologicalMap operation = new TerminologicalMap();
		operation.setCorpus(newCorpus);
		operation.start();
		
		TypedDependenciesConceptExtraction ceop = new TypedDependenciesConceptExtraction();
		ceop.setCorpus(newCorpus);
		ceop.setTerminologicalMaps(operation.getResults());
		ceop.start();

		TypedDependenciesRelationshipExtraction reop = new TypedDependenciesRelationshipExtraction();
		reop.setConceptsOperation(nounsOp);
		reop.setCorpus(newCorpus);
		reop.setTerminologicalMaps(operation.getResults());
		reop.start();
	}
}

