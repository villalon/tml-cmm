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

import org.jgrapht.graph.DefaultEdge;

import tml.utils.LuceneUtils;

/**
 * Class representing a relationship in a {@link ConceptMap}. It contains
 * two concepts, a source and a target, and a linking word.
 * 
 * It extends the Edge class from jpgraht to allow algorithms to be run on
 * the CM as it is a generic graph.
 * 
 * @author Jorge
 *
 * @param <V>
 */
public class Relationship<V> extends DefaultEdge {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5777375131580743883L;

	// Concepts, source and target
	private V source;
	private V target;
	// Linking word
	private String linkingWord;
	// Stemmed version of the linking word
	private String stemmedLinkingWord;
	// Position of the relationship in the map
	private int positionX;
	private int positionY;
	
	/**
	 * Creates a new relationship given two concepts and a linking word.
	 * @param sourceConcept the source concept
	 * @param linkingWord the word linking the two concepts
	 * @param targetConcept the target concept
	 * @throws Exception
	 */
	public Relationship(V sourceConcept, String linkingWord, V targetConcept) throws Exception {
		
		// Validates that all parameters are not null
		if(sourceConcept == null || targetConcept == null || linkingWord == null)
			throw new Exception("Invalid arguments, now null concepts or linking words are allowed.");
		
		this.source = sourceConcept;
		this.target = targetConcept;
		this.linkingWord = linkingWord;
		this.stemmedLinkingWord = LuceneUtils.stemWords(linkingWord);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	/**
	 * The equals operation between two relationships calling the equal
	 * operations on the three components: source concepts, target concepts
	 * and the linking word.
	 */
	public boolean equals(Object obj) {
		if(obj instanceof Relationship) {
			Relationship relationship = (Relationship) obj;
			return this.getSource().equals(relationship.getSource())
					&& this.getTarget().equals(relationship.getTarget())
					&& this.getLinkingWord().equals(relationship.getLinkingWord());
		} else
		return false;
	}
	
	/**
	 * The word that links the source and target concepts. 
	 * E.g: In "the fox" -> "jumped over" -> "the dog", the linking word is "jumped over".
	 * @return the linkingWord
	 */
	public String getLinkingWord() {
		return linkingWord;
	}
	
	/**
	 * @return X value for relationship position in the map
	 */
	public int getPositionX() {
		return positionX;
	}
	
	/**
	 * 
	 * @return Y value for relationship position in the map
	 */
	public int getPositionY() {
		return positionY;
	}

	@Override
	/**
	 * The source concept
	 */
	public V getSource() {
		return source;
	}

	/**
	 * @return the stemmed version of the linking word
	 */
	public String getStemmedLinkingWord() {
		return stemmedLinkingWord;
	}

	@Override
	/**
	 * The target concept
	 */
	public V getTarget() {
		return target;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	/**
	 * Changes the linking word of the relationship
	 * @param the new linking word value
	 */
	public void setLinkingWord(String linkingWord) {
		this.linkingWord = linkingWord;
	}

	/**
	 * Changes the X value for the position of the relationship in the map
	 * @param positionX the new X value
	 */
	public void setPositionX(int positionX) {
		this.positionX = positionX;
	}

	/**
	 * Changes the Y value for the position of the relationship in the map
	 * @param positionY the new Y value
	 */
	public void setPositionY(int positionY) {
		this.positionY = positionY;
	}
	
	@Override
	/**
	 * Human readable version of a relationship. Typically in the form:
	 * source concept -> linking word -> target concept
	 */
	public String toString() {
		return this.source.toString() + "-" + this.linkingWord +"[" + this.stemmedLinkingWord + "]->" + this.target.toString();
	}
}
