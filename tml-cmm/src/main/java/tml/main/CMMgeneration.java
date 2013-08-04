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

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import tml.Configuration;
import tml.corpus.TextDocument;
import tml.corpus.CorpusParameters.DimensionalityReduction;
import tml.storage.Repository;
import tml.vectorspace.TermWeighting.GlobalWeight;
import tml.vectorspace.TermWeighting.LocalWeight;
import tml.vectorspace.operations.CmmProcess;
import tml.vectorspace.operations.ce.ConceptExtractionOperation;
import tml.vectorspace.operations.ce.TypedDependenciesConceptExtraction;
import tml.vectorspace.operations.re.RelationshipExtractionOperation;
import tml.vectorspace.operations.re.TypedDependenciesRelationshipExtraction;
import tml.vectorspace.operations.summarization.LatentSemanticAnalysisSummarization;
import tml.vectorspace.operations.summarization.SummarizationOperation;

public class CMMgeneration {

	private static Logger logger = Logger.getLogger(CMMgeneration.class);

	private static ConceptExtractionOperation[] operationsCE = {
		//				new CompoundNouns(),
		new TypedDependenciesConceptExtraction(),
		//		new StopWordsDelimiters()
	};

	private static RelationshipExtractionOperation[] operationsRE = {
		//		new Tregex(),
		new TypedDependenciesRelationshipExtraction(),
		//				new SentenceCollocation(),
		//				new ParagraphCollocation()
	};

	private static SummarizationOperation[] operationsSummary = {
		new LatentSemanticAnalysisSummarization(),
		//new VectorLengthSummarization()
	};

	public static void main(String[] args) throws IOException {
		Properties prop = Configuration.getTmlProperties(true);
		Repository repository;
		List<TextDocument> docs = null;
		try {
			repository = new Repository(prop.getProperty("tml.lucene.indexpath"));
			docs = repository.getAllTextDocuments();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			return;
		}

		for(TextDocument document : docs) {
			if(!document.getExternalId().startsWith("Diagnostic"))
				continue;
			document.getParameters().setDimensionalityReduction(DimensionalityReduction.PCT);
			document.getParameters().setDimensionalityReductionThreshold(70);
			document.getParameters().setTermWeightLocal(LocalWeight.TF);
			document.getParameters().setTermWeightGlobal(GlobalWeight.None);
			document.getParameters().setTermSelectionThreshold(0);
			try {
				document.load(repository);
			} catch (Exception e1) {
				e1.printStackTrace();
				logger.error("Couldn't load document " + document.getExternalId());
				logger.error(e1);
				continue;
			}
			for(SummarizationOperation summOp : operationsSummary) {
				for(ConceptExtractionOperation opCE : operationsCE) {
					for(RelationshipExtractionOperation opRE : operationsRE) {


						ConceptExtractionOperation _opCE = null;
						SummarizationOperation _summOp = null;
						RelationshipExtractionOperation _opRE = null;
						try {
							_opCE = opCE.getClass().newInstance();
							_summOp = summOp.getClass().newInstance();
							_opRE = opRE.getClass().newInstance();
						} catch (Exception e) {
							e.printStackTrace();
							logger.error(e);
							return;
						}

						//_summOp.setPercentage(i);

						CmmProcess cmm = new CmmProcess(
								document.getSentenceCorpus(),
								_opCE,
								_opRE,
								_summOp);

						for(double i=1; i<=1; i+=0.1) {
							String filename = 
//								opCE.getName() + 
//								opRE.getName() + 
//								summOp.getName() + 
//								((int) (i * 100)) +
								"annotator3-" + document.getExternalId() + ".xml";

							try {
								cmm.start();
								cmm.getConceptMap().writeToXML("/opt/tml/cmex/" + filename);
							} catch (Exception e) {
								e.printStackTrace();
								logger.error(e);
							}
						}
					}
				}
			}
		}	
	}
}
