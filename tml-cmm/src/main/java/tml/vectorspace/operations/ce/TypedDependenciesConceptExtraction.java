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
package tml.vectorspace.operations.ce;

import java.util.ArrayList;
import java.util.List;

import tml.conceptmap.Concept;
import tml.conceptmap.TerminologicalConcept;
import tml.conceptmap.TerminologicalConceptMap;
import tml.vectorspace.operations.OperationEvent;
import tml.vectorspace.operations.OperationListener;
import tml.vectorspace.operations.TerminologicalMap;

/**
 * @author Jorge Villalon
 *
 */
public class TypedDependenciesConceptExtraction extends AbstractConceptExtraction {

    private List<TerminologicalConceptMap> terminologicalMaps;

    public TypedDependenciesConceptExtraction() {
        this.name = "TypeDepCE";
    }

    @Override
    public void start() throws Exception {
        super.start();

        if (this.terminologicalMaps == null) {
            TerminologicalMap operation = new TerminologicalMap();
            operation.addOperationListener(new OperationListener() {

                public void operationAction(OperationEvent evt) {
                    operationPerformed(evt);
                }
            });
            operation.setCorpus(corpus);
            operation.start();
            this.terminologicalMaps = operation.getResults();
        }

        this.results = new ArrayList<Concept>();

        List<String> terms = new ArrayList<String>();

        int total = 0;
        int actions = 0;
        operationPerformed(new OperationEvent(this, this.terminologicalMaps.size(), actions));
        for (TerminologicalConceptMap cmap : this.terminologicalMaps) {
            for (TerminologicalConcept concept : cmap.vertexSet()) {
                if (isCompoundNoun(concept)
                        && !terms.contains(concept.getTerm())) {
                    this.results.add(concept);
                    terms.add(concept.getTerm());
                    logger.debug("Concept added " + concept);
                    total++;
                }
            }
            actions++;
            operationPerformed(new OperationEvent(this, this.terminologicalMaps.size(), actions));
        }
        operationPerformed(new OperationEvent(this, this.terminologicalMaps.size(), this.terminologicalMaps.size()));

        logger.debug(total + " concepts added");

        super.end();
    }

    private boolean isCompoundNoun(TerminologicalConcept concept) {
        if (concept.getPartOfSpeech().startsWith("NN")) {
            return true;
            
        }
        return false;
    }

    /**
     * @param terminologicalMaps the terminologicalMaps to set
     */
    public void setTerminologicalMaps(List<TerminologicalConceptMap> terminologicalMaps) {
        this.terminologicalMaps = terminologicalMaps;
    }

    /**
     * @return the terminologicalMaps
     */
    public List<TerminologicalConceptMap> getTerminologicalMaps() {
        return terminologicalMaps;
    }
}
