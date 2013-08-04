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
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import tml.conceptmap.Concept;
import tml.utils.RegexUtils;
import tml.utils.WordNetUtils;

import edu.mit.jwi.item.POS;


public class ConceptComparator implements Comparator<Concept> {

	private static Logger logger = Logger.getLogger(ConceptComparator.class);
	private static Hashtable<String, List<String>> synonyms = new Hashtable<String, List<String>>();
	private static ConceptComparator defaultComparator = null;
	private List<String> equalities = new ArrayList<String>();

	/**
	 * @return the equalities
	 */
	public List<String> getEqualities() {
		return equalities;
	}

	/**
	 * @param equalities the equalities to set
	 */
	public void setEqualities(List<String> equalities) {
		this.equalities = equalities;
	}

	private boolean useWordnet = false;
	private boolean useStems = false;
	private boolean compounds = false; 

	public ConceptComparator(boolean useWordnet, boolean useStems,
			boolean compounds) {
		super();
		this.useWordnet = useWordnet;
		this.useStems = useStems;
		this.compounds = compounds;
	}

	public ConceptComparator() {
	}

	/**
	 * @return the compounds
	 */
	public boolean isCompounds() {
		return compounds;
	}

	/**
	 * @param compounds the compounds to set
	 */
	public void setCompounds(boolean compounds) {
		this.compounds = compounds;
	}

	/**
	 * @return the useWordnet
	 */
	public boolean isUseWordnet() {
		return useWordnet;
	}

	/**
	 * @param useWordnet the useWordnet to set
	 */
	public void setUseWordnet(boolean useWordnet) {
		this.useWordnet = useWordnet;
	}

	/**
	 * @return the useStems
	 */
	public boolean isUseStems() {
		return useStems;
	}

	/**
	 * @param useStems the useStems to set
	 */
	public void setUseStems(boolean useStems) {
		this.useStems = useStems;
	}

	public int compare(Concept o1, Concept o2) {
		// If the two terms are equal, simply return 0
		if(o1.getTerm().equals(o2.getTerm())) {
			String equality = o1.getTerm() + "=" + o2.getTerm() + "(term equals)";
			if(!equalities.contains(equality))
				equalities.add(equality);
			return 0;
		}
		
		List<String> terms1 = new ArrayList<String>();
		List<String> terms2 = new ArrayList<String>();

		if(this.isUseStems()) {
			terms1.add(o1.getStemmedTerm());
			terms2.add(o2.getStemmedTerm());
		} else {
			terms1.add(o1.getTerm());
			terms2.add(o2.getTerm());
		}

		if(this.isUseWordnet()) {
			for(String syn1 : getWordNetSynonyms(o1)) {
				terms1.add(syn1);
			}
			for(String syn2 : getWordNetSynonyms(o2)) {
				terms2.add(syn2);
			}
		}

		int result = -1;
		for(String t1 : terms1) {
			t1 = t1.toLowerCase();
			for(String t2 : terms2) {
				t2 = t2.toLowerCase();
				if(this.isCompounds()) {
					// Compares two terms
					if(t1.equals(t2)) {
						String equality = t1 + "=" + t2 + "(stemmed equals)";
						if(!equalities.contains(equality))
							equalities.add(equality);
						return 0;
					}

					// If one of the terms contains the other 
					if(RegexUtils.stringContained(t1, t2)) {
						String equality = t1 + "=" + t2 + "(contained)";
						if(!equalities.contains(equality))
							equalities.add(equality);
						return 0;
					}
				} else {
					result = t1.compareTo(t2);
					if(result == 0) {
						String equality = t1 + "=" + t2 + "(equals 2)";
						if(!equalities.contains(equality))
							equalities.add(equality);
						return result;
					}
				}
			}
		}

		return result;
	}

	private List<String> getWordNetSynonyms(Concept concept) {
		String term = concept.getTerm();

		if(ConceptComparator.synonyms.containsKey(term))
			return ConceptComparator.synonyms.get(term);

		POS wPOS = POS.NOUN;

		ConceptComparator.synonyms.put(term, WordNetUtils.getSynonyms(term, wPOS));
		return ConceptComparator.synonyms.get(term);
	}

	@Override
	public String toString() {
		String name = "";
		if(this.isCompounds())
			name += "Compounds";
		else
			name += "NoCompounds";
		if(this.isUseStems())
			name += "Stems";
		else
			name += "NoStems";
		if(this.isUseWordnet())
			name += "WordNet";
		else
			name += "NoWordNet";
		return name;
	}

	public static ConceptComparator getDefault() {
		if(ConceptComparator.defaultComparator == null) {
			ConceptComparator.defaultComparator = new ConceptComparator();
			ConceptComparator.defaultComparator.setCompounds(true);
			ConceptComparator.defaultComparator.setUseStems(true);
			ConceptComparator.defaultComparator.setUseWordnet(true);
		}
		return ConceptComparator.defaultComparator;
	}

	public static void setDefault(ConceptComparator ccomp) {
		logger.debug("Setting new concept comparator to " + ccomp);
		ConceptComparator.defaultComparator = ccomp;
	}
}
