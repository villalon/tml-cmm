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
package tml.vectorspace.operations.re;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.AsUndirectedGraph;

import tml.conceptmap.Concept;
import tml.conceptmap.Relationship;
import tml.conceptmap.TerminologicalConcept;
import tml.conceptmap.TerminologicalConceptMap;
import tml.conceptmap.TerminologicalRelationship;
import tml.vectorspace.operations.OperationEvent;
import tml.vectorspace.operations.TerminologicalMap;
import tml.vectorspace.operations.ce.ConceptExtractionOperation;
import tml.vectorspace.operations.ce.TypedDependenciesConceptExtraction;

public class TypedDependenciesRelationshipExtraction extends AbstractRelationshipExtraction {

    private List<TerminologicalConceptMap> terminologicalMaps;

    /**
     * @return the terminologicalMap
     */
    public List<TerminologicalConceptMap> getTerminologicalMaps() {
        return terminologicalMaps;
    }

    public TypedDependenciesRelationshipExtraction() {
        this.name = "TypeDepRE";
    }

    /**
     * @param terminologicalMap the terminologicalMap to set
     */
    public void setTerminologicalMaps(List<TerminologicalConceptMap> terminologicalMaps) {
        this.terminologicalMaps = terminologicalMaps;
    }

    @Override
    public void start() throws Exception {
        super.start();

        // First retrieve the terminological maps, which are the
        // typed dependencies maps
        if (this.terminologicalMaps == null) {
            if (this.conceptsOperation != null
                    && this.conceptsOperation instanceof TypedDependenciesConceptExtraction
                    && ((TypedDependenciesConceptExtraction) this.conceptsOperation).getTerminologicalMaps() != null) {
                this.terminologicalMaps = ((TypedDependenciesConceptExtraction) this.conceptsOperation).getTerminologicalMaps();
            } else {
                TerminologicalMap operation = new TerminologicalMap();
                operation.setCorpus(corpus);
                operation.start();
                this.terminologicalMaps = operation.getResults();
            }
        }

        ConceptExtractionOperation nounsOp = this.conceptsOperation;

        int actions = 0;
        operationPerformed(new OperationEvent(this, this.terminologicalMaps.size(), actions));
        for (TerminologicalConceptMap map : this.terminologicalMaps) {
            List<String> linkingWordsInMap = new ArrayList<String>();
            for (int i = 0; i < nounsOp.getResultsNumber(); i++) {
                for (int j = i + 1; j < nounsOp.getResultsNumber(); j++) {
                	Concept cI = nounsOp.getResults().get(i);
                	Concept cJ = nounsOp.getResults().get(j);

                    for (TerminologicalConcept cA : map.getConceptFromTerm(cI.getTerm())) {
                        for (TerminologicalConcept cB : map.getConceptFromTerm(cJ.getTerm())) {

                            // If both concepts are the same, we don't include loops
                            if (cA.getTerm().equals(cB.getTerm())) {
                                continue;
                            }

                            // Check if concept A is to the left of concept B in the text. If
                            // it is the case, then swap the concepts.
                            if (!cA.isLeftOf(cB)) {
                            	TerminologicalConcept tmp = cA;
                                cA = cB;
                                cB = tmp;
                            }

                            if (cA != null && cB != null) {
                                DijkstraShortestPath<TerminologicalConcept, TerminologicalRelationship<TerminologicalConcept>> path =
                                        new DijkstraShortestPath<TerminologicalConcept, TerminologicalRelationship<TerminologicalConcept>>(new AsUndirectedGraph<TerminologicalConcept, TerminologicalRelationship<TerminologicalConcept>>(map), cA, cB);

                                // TODO: Fix the need of a list of concepts to check a concept in a path.
                                if (path.getPathEdgeList() != null && path.getPathEdgeList().size() > 0) {
                                	TerminologicalConcept inPath = pathContainsConcept(path.getPathEdgeList(), nounsOp.getResults(), cA, cB);
                                    if (inPath != null) {
                                        //logger.debug("Path should be discarded, contains concept " + inPath);
                                        continue;
                                    }

                                    List<TerminologicalRelationship<TerminologicalConcept>> edgesList = sortPath(path.getPathEdgeList(), cA, cB);

                                    String linkingWord = null;
                                    switch (edgesList.size()) {
                                        case 1:
                                            linkingWord = getLinkingWordFromSingleEdge(edgesList.get(0));
//										if(linkingWord == null)
//											linkingWord = edgesList.get(0).getLinkingWord();
                                            break;
//									case 2:
//										linkingWord = getLinkingWordFromTwoEdges(
//												edgesList.get(0), 
//												edgesList.get(1), 
//												cA,
//												cB);
//										break;
                                        default:
                                            linkingWord = getLinkingWordFromPath(edgesList);
                                            break;
                                    }
                                    if (linkingWord != null) {
                                        boolean ignoreLinkingWord = false;
                                        for (String lw : linkingWordsInMap) {
                                            if (linkingWord.contains(lw)
                                                    || lw.contains(linkingWord)) {
                                                logger.debug("Linking word " + linkingWord + " should be ignored, because it contains " + lw + " which is already in the map");
                                                ignoreLinkingWord = true;
                                            }
                                        }
                                        if(!ignoreLinkingWord) {
                                        linkingWordsInMap.add(linkingWord);
                                        Relationship<Concept> newRel = new Relationship<Concept>(
                                                cA,
                                                linkingWord,
                                                cB);
                                        if (!this.results.contains(newRel)) {
                                            this.results.add(newRel);
                                        }
                                        logger.debug(newRel);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            actions++;
            operationPerformed(new OperationEvent(this, this.terminologicalMaps.size(), actions));
        }
        operationPerformed(new OperationEvent(this, this.terminologicalMaps.size(), this.terminologicalMaps.size()));
        super.end();
    }

	private String getLinkingWordFromPath(List<TerminologicalRelationship<TerminologicalConcept>> edgesList) {
        String linkingWord = "";
        for (int i = 1; i < edgesList.size() - 1; i++) {
        	TerminologicalRelationship<TerminologicalConcept> rel = edgesList.get(i);
            if (rel.getSource().equals(rel.getTarget())) {
                continue;
            }
            if (i == 1) {
                linkingWord += rel.getSource().getTerm() + " ";
            }
//			linkingWord += rel.getLinkingWord() + "-";
            linkingWord += rel.getTarget().getTerm() + " ";
        }
        if (linkingWord.equals("")) {
            return null;
        }
        return linkingWord;
    }

    private List<TerminologicalRelationship<TerminologicalConcept>> sortPath(List<TerminologicalRelationship<TerminologicalConcept>> path, TerminologicalConcept cA, TerminologicalConcept cB) throws Exception {
        List<TerminologicalRelationship<TerminologicalConcept>> oldList = new ArrayList<TerminologicalRelationship<TerminologicalConcept>>();
        List<TerminologicalRelationship<TerminologicalConcept>> newList = new ArrayList<TerminologicalRelationship<TerminologicalConcept>>();
        if (path.get(0).getSource().equals(cA)
                || path.get(0).getTarget().equals(cA)) {
            oldList = path;
        } else if (path.get(0).getSource().equals(cB)
                || path.get(0).getTarget().equals(cB)) {
            for (int i = path.size() - 1; i >= 0; i--) {
                oldList.add(path.get(i));
            }
        }

        Concept prevConcept = cA;
        for (TerminologicalRelationship<TerminologicalConcept> rel : oldList) {
            if (rel.getSource().equals(prevConcept)) {
                newList.add(rel);
                prevConcept = rel.getTarget();
            } else if (rel.getTarget().equals(prevConcept)) {
                newList.add(new TerminologicalRelationship<TerminologicalConcept>(rel.getTarget(), rel.getLinkingWord(), rel.getSource()));
                prevConcept = rel.getSource();
            } else {
                throw new Exception("Invalid path! Couldn't find corresponding concept " + prevConcept + " in " + rel);
            }
        }
        return newList;
    }

	private TerminologicalConcept pathContainsConcept(
			List<TerminologicalRelationship<TerminologicalConcept>> path, 
			List<Concept> concepts, 
			TerminologicalConcept conceptA, 
			TerminologicalConcept conceptB) {
        List<Concept> otherConcepts = new ArrayList<Concept>();
        for (Concept concept : concepts) {
            if (!concept.equals(conceptA)
                    && !concept.equals(conceptB)) {
                otherConcepts.add(concept);
            }
        }
        for (int i = 1; i < path.size() - 1; i++) {
        	TerminologicalRelationship<TerminologicalConcept> rel = path.get(i);
            if (otherConcepts.contains(rel.getSource())) {
                return rel.getSource();
            }
            if (otherConcepts.contains(rel.getTarget())) {
                return rel.getTarget();
            }

        }
        return null;
    }

    private String getLinkingWordFromSingleEdge(TerminologicalRelationship<TerminologicalConcept> relationship) {
        String[] patterns = {"prep_.*"};
        String infixPattern = "prep_(.*)";
        String output = null;

        for (String pattern : patterns) {
            if (relationship.getLinkingWord().matches(pattern)) {
                Pattern patternRegex = Pattern.compile(infixPattern);
                Matcher matcher = patternRegex.matcher(relationship.getLinkingWord());
                if (matcher.matches()) {
                    output = matcher.group(1);
                }
            }
        }

        return output;
    }
}
