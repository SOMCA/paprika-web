package spoon.utils.neo4j;

import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.Node;

/**
 * 
 * @author guillaume willefert Graph is a class who contains many method for
 *         create a correct command String
 * 
 */
public class Graph {

	private static final String CREATEIT = "  CREATE (it)-[:";
	private static final String RETURN = " RETURN ";

	/**
	 * Create a node with the label and with this parameter
	 * 
	 * @param lowNode
	 * @return
	 */
	public String create(LowNode lowNode) {
		/* créer une donnée */
		return "CREATE (" + PaprikaKeyWords.NAMELABEL + ":" + lowNode.getLabel() + lowNode.parametertoData() + ")"
				+ Graph.RETURN + PaprikaKeyWords.NAMELABEL;
	}

	/**
	 * Récupère l'id du node du premier record
	 * 
	 * @param result
	 * @return
	 */
	public long getID(StatementResult result, String labelNode) {
		if (result!=null && result.hasNext() && labelNode!=null) {
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
	 * @return
	 */
	public String relation(LowNode lowNode, LowNode lowNodeTarget, String relationLabel) {
		return matchPrefabs("it", lowNode) + matchPrefabs(PaprikaKeyWords.NAMELABEL, lowNodeTarget) + Graph.CREATEIT
				+ relationLabel + "]->(" + PaprikaKeyWords.NAMELABEL + ")";
	}

	/**
	 * Return a command who Create a relation between two nodes who exist.
	 * 
	 * @param lowNode
	 * @param lowNodeTarget
	 * @param relationLabel
	 * @return
	 */
	public String relation(LowNode lowNode, LowNode lowNodeTarget, LowNode lowNodeRelation) {
		return matchPrefabs("it", lowNode) + matchPrefabs(PaprikaKeyWords.NAMELABEL, lowNodeTarget) + Graph.CREATEIT
				+ lowNodeRelation.getLabel() + lowNodeRelation.parametertoData() + "]->(" + PaprikaKeyWords.NAMELABEL
				+ ")";
	}

	/**
	 * Return a command who Create a relation between two nodes, where the left
	 * node already exist
	 * 
	 * @param lowNode
	 * @param lowNodeTarget
	 * @param relationLabel
	 * @return
	 */
	public String relationcreateRight(LowNode lowNode, LowNode lowNodeTarget, String relationLabel) {
		return matchPrefabs("it", lowNode) + Graph.CREATEIT + relationLabel + "]->(" + PaprikaKeyWords.NAMELABEL + ":"
				+ lowNodeTarget.getLabel() + lowNodeTarget.parametertoData() + ")";
	}

	/**
	 * Utilisez pour toutes les fonctions, si le premier paramètre est labelID
	 * alors il n'y a pas d'autres paramètres à part l'id et on renvoie un match
	 * where id. Sinon, on applique un match avec l'ensemble des paramètres.
	 * 
	 * @param labelname
	 * @param lowNode
	 * @return
	 */

	public String matchPrefabs(String labelname, LowNode lowNode) {
		return " MATCH (" + labelname + ":" + lowNode.getLabel() + lowNode.parametertoData() + ") "
				+ lowNode.idfocus(labelname);
	}

	/**
	 * Return a command for clean all database
	 *
	 * private String deleteAll() { return "MATCH (n) DETACH DELETE n"; }
	 */

	/**
	 * renvoie une commande qui retourne les nodes d'un label avec pour
	 * conditions, parameter.
	 * 
	 * @param lowNode
	 * @return
	 */

	public String matchSee(LowNode lowNode) {
		return matchPrefabs(PaprikaKeyWords.NAMELABEL, lowNode) + Graph.RETURN + PaprikaKeyWords.NAMELABEL;
	}

	/**
	 * Retourne une commande qui retourne tous les nodes du node donné en
	 * paramètre lié à leur labelRelation.
	 * 
	 * @param lowNode
	 * @param lowNodeTarget
	 * @param relationLabel
	 * @return
	 */
	public String matchSee(LowNode lowNode, LowNode lowNodeTarget, String relationLabel) {
		return matchPrefabs("a", lowNode) + matchPrefabs(PaprikaKeyWords.NAMELABEL, lowNodeTarget) + " MATCH (a)-[:"
				+ relationLabel + "]->(" + PaprikaKeyWords.NAMELABEL + ")" + Graph.RETURN + PaprikaKeyWords.NAMELABEL;
	}

	/**
	 * parameter doit contenir toujours id, pour que set fonctionne, set
	 * applique ensuite le reste des paramètres au node.
	 * 
	 * @param lowNode
	 * @param newAttributsNode
	 * @return
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

	/**
	 * Supprime une donnée de la base de donnée et tous ces enfants et petits
	 * enfants.
	 * 
	 * @param lowNode
	 * @return
	 */
	public String deleteDataAndAllChildrends(LowNode lowNode) {
		return this.matchPrefabs("n", lowNode) + " MATCH (n)-[*]->(a)" + " DETACH DELETE n,a";
	}

}
