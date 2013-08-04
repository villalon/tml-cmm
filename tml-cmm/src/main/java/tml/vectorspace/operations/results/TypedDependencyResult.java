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
package tml.vectorspace.operations.results;

/**
 * A typed dependency from the Stanford parser.
 * 
 * @author Jorge Villalon
 *
 */
public class TypedDependencyResult extends AbstractResult {

	String nodeA;
	String nodeB;
	String linkingWord;
	int sentenceNumber;
	String nodeAPOS;
	String nodeBPOS;
	int nodeAPosition;
	int nodeBPosition;
	String sentenceId;

	/**
	 * @return the sentenceId
	 */
	public String getSentenceId() {
		return sentenceId;
	}
	/**
	 * @param sentenceId the sentenceId to set
	 */
	public void setSentenceId(String sentenceId) {
		this.sentenceId = sentenceId;
	}
	/**
	 * @return the nodeAPOS
	 */
	public String getNodeAPOS() {
		return nodeAPOS;
	}
	/**
	 * @param nodeAPOS the nodeAPOS to set
	 */
	public void setNodeAPOS(String nodeAPOS) {
		this.nodeAPOS = nodeAPOS;
	}
	/**
	 * @return the nodeBPOS
	 */
	public String getNodeBPOS() {
		return nodeBPOS;
	}
	/**
	 * @param nodeBPOS the nodeBPOS to set
	 */
	public void setNodeBPOS(String nodeBPOS) {
		this.nodeBPOS = nodeBPOS;
	}
	/**
	 * @return the nodeAPosition
	 */
	public int getNodeAPosition() {
		return nodeAPosition;
	}
	/**
	 * @param nodeAPosition the nodeAPosition to set
	 */
	public void setNodeAPosition(int nodeAPosition) {
		this.nodeAPosition = nodeAPosition;
	}
	/**
	 * @return the nodeBPosition
	 */
	public int getNodeBPosition() {
		return nodeBPosition;
	}
	/**
	 * @param nodeBPosition the nodeBPosition to set
	 */
	public void setNodeBPosition(int nodeBPosition) {
		this.nodeBPosition = nodeBPosition;
	}
	/**
	 * @return the sentenceNumber
	 */
	public int getSentenceNumber() {
		return sentenceNumber;
	}
	/**
	 * @param sentenceNumber the sentenceNumber to set
	 */
	public void setSentenceNumber(int sentenceNumber) {
		this.sentenceNumber = sentenceNumber;
	}
	/**
	 * @return the nodeA
	 */
	public String getNodeA() {
		return nodeA;
	}
	/**
	 * @param nodeA the nodeA to set
	 */
	public void setNodeA(String nodeA) {
		this.nodeA = nodeA;
	}
	/**
	 * @return the nodeB
	 */
	public String getNodeB() {
		return nodeB;
	}
	/**
	 * @param nodeB the nodeB to set
	 */
	public void setNodeB(String nodeB) {
		this.nodeB = nodeB;
	}
	/**
	 * @return the linkingWord
	 */
	public String getLinkingWord() {
		return linkingWord;
	}
	/**
	 * @param linkingWord the linkingWord to set
	 */
	public void setLinkingWord(String linkingWord) {
		this.linkingWord = linkingWord;
	}
}
