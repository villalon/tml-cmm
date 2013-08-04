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

import java.util.Collections;
import java.util.Comparator;

import tml.conceptmap.Concept;
import tml.conceptmap.utils.ConceptComparator;
import tml.vectorspace.operations.AbstractOperation;


/**
 * @author Jorge Villalon
 *
 */
public abstract class AbstractConceptExtraction extends AbstractOperation<Concept> implements ConceptExtractionOperation {

	private ConceptComparator conceptComparator;
	
	@Override
	protected void end() {
		Collections.sort(this.results, new Comparator<Concept>() {
			public int compare(Concept o1, Concept o2) {
				return o1.getTerm().compareTo(o2.getTerm());
			}
			
		});
		super.end();
	}

	/**
	 * @param conceptComparator the conceptComparator to set
	 */
	public void setConceptComparator(ConceptComparator conceptComparator) {
		this.conceptComparator = conceptComparator;
	}

	/**
	 * @return the conceptComparator
	 */
	public ConceptComparator getConceptComparator() {
		return conceptComparator;
	}
}
