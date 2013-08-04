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

import java.util.List;

/**
 * This class represents a terminological concept in a terminological map,
 *  it extends a normal {@link Concept} adding the part of speech and position
 *  within a sentence.
 *  This concepts are used to run syntactic rules that organize compound nouns and
 *  links them through typed dependencies.
 *  
 * @author Jorge Villalon
 *
 */
public class TerminologicalConcept extends Concept {

	/**
	 * Given two concepts and an infix, it calculates the new term for a concept
	 * that merges both.
	 * 
	 * @param conceptA
	 * @param conceptB
	 * @param infix
	 * @return
	 */
	private static String[] calculateNewTerms(TerminologicalConcept conceptA, TerminologicalConcept conceptB, String infix) {
		String[] output = new String[2];
		String newTerm = null;
		String newStemmedTerm = null;
		String infixTerm = "";
		if(infix != null)
			infixTerm = infix + " ";
		if(conceptA.getPositionInSentenceRight() >= conceptB.getPositionInSentenceLeft()) {
			newTerm = conceptB.getTerm() + " " + infixTerm + conceptA.getTerm();
			newStemmedTerm = conceptB.getStemmedTerm() + " " + infixTerm + conceptA.getStemmedTerm();
		} else {
			newTerm = conceptA.getTerm() + " " + infixTerm + conceptB.getTerm();
			newStemmedTerm = conceptA.getStemmedTerm() + " " + infixTerm + conceptB.getStemmedTerm();
		}
		output[0] = newTerm;
		output[1] = newStemmedTerm;
		return output;
	}
	
	/**
	 * When two concepts are merged, the new POS must be calculated for the
	 * resultant concept. This function does so.
	 * 
	 * @param posA the POS of the source concept
	 * @param posB the POS of the target concept
	 * @return
	 */
	private static String calculatePOSfromMerge(String posA, String posB) {
		if(posA == null && posB == null)
			return "NN";
		
		if(posA == null)
			return posB;
		
		if(posB == null)
			return posA;
		
		if(posA.length() >= 2 && 
				posB.length() >= 2 && 
				posA.substring(0, 2).equals(posB.substring(0, 2))) {
			return posA.substring(0, 2);
		}
		
		return posB;
	}
	
	/**
	 * This function merges a list of terminological concepts starting from the first
	 * and adding one at the time. It uses an infix between all concepts.
	 * 
	 * @param concepts
	 * @param infix
	 * @return
	 */
	public static TerminologicalConcept mergeManyConcepts(List<TerminologicalConcept> concepts, String[] infix) {
		TerminologicalConcept finalConcept = concepts.get(0);
		for(int i=1; i < concepts.size(); i++) {
			finalConcept = mergeTwoConcepts(finalConcept, concepts.get(i), infix[i]);
		}
		return finalConcept;
	}
	
	/**
	 * This function merges a list of terminological concepts starting from the first
	 * and adding one at the time.
	 * @param concepts
	 * @return
	 */
	public static TerminologicalConcept mergeManyConcepts(TerminologicalConcept[] concepts) {
		TerminologicalConcept finalConcept = concepts[0];
		for(int i=1; i < concepts.length; i++) {
			finalConcept = mergeTwoConcepts(finalConcept, concepts[i]);
		}
		return finalConcept;
	}
	
	/**
	 * Merges two concepts into a single one calculating the new stemmed term using a call tp
	 * a private function.
	 * @param conceptA
	 * @param conceptB
	 * @return
	 */
	public static TerminologicalConcept mergeTwoConcepts(TerminologicalConcept conceptA, TerminologicalConcept conceptB) {
		String newTerm = calculateNewTerms(conceptA, conceptB, null)[0];
		String newStemmedTerm = calculateNewTerms(conceptA, conceptB, null)[1];

		return mergeTwoConcepts(conceptA, conceptB, newTerm, newStemmedTerm);
	}
	
	/**
	 * Merges two concepts using an infix between the terms.
	 * @param conceptA
	 * @param conceptB
	 * @param infix
	 * @return
	 */
	public static TerminologicalConcept mergeTwoConcepts(TerminologicalConcept conceptA, TerminologicalConcept conceptB, String infix) {
		String newTerm = calculateNewTerms(conceptA, conceptB, infix)[0];
		String newStemmedTerm = calculateNewTerms(conceptA, conceptB, infix)[1];

		return mergeTwoConcepts(conceptA, conceptB, newTerm, newStemmedTerm);
	}

	/**
	 * Merges two concepts knowing the new term and its stemmed version.
	 * @param conceptA
	 * @param conceptB
	 * @param newTerm
	 * @param newStemmedTerm
	 * @return
	 */
	public static TerminologicalConcept mergeTwoConcepts(TerminologicalConcept conceptA, TerminologicalConcept conceptB, String newTerm, String newStemmedTerm) {
		TerminologicalConcept newConcept = new TerminologicalConcept(newTerm);
		
		String newPOS = calculatePOSfromMerge(conceptA.getPartOfSpeech(), conceptB.getPartOfSpeech());
		newConcept.setPartOfSpeech(newPOS);
		
		newConcept.setPositionInSentenceLeft(Math.min(conceptA.getPositionInSentenceLeft(),conceptB.getPositionInSentenceLeft()));
		newConcept.setPositionInSentenceRight(Math.max(conceptA.getPositionInSentenceRight(),conceptB.getPositionInSentenceRight()));
		
		newConcept.setStemmedTerm(newStemmedTerm);

		return newConcept;
	}

	private int positionInSentenceLeft;
	
	private int positionInSentenceRight;
	
	private String partOfSpeech;

	public TerminologicalConcept(String term) {
		super(term);
	}

	/**
	 * The part of speech of the concept
	 * @return a string with a pos (e.g: "NN")
	 */
	public String getPartOfSpeech() {
		return partOfSpeech;
	}

	/**
	 * @return the position of the concept in the source sentence from the left
	 */
	public int getPositionInSentenceLeft() {
		return positionInSentenceLeft;
	}

	/**
	 * @return the position of the concept in the source sentence from the right
	 */
	public int getPositionInSentenceRight() {
		return positionInSentenceRight;
	}

	/**
	 * 
	 * @param concept
	 * @return if the concept is left of another concept
	 */
	public boolean isLeftOf(TerminologicalConcept concept) {
		if(this.positionInSentenceRight < concept.positionInSentenceLeft
				|| this.positionInSentenceLeft < concept.positionInSentenceLeft)
			return true;
		else 
			return false;
	}

	/**
	 * sets the part of speech for this concept
	 * @param partOfSpeech
	 */
	public void setPartOfSpeech(String partOfSpeech) {
		this.partOfSpeech = partOfSpeech;
	}
	
	/**
	 * @param positionInSentence the positionInSentence to set
	 */
	public void setPositionInSentenceLeft(int positionInSentence) {
		this.positionInSentenceLeft = positionInSentence;
	}
	
	/**
	 * @param positionInSentenceRight the positionInSentenceRight to set
	 */
	public void setPositionInSentenceRight(int positionInSentenceRight) {
		this.positionInSentenceRight = positionInSentenceRight;
	}
	
	@Override
	public String toString() {
		// It is important for toString to be just the term because it affects
		// relationships' hashCode()
		return this.term + "[" + this.stemmedTerm + "-" + this.partOfSpeech + "-" + this.positionInSentenceLeft + ".." + this.positionInSentenceRight + "]";
	}
}
