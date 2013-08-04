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
import java.util.Comparator;

import tml.conceptmap.Concept;
import tml.conceptmap.Relationship;

public class RelationshipList extends ArrayList<Relationship<Concept>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -455511091903521489L;
	
	private Comparator<Relationship<Concept>> comparator = null;
	/**
	 * @return the comparator
	 */
	public Comparator<Relationship<Concept>> getComparator() {
		return comparator;
	}

	/**
	 * @param comparator the comparator to set
	 */
	public void setComparator(Comparator<Relationship<Concept>> comparator) {
		this.comparator = comparator;
	}

	@Override
	public boolean contains(Object o) {
		return indexOf(o) >= 0;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public int indexOf(Object o) {
		if(!(o instanceof Relationship))
			return -1;

		if(ConceptComparator.getDefault() == null)
			return -1;

		Relationship<Concept> r2 = (Relationship<Concept>) o;
		for(int index = 0; index < this.size(); index++) {
			Relationship<Concept> r1 = this.get(index);
			if(RelationshipComparator.getDefaultComparator().compare(r1, r2) == 0)
				return index;
		}
		return -1;	
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		if(!(o instanceof Concept))
			return super.remove(o);
		
		if(this.contains(o)) {
			int index = -1;
			for(Relationship<Concept> c1 : this) {
				index++;
				Relationship<Concept> c2 = (Relationship<Concept>) o;
				if(this.comparator.compare(c1, c2) == 0)
					break;
			}
			this.remove(index);
			return true;
		} else {
			return false;
		}
	}
}
