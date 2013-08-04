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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import tml.conceptmap.ConceptMap;
import tml.corpus.TextDocument;
import tml.storage.Repository;
import tml.utils.Stats;
import tml.vectorspace.operations.Summary;
import tml.vectorspace.operations.ce.CompoundNouns;
import tml.vectorspace.operations.results.SummaryResult;


public class CMMExperiment {

	private static Logger logger = Logger.getLogger(CMMExperiment.class);
	private String documentMatch = "Diagnostic.*"; 
	private String judgeMatch = ".*";

	/**
	 * @return the judgeMatch
	 */
	public String getJudgeMatch() {
		return judgeMatch;
	}

	/**
	 * @param judgeMatch the judgeMatch to set
	 */
	public void setJudgeMatch(String judgeMatch) {
		this.judgeMatch = judgeMatch;
	}

	/**
	 * @return the documentMatch
	 */
	public String getDocumentMatch() {
		return documentMatch;
	}

	/**
	 * @param documentMatch the documentMatch to set
	 */
	public void setDocumentMatch(String documentMatch) {
		this.documentMatch = documentMatch;
	}

	public class ExperimentResult {
		private Stats precisionCE;
		private Stats recallCE;
		private Stats fCE;
		private Stats kappaCE;
		private Stats precisionRE;
		private Stats recallRE;
		private Stats fRE;
		private Stats kappaRE;

		public ExperimentResult() {
			this.precisionCE = new Stats();
			this.recallCE = new Stats();
			this.fCE = new Stats();
			this.kappaCE = new Stats();
			this.precisionRE = new Stats();
			this.recallRE = new Stats();
			this.fRE = new Stats();
			this.kappaRE = new Stats();
		}

		public void addComparison(Comparison comparison) {
			if(comparison.getResultCE()!=null) {
				addValueToStats(comparison.getResultCE().getPrecision(), this.precisionCE);
				addValueToStats(comparison.getResultCE().getRecall(), this.recallCE);
				addValueToStats(comparison.getResultCE().getF(), this.fCE);
				addValueToStats(comparison.getResultCE().getKappa(), this.kappaCE);
			}
			if(comparison.getResultRE()!=null) {
				addValueToStats(comparison.getResultRE().getPrecision(), this.precisionRE);
				addValueToStats(comparison.getResultRE().getRecall(), this.recallRE);
				addValueToStats(comparison.getResultRE().getF(), this.fRE);
				addValueToStats(comparison.getResultRE().getKappa(), this.kappaRE);
			}
		}

		private void addValueToStats(double v, Stats stats) {
			if(!Double.isInfinite(v)
					&& !Double.isNaN(v))
				stats.add(v);		
		}

		public void calculateDerived() {
			this.kappaCE.calculateDerived();
			this.fCE.calculateDerived();
			this.recallCE.calculateDerived();
			this.precisionCE.calculateDerived();
			this.kappaRE.calculateDerived();
			this.fRE.calculateDerived();
			this.recallRE.calculateDerived();
			this.precisionRE.calculateDerived();
		}
	}

	private List<String> judges;
	private List<String> essays;
	private Hashtable<String, ConceptMap> observations;
	private List<ConceptComparator> comparators;
	private Hashtable<ConceptComparator, ExperimentResult[][]> results;
	private boolean includeRelationships = true;
	private boolean totalUnitsAreNouns = true;
	private boolean includeRecallAndF = false;

	/**
	 * @return the includeRecallAndF
	 */
	public boolean isIncludeRecallAndF() {
		return includeRecallAndF;
	}

	/**
	 * @param includeRecallAndF the includeRecallAndF to set
	 */
	public void setIncludeRecallAndF(boolean includeRecallAndF) {
		this.includeRecallAndF = includeRecallAndF;
	}

	/**
	 * @return the totalUnitsAreNouns
	 */
	public boolean isTotalUnitsAreNouns() {
		return totalUnitsAreNouns;
	}

	/**
	 * @param totalUnitsAreNouns the totalUnitsAreNouns to set
	 */
	public void setTotalUnitsAreNouns(boolean totalUnitsAreNouns) {
		this.totalUnitsAreNouns = totalUnitsAreNouns;
	}

	/**
	 * @return the includeRelationships
	 */
	public boolean isIncludeRelationships() {
		return includeRelationships;
	}

