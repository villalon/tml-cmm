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
package tml.conceptmap;

import java.lang.reflect.Field;

import org.apache.commons.lang.StringUtils;

import tml.utils.LuceneUtils;
import tml.vectorspace.operations.results.AbstractResult;


/**
 * This class represents a concept in the concept map, it can be a term or a phrase.
 * It has a position in the map (as an X,Y pair).
 * 
 * @author Jorge Villalon
 *
 */
public class Concept extends AbstractResult {

	// The term
	protected String term;

	// The stemmed version of the term
	protected String stemmedTerm;

	// The position in the CM
	protected int positionX;
	protected int positionY;

	public Concept(String term) {
		super();
		this.term = cleanTerm(term);
		this.stemmedTerm = LuceneUtils.stemWords(term);
	}

	/**
	 * Cleans terms from non writable characters
	 * @param term
	 * @return the clean term
	 */
	private String cleanTerm(String term) {
		term = term.toLowerCase().trim();
		term = term.replaceFirst("^[\\.,;-_]+", "");
		term = StringUtils.reverse(term);
		term = term.replaceFirst("^[\\.,;-_]+", "");
		term = StringUtils.reverse(term);
		return term;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Concept) {
			Concept concept = (Concept) obj;
			return this.toString().equals(concept.toString());
		} else 
			return false;
	}

	@Override
	public String[] getHeaders() {
		String[] out = new String[this.getClass().getDeclaredFields().length];
		for(int i=0; i<this.getClass().getDeclaredFields().length; i++) {
			Field field = this.getClass().getDeclaredFields()[i];
			out[i] = field.getName();
		}
		return out;
	}

	/**
	 * The X component of the concept's position in the CM
	 * @return
	 */
	public int getPositionX() {
		return positionX;
	}

	/**
	 * The Y component of the concept's position in the CM
	 * @return
	 */
	public int getPositionY() {
		return positionY;
	}

	/**
	 * @return the stemmedTerm
	 */
	public String getStemmedTerm() {
		return stemmedTerm;
	}

	/**
	 * @return the term
	 */
	public String getTerm() {
		return term;
	}

	@Override
	public String[] getValues() throws IllegalArgumentException, IllegalAccessException {
		String[] out = new String[this.getClass().getDeclaredFields().length];
		for(int i=0; i<this.getClass().getDeclaredFields().length; i++) {
			Field field = this.getClass().getDeclaredFields()[i];
			if(field.get(this)!=null)
				out[i] = field.get(this).toString();
		}
		return out;		
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	/**
	 * 
	 * @param positionX the new X position for the concept in the CM
	 */
	public void setPositionX(int positionX) {
		this.positionX = positionX;
	}
	
	/**
	 * 
	 * @param positionY the new Y position for the concept in the CM
	 */
	public void setPositionY(int positionY) {
		this.positionY = positionY;
	}
	
	/**
	 * @param stemmedTerm the stemmedTerm to set
	 */
	public void setStemmedTerm(String stemmedTerm) {
		this.stemmedTerm = stemmedTerm;
	}

	/**
	 * @param term the term to set
	 */
	public void setTerm(String term) {
		this.term = term;
	}
	
	@Override
	public String toString() {
		// It is important for toString to be just the term because it affects
		// relationships' hashCode()
		return this.term + "[" + this.stemmedTerm + "]";
	}
}
