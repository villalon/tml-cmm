package tml.vectorspace.operations;
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
/**
 * 
 */


import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tml.annotators.PennTreeAnnotator;
import tml.conceptmap.NoPennTreeAnnotationsException;
import tml.utils.StanfordUtils;
import tml.vectorspace.operations.results.TypedDependencyResult;

import edu.stanford.nlp.trees.Tree;

/**
 * This operation extracts a list of dependencies from a sentence corpus. It takes
 * all sentences, obtains their PennTree strings and calculates the typed
 * dependencies.
 * 
 * @author Jorge Villalon
 *
 */
public class TypedDependencies extends AbstractOperation<TypedDependencyResult> {

	/**
	 * Creates a new operation
	 */
	public TypedDependencies() {
		this.name = "Terminological map";
	}

	@Override
	public void start() throws Exception {
		super.start();

		// Dependencies will be stored in a hash table
		Hashtable<String, List<String>> dependencies = new Hashtable<String, List<String>>();
		int total = 0;

		// Callback for asynchronous events
		operationPerformed(new OperationEvent(this, this.corpus.getPassages().length, total));

		// Main loop over each sentence in the corpus
		for(String sentenceId : this.corpus.getPassages()) {
			total++;
			operationPerformed(new OperationEvent(this, this.corpus.getPassages().length, total));

			// Obtain the PennTree annotations from the repository
			Tree tree = null;
			String pennTreeString = null;
			pennTreeString = this.repository.getAnnotations(sentenceId, PennTreeAnnotator.FIELD_NAME);
			if(pennTreeString==null) {
				throw new NoPennTreeAnnotationsException();
			}
			tree = StanfordUtils.getTreeFromString(sentenceId, pennTreeString);

			// Use the tree to obtain the typed dependencies
			List<String> current = StanfordUtils.calculateTypedDependencies(tree);
			if(current != null) {
				// Dependencies are stored as a list of strings for each sentence
				dependencies.put(sentenceId, current);
			}

			// Do not extract more results than the max results
			if(this.maxResults > 0 && total >= this.maxResults)
				break;
		}

		int sentenceNumber = 0;
		operationPerformed(new OperationEvent(this, dependencies.keySet().size(), sentenceNumber));

		// Dependencies strings are now parsed to extract the information in them
		
		// Main loop that goes sentence by sentence
		for(String key : dependencies.keySet()) {
			sentenceNumber++;
			// Loop for each dependency
			for(String dependency : dependencies.get(key)) {
				// The regular expression pattern to extract the info
				Pattern pattern = Pattern.compile("^(\\w+)\\((.+)-(\\d+)'?-(.+), (.+)-(\\d+)'?-(.+)\\)$");
				Matcher matcher = pattern.matcher(dependency);
				if(matcher.matches()) {
					String linking = matcher.group(1);
					String nodeAlbl = matcher.group(2);
					String nodeBlbl = matcher.group(5);
					String nodeAPOS = matcher.group(4);
					if(nodeAPOS.equals("null"))
						nodeAPOS = "NN";
					String nodeBPOS = matcher.group(7);
					if(nodeBPOS.equals("null"))
						nodeBPOS = "NN";
					int posNodeA = Integer.parseInt(matcher.group(3));
					int posNodeB = Integer.parseInt(matcher.group(6));

					TypedDependencyResult result = new TypedDependencyResult();
					result.setNodeA(nodeAlbl);
					result.setNodeB(nodeBlbl);
					result.setLinkingWord(linking);
					result.setSentenceNumber(sentenceNumber);
					result.setSentenceId(key);
					result.setNodeAPOS(nodeAPOS);
					result.setNodeBPOS(nodeBPOS);
					result.setNodeAPosition(posNodeA);
					result.setNodeBPosition(posNodeB);

					if(!linking.equals("root")) {
						results.add(result);
					}
				} else
					throw new Exception("Dependency does not match the regular expression. Dep:" + dependency);
			}
			operationPerformed(new OperationEvent(this, dependencies.keySet().size(), sentenceNumber));
		}

		// Sorts the results by sentence number
		Collections.sort(results, new Comparator<TypedDependencyResult>() {
			public int compare(TypedDependencyResult o1,
					TypedDependencyResult o2) {
				return o1.getSentenceNumber() - o2.getSentenceNumber();
			}
		});
		super.end();
	}
}
