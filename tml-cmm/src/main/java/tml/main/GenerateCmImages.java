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
package tml.main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import tml.Configuration;
import tml.conceptmap.ConceptMap;
import tml.conceptmap.ui.PrefuseMap;


public class GenerateCmImages {

	private static final String DOCUMENT_MATCH = "Diagnostic(01).*";
	private static final String JUDGE_MATCH = "(TypeDepCETypeDepRELSA.*)";
	
	private static Logger logger = Logger.getLogger(CMMexp.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Configuration.setDebugMode(true);
		Configuration.getTmlProperties();

		List<File> directories = new ArrayList<File>();

		directories.add(new File("target/annotations/"));

		for(File dir : directories) {
			if(!dir.exists())
				continue;
			
			for(File f1 : dir.listFiles()) {
				if(f1.isDirectory())
					continue;
				try {
					String judge = f1.getName().split("-")[0];
					String essay = f1.getName().split("-")[1].split("\\.")[0];
					if(!judge.matches(JUDGE_MATCH))
						continue;
					if(!essay.matches(DOCUMENT_MATCH))
						continue;
					logger.debug("Reading concept map from " + f1.getName());
					ConceptMap map = ConceptMap.getFromXML(f1.getAbsolutePath());
					PrefuseMap imgMap = new PrefuseMap(map);
					imgMap.saveImage(new File("target/images/" + f1.getName() + ".jpg"));
				} catch (Exception e) {
					logger.error(f1.getName());
					continue;
				}
			}
		}
	}
}
