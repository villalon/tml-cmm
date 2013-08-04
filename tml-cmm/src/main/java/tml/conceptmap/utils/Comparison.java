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

import org.apache.log4j.Logger;

import tml.conceptmap.Concept;
import tml.conceptmap.ConceptMap;
import tml.conceptmap.Relationship;

public class Comparison {

	public class ComparisonResult {
		private double precision;
		private double recall;
		private double F;
		private double Kappa;

		public ComparisonResult(double uniqueA, double uniqueB, double intersection, double total) throws Exception {
			if(intersection + uniqueA == 0
					|| intersection + uniqueB == 0) {
				precision = 0;
				recall = 0;
				F = 0;
				Kappa = 0;
			} else {
				//			precision = (total - uniqueA - uniqueB) / total;
				precision = intersection / Math.min(uniqueA + intersection, uniqueB + intersection);
				recall = intersection / Math.max(uniqueA + intersection, uniqueB + intersection);
				if(precision + recall > 0)
					F = (2 * recall * precision) /(precision + recall);
				else
					F = 0;
				Kappa = calculateKappa(uniqueA, uniqueB, intersection, total);
				Kappa = calculateKappaSelection(uniqueA, uniqueB, intersection, total);
			}
		}

		// return the binomial coefficient n choose k.
		private long binomial(int n, int k) {
			return nint(Math.exp(logFactorial(n) - logFactorial(k) - logFactorial(n-k)));
		}

		private double binomialCoefficient(int n, int m) {
			return binomial(m, n);
		}

		private double calculateKappa(double uniqueA, double uniqueB, double intersection, double total) {
			double ncAkC = uniqueA + intersection; 
			double ncBkC = uniqueB + intersection; 
			double ncAkNC = total - (uniqueA + intersection); 
			double ncBkNC = total - (uniqueB + intersection);

			double Ae = (1/Math.pow(total, 2)) * (ncAkC * ncBkC + ncAkNC * ncBkNC);
			double Ao = (total - uniqueA - uniqueB) / total;

			double kappa = (Ao - Ae ) / (1 - Ae);
			return kappa;
		}

		private double calculateKappaSelection(double uniqueA, double uniqueB, double intersection, double total) throws Exception {
			double totalNums = Math.min(uniqueA + intersection, uniqueB + intersection);
			double combinations = binomialCoefficient((int) totalNums, (int) total);
			double Ae = 1 / combinations;
			double Ao = intersection / totalNums;
			
			if(combinations == 0)
				Ae = 0;
			
			if(totalNums == 0)
				Ao = 0;

			double kappa = (Ao - Ae ) / (1 - Ae);
			if(1 - Ae == 0) {
				kappa = 0;
			}
			if(Double.isNaN(kappa))
				throw new Exception("NaN");
			return kappa;
		}

		/**
		 * @return the f
		 */
		public double getF() {
			return F;
		}

		/**
		 * @return the kappa
		 */
		public double getKappa() {
			return Kappa;
		}

		/**
		 * @return the precision
		 */
		public double getPrecision() {
			return precision;
		}

		/**
		 * @return the recall
		 */
		public double getRecall() {
			return recall;
		}

		// return log n!
		private double logFactorial(int n) {
			double ans = 0.0;
			for (int i = 1; i <= n; i++)
				ans += Math.log(i);
			return ans;
		}

		// return integer nearest to x
		private long nint(double x) {
			if (x < 0.0) return (long) Math.ceil(x - 0.5);
			return (long) Math.floor(x + 0.5);
		}

		@Override
		public String toString() {
			return "P:" + this.precision + " R:" + this.recall + " F:" + this.F;
		}
	}

	private static Logger logger = Logger.getLogger(Comparison.class);

	private ConceptList singleConceptsA;
	private ConceptList singleConceptsB;
	private ConceptList commonConcepts;

	private RelationshipList singleRelationshipsA;
	private RelationshipList singleRelationshipsB;
	private RelationshipList commonRelationships;

	private ComparisonResult resultCE = null;
	private ComparisonResult resultRE = null;
	
