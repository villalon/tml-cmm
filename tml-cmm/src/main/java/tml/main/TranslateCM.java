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
import java.io.FileWriter;

import tml.conceptmap.ConceptMap;


public class TranslateCM {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		if(args == null || args.length == 0) {
			System.out.println("Usage:");
			System.exit(1);
		}
		ConceptMap cmap = ConceptMap.getFromXML(args[0]);
		FileWriter writer = new FileWriter(new File(args[0] + ".txt"));
		writer.append(cmap.exportPropositionsAsText());
		writer.close();
	}

}
