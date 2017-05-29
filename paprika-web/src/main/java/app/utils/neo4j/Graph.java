package app.utils.neo4j;

import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.Node;

import app.utils.PaprikaKeyWords;

/**
 * Graph is a class who contains many method for create a correct command String
 *
 * @author guillaume
 * 
 */
public class Graph {

	private static final String CREATEIT = "  CREATE (it)-[:";
	private static final String RETURN = " RETURN ";
	private static final String RETURNZ = ") RETURN ";

	/**
	 * Create a node with a custom lowNode
	 * 
	 * @param lowNode
	 * @return a command String for Neo4j Cypher
	 */
	public String create(LowNode lowNode) {
		/* créer une donnée */
		return "CREATE (" + PaprikaKeyWords.NAMELABEL + ":" + lowNode.getLabel() + lowNode.parametertoData() + Graph.RETURNZ + PaprikaKeyWords.NAMELABEL;
	}

	/**
	 * Found the first id node of the list received per Neo4j with the labelNode
	 * 
	 * @param result
	 * @param labelNode
	 *            the label node when you search
	 * @return the id of the first node encounter
	 */
	public long getID(StatementResult result, String labelNode) {
		if (result != null && result.hasNext() && labelNode != null) {
			Record record = result.next();
			Node node = record.get(labelNode).asNode();
			if (node != null) {
				return node.id();
			}
		}
		return -1;

	}

	/**
	 * Return a command who Create a relation between two nodes who exist.
	 * 
	 * @param lowNode
	 * @param lowNodeTarget
	 * @param relationLabel
	 * @return a command String for Neo4j Cypher
	 */

	public String relation(LowNode lowNode, LowNode lowNodeTarget, String relationLabel) {
		if (relationLabel == null)
			relationLabel = "*";
		return matchPrefabs("it", lowNode) + matchPrefabs(PaprikaKeyWords.NAMELABEL, lowNodeTarget) + Graph.CREATEIT
				+ relationLabel + "]->(" + PaprikaKeyWords.NAMELABEL + ")";
	}

	/**
	 * Return a command who Create a relation between two nodes, where the left
	 * node already exist
	 * 
	 * @param lowNode
	 * @param lowNodeTarget
	 * @param relationLabel
	 * @return a command String for Neo4j Cypher
	 */
	public String relationcreateRight(LowNode lowNode, LowNode lowNodeTarget, String relationLabel) {
		if (relationLabel == null)
			relationLabel = "*";
		return matchPrefabs("it", lowNode) + Graph.CREATEIT + relationLabel + "]->(" + PaprikaKeyWords.NAMELABEL + ":"
				+ lowNodeTarget.getLabel() + lowNodeTarget.parametertoData() + ")";
	}

	/**
	 * Transform a LowNode with a labelname on MATCH String.
	 * 
	 * @param labelname
	 * @param lowNode
	 * @return a command String for Neo4j Cypher
	 */

	public String matchPrefabs(String labelname, LowNode lowNode) {
		String before = " MATCH (" + labelname;
		String after = lowNode.parametertoData() + ") ";
		if(lowNode.getID()!=-1){
			after+=" WHERE ID(" + labelname + ") = " + Long.toString(lowNode.getID());
		}
		
		if (lowNode.getLabel() == null) {
			return before + after;
		}
		return before + ":" + lowNode.getLabel() + after;
	}

	/**
	 * return the match of the lownode.
	 * 
	 * @param lowNode
	 * @return a command String for Neo4j Cypher
	 */

	public String matchSee(LowNode lowNode) {
		return matchPrefabs(PaprikaKeyWords.NAMELABEL, lowNode) + Graph.RETURN + PaprikaKeyWords.NAMELABEL;
	}

	/**
	 * Return the match who contains all childrens of the first node, linked to
	 * the relation. With using two lowNode who contains parameter.
	 * 
	 * @param lowNode
	 * @param lowNodeTarget
	 * @param relationLabel
	 * @return a command String for Neo4j Cypher
	 */
	public String matchSee(LowNode lowNode, LowNode lowNodeTarget, String relationLabel) {
		if (relationLabel == null)
			relationLabel = "*";
		return matchPrefabs("a", lowNode) + matchPrefabs(PaprikaKeyWords.NAMELABEL, lowNodeTarget) + " MATCH (a)-[:"
				+ relationLabel + "]->(" + PaprikaKeyWords.NAMELABEL + Graph.RETURNZ + PaprikaKeyWords.NAMELABEL;
	}

	/**
	 * Return the match who contains all childrens of the first node, linked to
	 * the relation of the label of the first node and labelchildren for each childrens.
	 * 
	 * If the relationLabel is null, so take all childrens without check the relation.
	 * If labelChildren is null, so search on all childrens.
	 * 
	 * @param label 
	 * @param labelChildren 
	 * @param relationLabel 
	 * 
	 * @return a command String for Neo4j Cypher
	 */
	public String matchSee(String label, String labelChildren, String relationLabel) {
		if (relationLabel == null)
			relationLabel = "*";
			
		String children="";
		if(labelChildren!=null)
		   children=":" + labelChildren;
		
		return "MATCH (d:"+label+")-[:"+relationLabel+"]->("+PaprikaKeyWords.NAMELABEL + children + Graph.RETURNZ+ PaprikaKeyWords.NAMELABEL;
	}

	
	
	
	/**
	 * 
	 * Set apply the parameter of the second LowNode on the first.
	 * 
	 * @param lowNode
	 * @param newAttributsNode
	 * @return a command String for Neo4j Cypher
	 */
	public String set(LowNode lowNode, LowNode newAttributsNode) {
		String result = this.matchPrefabs(PaprikaKeyWords.NAMELABEL, lowNode);
		StringBuilder str = newAttributsNode.parametertoData();
		if (str.length() != 0) {
			result += " SET " + PaprikaKeyWords.NAMELABEL + "+=" + str;
		}
		result += Graph.RETURN + PaprikaKeyWords.NAMELABEL;
		return result;
	}
	

}