	public Comparison(ConceptMap cA, ConceptMap cB, int total) throws Exception {
		this.commonConcepts = new ConceptList();
		this.singleConceptsA = new ConceptList();
		this.singleConceptsB = new ConceptList();
		this.commonRelationships = new RelationshipList();
		this.singleRelationshipsA = new RelationshipList();
		this.singleRelationshipsB = new RelationshipList();

		ConceptList allConceptsFromB = new ConceptList();

		// All concepts from CM B in a list
		for(Concept concept : cB.vertexSet()) {
			allConceptsFromB.add(concept);
		}

		for(Concept concept : cA.vertexSet()) {
			int index = allConceptsFromB.indexOf(concept);
			if(index >= 0) {
				this.commonConcepts.add(concept);
			} else {
				this.singleConceptsA.add(concept);
			}
		}

		for(Concept concept : allConceptsFromB) {
			if(this.commonConcepts.indexOf(concept) < 0)
				this.singleConceptsB.add(concept);
		}

		// Compare the two sets of relationships
		RelationshipList allRelationshipsFromB = new RelationshipList();

		for(Relationship<Concept> relationship : cB.edgeSet()) {
			// Relationships are required to have both concepts
			// in the set of common concepts between the to CMs.
			// TODO: WHY? No idea!
			if(this.commonConcepts.contains(relationship.getSource())
					&& this.commonConcepts.contains(relationship.getTarget()))
				allRelationshipsFromB.add(relationship);
		}

		RelationshipList allRelationshipsFromA = new RelationshipList();

		for(Relationship<Concept> relationship : cA.edgeSet()) {
			if(this.commonConcepts.contains(relationship.getSource())
					&& this.commonConcepts.contains(relationship.getTarget()))
				allRelationshipsFromA.add(relationship);
		}

		for(Relationship<Concept> relationship : allRelationshipsFromA) {
			if(allRelationshipsFromB.contains(relationship)) {
				this.commonRelationships.add(relationship);
			} else {
				this.singleRelationshipsA.add(relationship);
			}
		}

		for(Relationship<Concept> relationship : allRelationshipsFromB) {
			if(this.commonRelationships.indexOf(relationship) < 0)
				this.singleRelationshipsB.add(relationship);
		}

		if(this.singleConceptsA.size() + this.commonConcepts.size() == 0
				|| this.singleConceptsB.size() + this.commonConcepts.size() == 0)
			throw new Exception("Invalid comparison");

		int max = Math.max(this.singleConceptsA.size() + this.commonConcepts.size(), 
				this.singleConceptsB.size() + this.commonConcepts.size());

		if(max > total) {
			logger.debug("More observations than total");
			total = max;
		}

		resultCE = new ComparisonResult(
				this.singleConceptsA.size(), 
				this.singleConceptsB.size(), 
				this.commonConcepts.size(),
				total);

		resultRE = new ComparisonResult(
				this.singleRelationshipsA.size(), 
				this.singleRelationshipsB.size(), 
				this.commonRelationships.size(),
				total);
	}

	/**
	 * @return the resultCE
	 */
	public ComparisonResult getResultCE() {
		return resultCE;
	}

	/**
	 * @return the resultRE
	 */
	public ComparisonResult getResultRE() {
		return resultRE;
	}

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("Common concepts\n");
		buff.append("---------------\n");
		for(Concept concept : this.commonConcepts) {
			buff.append(concept);
			buff.append("\n");
		}
		buff.append("---------------\n");
		buff.append("Single concepts A\n");
		buff.append("---------------\n");
		for(Concept concept : this.singleConceptsA) {
			buff.append(concept);
			buff.append("\n");
		}
		buff.append("---------------\n");
		buff.append("Single concepts B\n");
		buff.append("---------------\n");
		for(Concept concept : this.singleConceptsB) {
			buff.append(concept);
			buff.append("\n");
		}
		buff.append("---------------\n");
		buff.append("Common relationships\n");
		buff.append("---------------\n");
		for(Relationship<Concept> relationship : this.commonRelationships) {
			buff.append(relationship);
			buff.append("\n");
		}
		buff.append("---------------\n");
		buff.append("Single relationships A\n");
		buff.append("---------------\n");
		for(Relationship<Concept> relationship : this.singleRelationshipsA) {
			buff.append(relationship);
			buff.append("\n");
		}
		buff.append("---------------\n");
		buff.append("Single relationships B\n");
		buff.append("---------------\n");
		for(Relationship<Concept> relationship : this.singleRelationshipsB) {
			buff.append(relationship);
			buff.append("\n");
		}
		if(resultCE != null) {
			buff.append("---------------\nPrecision CE:");
			buff.append(resultCE.getPrecision());
			buff.append("\n---------------\nRecall CE:");
			buff.append(resultCE.getRecall());
			buff.append("\n---------------\nF CE:");
			buff.append(resultCE.getF());
		}
		if(resultRE != null) {
			buff.append("\n---------------\nPrecision RE:");
			buff.append(resultRE.getPrecision());
			buff.append("\n---------------\nRecall RE:");
			buff.append(resultRE.getRecall());
			buff.append("\n---------------\nF RE:");
			buff.append(resultRE.getF());
		}
		buff.append("\n---------------\n");
		return buff.toString();
	}
}
