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
package tml.vectorspace.operations.ce;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

import tml.conceptmap.Concept;
import tml.utils.RegexUtils;


public class StopWordsDelimiters extends AbstractConceptExtraction {

	@SuppressWarnings("deprecation")
	@Override
	public void start() throws Exception {
		super.start();

		String[] stopwords = this.corpus.getRepository().getStopwords();
		List<String> stopwordsList = new ArrayList<String>();
		
		for(String stop : stopwords)
			stopwordsList.add(stop);

		if(stopwords != null) {
			List<String> terms = new ArrayList<String>(); 
			for(int i=0; i<this.corpus.getPassages().length; i++) {
				String passageId = this.corpus.getPassages()[i];
				String content = this.corpus.getRepository().getDocumentField(passageId,
						this.corpus.getRepository().getLuceneContentField()).toLowerCase();
				StandardTokenizer tokenizer = new StandardTokenizer(Version.LUCENE_29, new StringReader(content));
				Token tk = new Token();
				String ngram = null;
				while((tk = tokenizer.next(tk)) != null) {
					if(stopwordsList.contains(tk.term()) && ngram != null) {
						if(!terms.contains(ngram))
							terms.add(ngram);
						ngram = null;
						logger.debug(tk.term() + " ignored");
						continue;
					}
					if(ngram == null)
						ngram = tk.term();
					else
						ngram += " " + tk.term();
				}
				
			}
			List<String> cleanList = new ArrayList<String>();
			for(String s : terms) {
				if(!RegexUtils.stringIsContainedInList(cleanList, s)) {
					cleanList.add(s);
					if(this.results.size() < this.maxResults)
						this.results.add(new Concept(s));
				}
			}
		}

		super.end();
	}
}