	/**
	 * @param includeRelationships the includeRelationships to set
	 */
	public void setIncludeRelationships(boolean includeRelationships) {
		this.includeRelationships = includeRelationships;
	}

	public CMMExperiment() {
		this.judges = new ArrayList<String>();
		this.essays = new ArrayList<String>();
		this.observations = new Hashtable<String, ConceptMap>();
		this.comparators = new ArrayList<ConceptComparator>();
		this.results = new Hashtable<ConceptComparator, ExperimentResult[][]>();
	}

	public void addComparator(ConceptComparator comp) {
		this.comparators.add(comp);
	}

	public void addJudge(String judge) {
		if(!judge.matches(judgeMatch))
			return;

		if(this.judges.contains(judge))
			return;

		this.judges.add(judge);
	}

	public void addEssay(String essay) {
		if(!essay.matches(documentMatch))
			return;

		if(this.essays.contains(essay))
			return;

		this.essays.add(essay);
	}

	public void addObservation(String judge, String essay, ConceptMap map) {
		if(!judges.contains(judge)
				|| !essays.contains(essay))
			return;

		String key = key(judge, essay);
		if(this.observations.containsKey(key)) {
			this.observations.remove(this.observations.get(key));
		}
		this.observations.put(key, map);
	}

	public void process(Repository repository) throws Exception {
		Hashtable<String, Integer> totals = loadDocumentTotals(repository);

		for(ConceptComparator comparator : comparators) {
			ConceptComparator.setDefault(comparator);
			ExperimentResult[][] result = new ExperimentResult[this.judges.size()][this.judges.size()];
			this.results.put(comparator, result);
			for(int i=0; i<this.judges.size(); i++) {
				for(int j=i+1; j<this.judges.size(); j++) {
					result[i][j] = new ExperimentResult();
					String judge1 = this.judges.get(i);
					String judge2 = this.judges.get(j);
					for(String essay : this.essays) {
						if(!essay.matches(documentMatch))
							continue;
						ConceptMap c1 = this.observations.get(key(judge1, essay));
						ConceptMap c2 = this.observations.get(key(judge2, essay));
						try {
							Comparison comp = new Comparison(c1, c2, totals.get(essay));
							result[i][j].addComparison(comp);
							logger.debug(c1);
							logger.debug(c2);
							logger.debug(comp);

							logger.debug("Equalities");
							for(String equality : comparator.getEqualities()) {
								logger.debug(equality);
							}
							comparator.setEqualities(new ArrayList<String>());
						} catch (Exception e) {
							logger.error("Invalid values on " + essay + " " + judge1 + " " + judge2);
							logger.error(e);
						}
					}
					result[i][j].calculateDerived();
				}
			}
		}
	}

	private Hashtable<String, Integer> loadDocumentTotals(Repository repository) throws Exception {
		Hashtable<String, Integer> totals = loadFromFile();
		if(totals != null)
			return totals;

		totals = new Hashtable<String, Integer>();
		for(TextDocument doc : repository.getAllTextDocuments()) {
			// Filters documents to include in processing

			// Loads document and calculate total potential concepts
			doc.load(repository);
			int value = -1;
			if(!this.isTotalUnitsAreNouns()) {
				Summary summary = new Summary();
				summary.setCorpus(doc.getSentenceCorpus());
				summary.start();
				for(SummaryResult r : summary.getResults()) {
					if(r.getItem().equals("Terms"))
						value = Integer.parseInt(r.getValue());					
				}
			} else {
				CompoundNouns nouns = new CompoundNouns();
				nouns.setCorpus(doc.getSentenceCorpus());
				nouns.start();
				value = nouns.getResultsNumber();
			}
			totals.put(doc.getExternalId(), value);
		}
		saveTotals(totals);
		return totals;
	}
	public void printSummary(int width) {
		String emptyString = String.format("%1$" + width + "s", " ");
		String dataPattern = "%1$4.2f|%2$4.2f|%3$4.2f|%5$4.2f";
		if(this.isIncludeRecallAndF())
			dataPattern = "%1$4.2f|%2$4.2f|%3$4.2f|%4$4.2f";
		String namePattern = "%1$" + width + "s";
		for(ConceptComparator comparator : comparators) {
			System.out.println(comparator);
			ExperimentResult[][] result = this.results.get(comparator);
			System.out.print(emptyString);
			for(String judge : this.judges)
				System.out.print(String.format(namePattern, judge));
			System.out.println();
			for(int i=0; i<this.judges.size(); i++) {
				String judge = this.judges.get(i);
				System.out.print(String.format(namePattern, judge));
				for(int j=0; j<this.judges.size(); j++) {
					if(result[i][j] != null) {
						Object[] dataCE = {new Double(result[i][j].precisionCE.mean),
								new Double(result[i][j].recallCE.mean),
								new Double(result[i][j].fCE.mean),
								new Double(result[i][j].kappaCE.mean),
								new Double(result[i][j].precisionCE.count)};
						System.out.print(String.format(namePattern, String.format(dataPattern, dataCE)));
					} else {
						System.out.print(emptyString);
					}
				}
				System.out.println();
				System.out.print(emptyString);
				if(this.includeRelationships)
					for(int j=0; j<this.judges.size(); j++) {
						if(result[i][j] != null) {
							Object[] data = {new Double(result[i][j].precisionRE.mean),
									new Double(result[i][j].recallRE.mean),
									new Double(result[i][j].fRE.mean),
									new Double(result[i][j].kappaRE.mean),
									new Double(result[i][j].precisionRE.count)};
							System.out.print(String.format(namePattern, String.format(dataPattern, data)));
						} else {
							System.out.print(emptyString);
						}
					}
				System.out.println();
			}
		}
	}

