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
package tml.conceptmap.rules;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import tml.Configuration;
import tml.conceptmap.rules.AbstractRule.RULE_ACTION;


@SuppressWarnings("rawtypes")
public class RulesList extends ArrayList<AbstractRule> {
	//	private static final String RULES_PACKAGE = "tml.conceptmap.rules.";
	/**
	 * 
	 */
	private static final long serialVersionUID = -4465332670320947713L;
	private static Logger logger = Logger.getLogger(RulesList.class);
	private static RulesList defaultRules = null;

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		for(AbstractRule rule : this) {
			buff.append(rule);
			buff.append("\n");
		}
		return buff.toString();
	}

	public static RulesList getDefaultRules() {
		if(defaultRules == null)
			defaultRules = getDefaultList();

		return defaultRules;
	}

	public static RulesList getDefaultRules(String filename) {
		if(defaultRules == null) {
			defaultRules = getDefaultList(filename);
			logger.debug(defaultRules);
		}

		return defaultRules;
	}

	private static RulesList getDefaultList() {
		String filename = Configuration.getTmlFolder() + "cmm/tml.conceptmap.rules.xml";
		logger.debug(filename);
		return getDefaultList(filename);
	}

	private static RulesList getDefaultList(String filename) {
		return defaultRules();
		/*		InputStream iStream=null;
		try {
			iStream = new FileInputStream(filename);
		} catch (FileNotFoundException e1) {
			logger.error(e1.getMessage());
		}

		Digester digester = new Digester();
		digester.setValidating(false);
		digester.addObjectCreate("rules", RULES_PACKAGE + "RulesList");
		digester.addObjectCreate("rules/erule", RULES_PACKAGE + "EdgeRule");
		digester.addSetProperties("rules/erule");
		digester.addSetNext("rules/erule", "add", RULES_PACKAGE + "EdgeRule");
		digester.addCallMethod("rules/erule/pattern", "addPatternFromString", 0);
		digester.addObjectCreate("rules/vrule", RULES_PACKAGE + "VertexOutgoingEdgesRule");
		digester.addSetProperties("rules/vrule");
		digester.addSetNext("rules/vrule", "add", RULES_PACKAGE + "VertexOutgoingEdgesRule");
		digester.addCallMethod("rules/vrule/pattern", "addPatternFromString", 0);
		digester.addObjectCreate("rules/varule", RULES_PACKAGE + "VertexAllOutgoingEdgesRule");
		digester.addSetProperties("rules/varule");
		digester.addSetNext("rules/varule", "add", RULES_PACKAGE + "VertexAllOutgoingEdgesRule");
		digester.addCallMethod("rules/varule/pattern", "addPatternFromString", 0);
		digester.addObjectCreate("rules/vtrule", RULES_PACKAGE + "VertexThreeOutgoingEdgesRule");
		digester.addSetProperties("rules/vtrule");
		digester.addSetNext("rules/vtrule", "add", RULES_PACKAGE + "VertexThreeOutgoingEdgesRule");
		digester.addCallMethod("rules/vtrule/pattern", "addPatternFromString", 0);
		RulesList rules = null;
		try {
			InputSource source = new InputSource(new FileInputStream(filename));
			source.setEncoding("UTF-8");
			rules = (RulesList) digester.parse(source);
			Collections.sort(rules, new AbstractRule.AbstractRuleComparator());
			logger.debug(rules);
			logger.debug("Default concept map rules loaded");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Couldn't load default concept map rules!");
			logger.error(e);
		}

		return rules;*/
	}

	private static RulesList defaultRules() {
		RulesList rlist = new RulesList();
		EdgeRule e = new EdgeRule();
		e.setName("Compound Conjunctions");
		e.setOrder(5);
		e.setAction(RULE_ACTION.MERGE_VERTICES_INFIX);
		e.setSingleEdge(true);
		e.setInfix("conj_(.*)");
		e.setPositionDifference(2);
		e.setLeftPOS("NN.*");
		e.setRightPOS("NN.*");
		e.addPattern("conj_and");
		rlist.add(e);
		e = new EdgeRule();
		e.setName("Compound Preposition");
		e.setOrder(6);
		e.setAction(RULE_ACTION.MERGE_VERTICES_INFIX);
		e.setSingleEdge(false);
		e.setInfix("prep_(.*)");
		e.setPositionDifference(2);
		e.setLeftPOS("NN.*");
		e.setRightPOS("NN.*");
		e.addPattern("prep_of");
		rlist.add(e);
		e = new EdgeRule();
		e.setName("Compound Nouns");
		e.setOrder(10);
		e.setAction(RULE_ACTION.MERGE_VERTICES);
		e.setPositionDifference(1);
		e.addPattern("amod");
		e.addPattern("nn");
		e.addPattern("number");
		e.addPattern("num");
		rlist.add(e);
		e = new EdgeRule();
		e.setName("Negated Verbs");
		e.setOrder(14);
		e.setAction(RULE_ACTION.MERGE_VERTICES);
		e.setPositionDifference(1);
		e.addPattern("neg");
		rlist.add(e);
		e = new EdgeRule();
		e.setName("Compound Verbs");
		e.setOrder(15);
		e.setAction(RULE_ACTION.MERGE_VERTICES);
		e.setPositionDifference(1);
		e.setLeftPOS("VB.*");
		e.setRightPOS("VB.*");
		e.addPattern("advmod");
		e.addPattern("aux");
		e.addPattern("auxpass");
		rlist.add(e);
		e = new EdgeRule();
		e.setName("Remove determiners");
		e.setOrder(20);
		e.setAction(RULE_ACTION.SUBDUE_TARGET);
		e.setPositionDifference(1);
		e.addPattern("det");
		rlist.add(e);
		return rlist;
	}
}
