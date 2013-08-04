package tml.vectorspace.operations;
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


import java.util.ArrayList;
import java.util.List;

import tml.conceptmap.TerminologicalConcept;
import tml.conceptmap.TerminologicalConceptMap;
import tml.conceptmap.TerminologicalRelationship;
import tml.conceptmap.rules.AbstractEdgeRule;
import tml.conceptmap.rules.AbstractRule;
import tml.conceptmap.rules.AbstractVertexRule;
import tml.conceptmap.rules.RulesList;
import tml.vectorspace.operations.results.TypedDependencyResult;

/**
 * @author Jorge Villalon
 *
 */
public class TerminologicalMap extends AbstractOperation<TerminologicalConceptMap> {

    public TerminologicalMap() {
        this.name = "Terminological processed map";
    }
    private boolean applyRules = true;

    public boolean isApplyRules() {
        return applyRules;
    }

    public void setApplyRules(boolean applyRules) {
        this.applyRules = applyRules;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void start() throws Exception {
        super.start();
        TypedDependencies operation = new TypedDependencies();
        operation.setCorpus(corpus);
        operation.setMaxResults(maxResults);
        operation.addOperationListener(new OperationListener() {

            public void operationAction(OperationEvent evt) {
                operationPerformed(evt);
            }
        });
        operation.start();

        List<TerminologicalConceptMap> maps = new ArrayList<TerminologicalConceptMap>();

        String currentSentence = "";
        TerminologicalConceptMap currentMap = null;
        int total = 0;
        operationPerformed(new OperationEvent(this, operation.getResults().size(), total));
        for (TypedDependencyResult r : operation.getResults()) {
            total++;
            operationPerformed(new OperationEvent(this, operation.getResults().size(), total));
            if (!currentSentence.equals(r.getSentenceId())) {
                currentSentence = r.getSentenceId();
                maps.add(new TerminologicalConceptMap());
                currentMap = maps.get(maps.size() - 1);
                currentMap.setName(r.getSentenceId());
                currentMap.setText(corpus.getRepository().getDocumentField(
                        currentSentence, corpus.getRepository().getLuceneContentField()));
            }
            TerminologicalConcept cA = currentMap.getConceptFromTerm(r.getNodeA(), r.getNodeAPosition());
            if (cA == null) {
                cA = new TerminologicalConcept(r.getNodeA());
                cA.setPartOfSpeech(r.getNodeAPOS());
                cA.setPositionInSentenceLeft(r.getNodeAPosition());
                cA.setPositionInSentenceRight(r.getNodeAPosition());
            }
            TerminologicalConcept cB = currentMap.getConceptFromTerm(r.getNodeB(), r.getNodeBPosition());
            if (cB == null) {
                cB = new TerminologicalConcept(r.getNodeB());
                cB.setPartOfSpeech(r.getNodeBPOS());
                cB.setPositionInSentenceLeft(r.getNodeBPosition());
                cB.setPositionInSentenceRight(r.getNodeBPosition());
            }
            if (!currentMap.containsVertex(cA)) {
                currentMap.addVertex(cA);
            }
            if (!currentMap.containsVertex(cB)) {
                currentMap.addVertex(cB);
            }
            currentMap.addEdge(cA, cB,
                    new TerminologicalRelationship<TerminologicalConcept>(cA, r.getLinkingWord(), cB));
        }

        if (maps.size() == 0) {
            this.results = new ArrayList<TerminologicalConceptMap>();
            super.end();
            return;
        }

        operationPerformed(new OperationEvent(this, maps.size(), 0));
        for (int i = 0; i < maps.size(); i++) {
            operationPerformed(new OperationEvent(this, maps.size(), i));
            TerminologicalConceptMap cmap = maps.get(i);
//			cmap.printMap();

            // For each rule in the list, process it until there's nothing to process
            for (AbstractRule rule : RulesList.getDefaultRules()) {
                boolean ruleFinished = false;

                // If it shouldn't apply rules just ignore them
                if (!this.isApplyRules()) {
                    continue;
                }

                while (!ruleFinished) {
                    ruleFinished = true;
                    rule.setConceptMap(cmap);
//					rule.setNewConceptMap(null);

                    if (rule instanceof AbstractVertexRule) {
                        AbstractVertexRule vrule = (AbstractVertexRule) rule;
                        for (TerminologicalConcept vertex : cmap.vertexSet()) {
                            vrule.setVertex(vertex);
                            if (vrule.process()) {
                                ruleFinished = false;
                                break;
                            }
                        }
                    } else if (rule instanceof AbstractEdgeRule) {
                        AbstractEdgeRule erule = (AbstractEdgeRule) rule;
                        for (TerminologicalRelationship<TerminologicalConcept> relationship : cmap.edgeSet()) {
                            erule.setRelationship(relationship);
                            if (erule.process()) {
                                ruleFinished = false;
                                break;
                            }
                        }
                    }
                }

                logger.debug("Rule " + rule.getName() + " finished");
                cmap.printMap();
            }
            this.results.add(cmap);
        }
        super.end();
    }
}
