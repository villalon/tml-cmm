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
package tml.vectorspace.operations;

import tml.conceptmap.Concept;
import tml.conceptmap.ConceptMap;
import tml.conceptmap.Relationship;
import tml.corpus.Corpus;
import tml.vectorspace.operations.AbstractOperation;
import tml.vectorspace.operations.OperationEvent;
import tml.vectorspace.operations.OperationListener;
import tml.vectorspace.operations.ce.ConceptExtractionOperation;
import tml.vectorspace.operations.re.RelationshipExtractionOperation;
import tml.vectorspace.operations.results.Summary;
import tml.vectorspace.operations.summarization.SummarizationOperation;

/**
 * 
 * @author Jorge Villalon
 *
 */
public class CmmProcess extends AbstractOperation<ConceptMap> {

//	private Logger logger = Logger.getLogger(CmmProcess.class);
    private ConceptMap conceptMap;

    /**
     * @return the ceOperation
     */
    public ConceptExtractionOperation getCeOperation() {
        return ceOperation;
    }

    /**
     * @param ceOperation the ceOperation to set
     */
    public void setCeOperation(ConceptExtractionOperation ceOperation) {
        this.ceOperation = ceOperation;
        this.ceOperation.addOperationListener(new OperationListener() {

            public void operationAction(OperationEvent evt) {
                operationPerformed(evt);
            }
        });
    }

    /**
     * @return the reOperation
     */
    public RelationshipExtractionOperation getReOperation() {
        return reOperation;
    }

    /**
     * @param reOperation the reOperation to set
     */
    public void setReOperation(RelationshipExtractionOperation reOperation) {
        this.reOperation = reOperation;
        this.reOperation.addOperationListener(new OperationListener() {

            public void operationAction(OperationEvent evt) {
                operationPerformed(evt);
            }
        });
    }

    /**
     * @return the conceptMap
     */
    public ConceptMap getConceptMap() {
        return conceptMap;
    }
    private ConceptExtractionOperation ceOperation;
    private RelationshipExtractionOperation reOperation;
    private SummarizationOperation summOperation;

    public SummarizationOperation getSummOperation() {
        return summOperation;
    }

    public void setSummOperation(SummarizationOperation summOperation) {
        this.summOperation = summOperation;
        this.summOperation.addOperationListener(new OperationListener() {

            public void operationAction(OperationEvent evt) {
                operationPerformed(evt);
            }
        });
    }

    public CmmProcess(Corpus corpus, ConceptExtractionOperation ceop, RelationshipExtractionOperation reop, SummarizationOperation summOp) {
        this.setCeOperation(ceop);
        this.setReOperation(reop);
        this.setSummOperation(summOp);
        this.corpus = corpus;
    }

    @Override
    public void start() throws Exception {
        super.start();
        extractConceptMap();
    }

    private void extractConceptMap() throws Exception {
        if (!parametersAreValid()) {
            String message = "Invalid parameters for CMM";
            logger.error(message);
            throw new Exception(message);
        }

        // Concept Extraction
        this.ceOperation.setCorpus(this.corpus);
        try {
            this.ceOperation.start();
        } catch (Exception e) {
            String message = "Exception running the CE step." + e.getMessage();
            logger.error(message);
            throw new Exception(message, e);
        }
        if (this.ceOperation.getResults().size() == 0) {
            String message = "No concepts found.";
            logger.error(message);
            return;
        }

        // Relationship Extraction
        this.reOperation.setConceptsOperation(this.ceOperation);
        this.reOperation.setCorpus(this.corpus);
        try {
            this.reOperation.start();
        } catch (Exception e) {
            String message = "Exception running the RE." + e.getMessage();
            logger.error(message);
            throw new Exception(message, e);
        }
        if (this.reOperation.getResults().size() == 0) {
            logger.warn("No relationships found!");
        }

        // Summarization
        this.summOperation.setCorpus(this.corpus);
        try {
            this.summOperation.start();
        } catch (Exception e) {
            logger.error("Exception running the summarization step");
            throw new Exception("Exception running the summarization step", e);
        }

        Summary summary = this.summOperation.getResults().get(0);
        
        // Creating the ConceptMap
        this.conceptMap = new ConceptMap();
        this.conceptMap.setName(this.corpus.getName());

        for (Concept concept : this.ceOperation.getResults()) {
            String term = concept.getStemmedTerm();
            int index = this.corpus.getIndexOfTerm(term);
            if(index < 0) {
                logger.debug("Term " + term + " not found in corpus");
            } else {
                double rank = (double) summary.getTermsRank()[index] / (double) summary.getTermsRank().length;
                logger.debug("Term " + term + " found with rank " + rank);
            }
            this.conceptMap.addVertex(concept);
        }

        for (Relationship<Concept> relationship : this.reOperation.getResults()) {
            try {
                Concept cSource = null;
                Concept cTarget = null;

                if (this.conceptMap.getConceptFromTerm(relationship.getSource().getTerm()).size() > 0) {
                    cSource = this.conceptMap.getConceptFromTerm(relationship.getSource().getTerm()).get(0);
                }

                if (this.conceptMap.getConceptFromTerm(relationship.getTarget().getTerm()).size() > 0) {
                    cTarget = this.conceptMap.getConceptFromTerm(relationship.getTarget().getTerm()).get(0);
                }

                if (cSource == null) {
                    this.conceptMap.addVertex(relationship.getSource());
                    cSource = relationship.getSource();
                }

                if (cTarget == null) {
                    this.conceptMap.addVertex(relationship.getTarget());
                    cTarget = relationship.getTarget();
                }

                Relationship<Concept> newRel = new Relationship<Concept>(cSource, relationship.getLinkingWord(), cTarget);

                this.conceptMap.addEdge(
                        newRel.getSource(),
                        newRel.getTarget(),
                        newRel);
            } catch (Exception e) {
                logger.error("Invalid relationship! Can't add it to the final map");
                logger.error(relationship);
                logger.error(e);
            }
        }

        this.conceptMap.summarizeUsingCentrality(25);

        logger.debug("Concept Map created successfully.");
    }

    private boolean parametersAreValid() {
        return this.ceOperation != null
                && this.reOperation != null
                && this.corpus != null
                && this.summOperation != null;
    }
}
