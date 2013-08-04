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

import java.util.Comparator;

import tml.conceptmap.Concept;
import tml.conceptmap.Relationship;

public class RelationshipComparator implements Comparator<Relationship<Concept>> {

	private ConceptComparator conceptComparator = null;
	private static RelationshipComparator defaultComparator = null;
	private boolean compareLinkingWords = false;

	/**
	 * @return the compareLinkingWords
	 */
	public boolean isCompareLinkingWords() {
		return compareLinkingWords;
	}

	/**
	 * @param compareLinkingWords the compareLinkingWords to set
	 */
	public void setCompareLinkingWords(boolean compareLinkingWords) {
		this.compareLinkingWords = compareLinkingWords;
	}

	/**
	 * @return the defaultComparator
	 */
	public static RelationshipComparator getDefaultComparator() {
		if(defaultComparator == null) {
			defaultComparator = new RelationshipComparator(ConceptComparator.getDefault(), false);
		}
		return defaultComparator;
	}

	/**
	 * @param defaultComparator the defaultComparator to set
	 */
	public static void setDefaultComparator(RelationshipComparator defaultComparator) {
		RelationshipComparator.defaultComparator = defaultComparator;
	}

	public RelationshipComparator(ConceptComparator cComp, boolean compareLinkingWords) {
		super();
		this.setConceptComparator(cComp);
		this.setCompareLinkingWords(compareLinkingWords);
	}

	public int compare(Relationship<Concept> o1, Relationship<Concept> o2) {
		if(this.conceptComparator == null)
			return Integer.MAX_VALUE;

		int result = 1;

		// If the concepts are equivalent result is 0
		if((this.conceptComparator.compare(o1.getSource(), o2.getSource()) == 0 &&
				this.conceptComparator.compare(o1.getTarget(), o2.getTarget()) == 0)
				||
				(this.conceptComparator.compare(o1.getSource(), o2.getTarget()) == 0 &&
						this.conceptComparator.compare(o1.getTarget(), o2.getSource()) == 0)
		) {
			result = 0;
		} else {
			return result;
		}

		if(!compareLinkingWords)
			return result;

		// If not, create two concepts with the linking words and compare them
		// TODO: Use a semantic idea to compare sentences like 
		//		@ARTICLE{Li2006,
		//			  author = {Yuhua Li and David McLean and Zuhair A. Bandar and James D. O'Shea
		//				and Keeley Crockett},
		//			  title = {Sentence similarity based on semantic nets and corpus statistics},
		//			  journal = {IEEE Transactions on Knowledge and Data Engineering},
		//			  year = {2006},
		//			  volume = {18},
		//			  pages = {1138--1150},
		//			  number = {8}
		//			}

		Concept clA = new Concept(o1.getLinkingWord());
		Concept clB = new Concept(o2.getLinkingWord());

		return this.conceptComparator.compare(clA, clB);
	}

	@Override
	public String toString() {
		return this.conceptComparator.toString();
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
