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
package tml.conceptmap.rules;

import tml.conceptmap.TerminologicalConcept;

public abstract class AbstractVertexRule<E> extends AbstractRule<E> {

	protected TerminologicalConcept vertex = null;

	protected void validateParameters() throws Exception {
		if(patterns == null)
			throw new Exception("Empty pattern in rule " + this.name);

		if(vertex == null)
			throw new Exception("Empty vertex in vertex rule " + this.name);
	}

	public abstract void addPatternFromString(String pattern);

	/**
	 * @return the vertex
	 */
	public TerminologicalConcept getVertex() {
		return vertex;
	}

	/**
	 * @param vertex the vertex to set
	 */
	public void setVertex(TerminologicalConcept vertex) {
		this.vertex = vertex;
	}	
}
