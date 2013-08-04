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
package tml.test;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import tml.Configuration;
import tml.annotators.PennTreeAnnotator;
import tml.conceptmap.Concept;
import tml.conceptmap.Relationship;
import tml.corpus.Corpus;
import tml.corpus.SearchResultsCorpus;
import tml.corpus.CorpusParameters.TermSelection;
import tml.vectorspace.operations.CmmProcess;
import tml.vectorspace.operations.TypedDependencies;
import tml.vectorspace.operations.ce.TypedDependenciesConceptExtraction;
import tml.vectorspace.operations.re.TypedDependenciesRelationshipExtraction;
import tml.vectorspace.operations.summarization.LatentSemanticAnalysisSummarization;

/**
 * This test verifies that all steps in CMM are working correctly.
 * Firstly, it indexes a document from the Uppsala corpus, then parses it
 * with the Stanford parser and checks the validity of the PennString
 * previously obtained with Stanford's GrammarScope tool.
 * Secondly, it extracts the Terminological Map from the first sentence by
 * processing the typed dependencies.
 * Thirdly, it runs the relationship extraction operation based on the 
 * concepts extracted before.
 * Fourthly, it runs the LSA based summarization that ranks the concepts.
 * 
 * Finally, it runs the CmmProcess operation, that runs all three operations
 * as a single step.
 * 
 * @author Jorge
 *
 */
public class CMMTest extends AbstractTmlIndexingTest {

	private static final String sentence3pennstring = "(ROOT \r\n" +
	"(S \r\n" +
	"(NP (PRP I)) \r\n" +
	"(VP (VBP am) \r\n" +
	"(VP (VBG going) \r\n" +
	"(S \r\n" +
	"(VP (TO to) \r\n" +
	"(VP (VB assess) \r\n" +
	"(NP (PRP$ my) (NNS strengths) \r\n" +
	"(CC and) \r\n" +
	"(NNS weaknesses)) \r\n" +
	"(PP (IN in) \r\n" +
	"(NP \r\n" +
	"(NP (DT the) (CD four) (NNS skills)) \r\n" +
	"(PP (IN of) \r\n" +
	"(NP (NN listening) (, ,) (NN reading) (, ,) (NN speaking) \r\n" +
	"(CC and) \r\n" +
	"(NN writing)))))))))) \r\n" +
	"(. .)))";

	private TypedDependencies typedDependenciesOperation;

	private static Corpus corpus;

	private TypedDependenciesConceptExtraction conceptsOp;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		AbstractTmlIndexingTest.setUpBeforeClass();
		repository.addDocumentsInFolder(Configuration.getTmlFolder() + "/corpora/uppsala",1);
		
		corpus = new SearchResultsCorpus("externalid:s3d0100.a1");
		corpus.getParameters().setTermSelectionCriterion(TermSelection.TF);
		corpus.getParameters().setTermSelectionThreshold(0);
		corpus.load(repository);
	}

	@Test
	public void PennTreeAnnotation() throws Exception {

		repository.addAnnotator(new PennTreeAnnotator());
		Thread th = repository.annotateDocuments();
		th.run();
		th.join();

		assertNotNull(th);
		
		assertNotNull(repository.getAnnotations("s1d0100.a1", "penntree"));

		String[] lines = repository.getAnnotations("s3d0100.a1", "penntree").split("\r\n");
		String[] expectedLines = sentence3pennstring.split("\r\n");

		assertEquals(lines.length, expectedLines.length);
		
		for(int i=0;i<Math.min(lines.length, expectedLines.length);i++) {
			assertEquals(expectedLines[i].trim(), lines[i].trim());
		}
	}
	
	@Test
	public void extractTypedDependencies() throws Exception {
		typedDependenciesOperation = new TypedDependencies();
		typedDependenciesOperation.setCorpus(corpus);
		typedDependenciesOperation.start();
		
		typedDependenciesOperation.printResults();
	}
	
	@Test
	public void ConceptMapExtraction() throws Exception {
		conceptsOp = new TypedDependenciesConceptExtraction();
		conceptsOp.setCorpus(corpus);
		conceptsOp.start();
		
		assertEquals(7, conceptsOp.getResults().size());

		TypedDependenciesRelationshipExtraction re = new TypedDependenciesRelationshipExtraction();
		re.setCorpus(corpus);
		re.setConceptsOperation(conceptsOp);
		re.start();
		
		assertEquals(1, re.getResults().size());

		LatentSemanticAnalysisSummarization summarization = new LatentSemanticAnalysisSummarization();
		summarization.setCorpus(corpus);
		summarization.start();
		
		List<Relationship<Concept>> relationships = re.getResults();
		for(Relationship<Concept> rel : relationships) {
			System.out.println(rel);
		}
		
		CmmProcess cmm = new CmmProcess(corpus, new TypedDependenciesConceptExtraction(), new TypedDependenciesRelationshipExtraction(), new LatentSemanticAnalysisSummarization());
		cmm.start();
	}
}