	public void printSummaryLong(int width) {
		System.out.println();
		System.out.print("Comparator|Judge 1|Judge 2|Pce|Rce|Fce|Nce");
		String dataPattern = "%1$4.2f|%2$4.2f|%3$4.2f|%5$4.2f";
		if(this.includeRelationships) {
			System.out.print("|Pre|Rre|Fre|Nre");
		}
		System.out.println();
		for(ConceptComparator comparator : comparators) {
			ExperimentResult[][] result = this.results.get(comparator);
			for(int i=0; i<this.judges.size(); i++) {
				String judge = this.judges.get(i);
				for(int j=0; j<this.judges.size(); j++) {
					String judge2 = this.judges.get(j);
					if(result[i][j] != null) {
						System.out.print(comparator + "|");
						System.out.print(judge + "|" + judge2 + "|");
						Object[] dataCE = {new Double(result[i][j].precisionCE.mean),
								new Double(result[i][j].recallCE.mean),
								new Double(result[i][j].fCE.mean),
								new Double(result[i][j].kappaCE.mean),
								new Double(result[i][j].precisionCE.count)};
						System.out.print(String.format(dataPattern, dataCE));
					}
					if(this.includeRelationships && result[i][j] != null) {
						Object[] data = {new Double(result[i][j].precisionRE.mean),
								new Double(result[i][j].recallRE.mean),
								new Double(result[i][j].fRE.mean),
								new Double(result[i][j].kappaRE.mean),
								new Double(result[i][j].precisionRE.count)};
						System.out.print("|" + String.format(dataPattern, data));
					}
					if(result[i][j] != null || (this.includeRelationships && result[i][j] != null))
					System.out.println();
				}
			}
		}
	}

	private String key(String judge, String essay) {
		return judge + "-" + essay;
	}

	@SuppressWarnings("unchecked")
	private Hashtable<String, Integer> loadFromFile() throws Exception {
		File file = new File("target/cmmexpTotals.bin");
		if(!file.exists()) {
			logger.debug("Miss!");
			return null;
		}
		logger.debug("Hit!");
		FileInputStream inputStream = new FileInputStream(file);
		ObjectInputStream objectStream = new ObjectInputStream(inputStream);
		Hashtable<String, Integer> output = (Hashtable<String, Integer>) objectStream.readObject();
		objectStream.close();
		return output;
	}

	private void saveTotals(Hashtable<String, Integer> totals) {
		try {
			FileOutputStream fileStream = new FileOutputStream("target/cmmexpTotals.bin");
			ObjectOutputStream objectStream = new ObjectOutputStream(fileStream);
			objectStream.writeObject(totals);
			objectStream.close();
		} catch (Exception e) {
			logger.error("Couldn't save document totals");
			logger.error(e);
		}
	}
}