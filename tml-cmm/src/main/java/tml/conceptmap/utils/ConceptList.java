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
package tml.conceptmap.utils;

import java.util.ArrayList;

import tml.conceptmap.Concept;

public class ConceptList extends ArrayList<Concept> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3100043198541118760L;

	@Override
	public boolean contains(Object o) {
		return this.indexOf(o) >= 0;
	}

	@Override
	public boolean remove(Object o) {
		if(!(o instanceof Concept))
			return super.remove(o);

		Concept c2 = (Concept) o;
		int index = -1;
		for(Concept c1 : this) {
			index++;
			if(ConceptComparator.getDefault().compare(c1, c2) == 0) {
				this.remove(index);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int indexOf(Object o) {
		if(!(o instanceof Concept))
			return -1;

		if(ConceptComparator.getDefault() == null)
			return -1;

		Concept c2 = (Concept) o;
		for(int index = 0; index < this.size(); index++) {
			Concept c1 = this.get(index);
			if(ConceptComparator.getDefault().compare(c1, c2) == 0)
				return index;
		}
		return -1;
	}
}
