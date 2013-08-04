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
package tml.vectorspace.operations.re;

import tml.conceptmap.Concept;
import tml.conceptmap.Relationship;
import tml.vectorspace.operations.AbstractOperation;
import tml.vectorspace.operations.ce.ConceptExtractionOperation;

/**
 * @author Jorge Villalon
 *
 */
public abstract class AbstractRelationshipExtraction extends AbstractOperation<Relationship<Concept>> implements RelationshipExtractionOperation {
	
	protected ConceptExtractionOperation conceptsOperation = null;

	/**
	 * @return the concepts
	 */
	public ConceptExtractionOperation getConceptsOperation() {
		return conceptsOperation;
	}

	/**
	 * @param conceptsOp the concepts to set
	 */
	public void setConceptsOperation(ConceptExtractionOperation conceptsOp) {
		this.conceptsOperation = conceptsOp;
	}
	
	@Override
	public void start() throws Exception {
		super.start();
		
		if(this.conceptsOperation == null)
			throw new Exception("Can't extract relationships if concepts are not assigned");
	}
}
