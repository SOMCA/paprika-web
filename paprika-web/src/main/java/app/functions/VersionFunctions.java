package app.functions;

import org.neo4j.driver.v1.Record;

import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;

import app.application.PaprikaFacade;
import app.model.Version;
import app.utils.PaprikaKeyWords;

import app.utils.neo4j.LowNode;

/**
 * VersionFunctions is a utils class linked to Version class but use neo4j
 * 
 * @author guillaume
 *
 */
public class VersionFunctions extends Functions {

	private static final String REL_VERSION_CODESMELLS = "EXHIBITS";
	private static final String LABELQUERY = "CodeSmells";
	private static final String REL = " MATCH (n)-[:" + REL_VERSION_CODESMELLS + "]->(target:" + LABELQUERY;

	/**
	 * Add 1 to a property on the project of the version
	 * @param lowNode
	 * @param attribute
	 * @param value
	 * @param tx
	 * @return
	 */
	private String increment(LowNode lowNode, String attribute, long value, Transaction tx) {

		long size = value;
		size += 1;
		LowNode nodePut = new LowNode(PaprikaKeyWords.LABELUSER);
		nodePut.addParameter(attribute, size);

		tx.run(graph.set(lowNode, nodePut));
		return Long.toString(size);
	}

	/**
	 * 
	 * Write a new version on the project
	 * 
	 * @param idProject
	 *            id of the Project
	 * @param version
	 *            name of the Version
	 * @return a Version LowNode who contains many parameters.
	 * 
	 */
	public LowNode writeVersion(long idProject, String version) {

		StatementResult result;
		Record record;
		Node node;

		LowNode nodeApp = new LowNode(PaprikaKeyWords.LABELPROJECT);
		nodeApp.setId(idProject);

		// Incrémente le nombre de versions dans l'application:
		LowNode nodeVer;
		try (Transaction tx = this.session.beginTransaction()) {

			result = tx.run(graph.matchSee(nodeApp));
			record = result.next();
			node = record.get(PaprikaKeyWords.NAMELABEL).asNode();
			nodeVer = new LowNode(PaprikaKeyWords.VERSIONLABEL);
			nodeVer.addParameter(PaprikaKeyWords.NAMEATTRIBUTE, version);
			nodeVer.addParameter(PaprikaKeyWords.ORDER, this.increment(nodeApp, PaprikaKeyWords.ATTRIBUTE_NB_VERSION,
					node.get(PaprikaKeyWords.ATTRIBUTE_NB_VERSION).asLong(), tx));
			// Créer une version node:
			result = tx.run(graph.create(nodeVer));
			long id = this.graph.getID(result, PaprikaKeyWords.NAMELABEL);
			nodeVer.setId(id);

			tx.run(graph.relation(nodeApp, nodeVer, PaprikaKeyWords.REL_PROJECT_VERSION));
			tx.success();
		}
		return nodeVer;
	}

	/**
	 * Return all code smells of the version on a result.
	 * 
	 * @param version
	 *            a version with a correct id.
	 * @return a result who can contains many codesmells or be empty
	 */
	public StatementResult loadDataCodeSmell(Version version) {
		StatementResult result;
		try (Transaction tx = this.session.beginTransaction()) {
			result = tx.run("MATCH (ver:" + PaprikaKeyWords.VERSIONLABEL + ") WHERE ID(ver)=" + version.getID()
					+ " MATCH (ver)-[:" + REL_VERSION_CODESMELLS
					+ "]->(codesmells) MATCH (codesmells)-[:RESULT]->(target) RETURN target ");
			tx.success();
		}
		return result;

	}

	/**
	 * Return the id of the Version with the name of the version and the id
	 * Project. Return -1 if not found.
	 * 
	 * @param idProject
	 *            id of the project
	 * @param version
	 *            name of the version.
	 * @return the id of the version.
	 */
	public long receiveIDOfVersion(long idProject, String version) {
		LowNode nodeApp = new LowNode(PaprikaKeyWords.LABELPROJECT);
		nodeApp.setId(idProject);
		LowNode nodeVer = new LowNode(PaprikaKeyWords.VERSIONLABEL);
		nodeVer.addParameter(PaprikaKeyWords.NAMEATTRIBUTE, version);

		StatementResult result;
		try (Transaction tx = this.session.beginTransaction()) {
			result = tx.run(graph.matchSee(nodeApp, nodeVer, PaprikaKeyWords.REL_PROJECT_VERSION));
			tx.success();
		}
		if (result.hasNext()) {
			return this.graph.getID(result, PaprikaKeyWords.NAMELABEL);
		}

		return -1;
	}

	/**
	 * Return the number of code smells of the version of the parameter 'id'.
	 * 
	 * @param id id of the version
	 * @return the number of codesmells of the version
	 */
	public long getNumberOfSmells(long id) {
		StatementResult result;
		try (Transaction tx = this.session.beginTransaction()) {
			LowNode node = new LowNode(PaprikaKeyWords.VERSIONLABEL);
			node.setId(id);
			result = tx.run(this.graph.matchPrefabs("n", node) + REL + " return target.number");
			tx.success();
		}
		if (result.hasNext()) {
			Value value = result.next().get("number");
			if (value != null && !value.isNull()) {
				return value.asLong();
			}
		}
		return 0;
	}

	/**
	 * Apply the number of smells on the node of parameter id.
	 * The node is normally a code smell node.
	 * 
	 * @param id
	 * @param number
	 */
	public void applyNumberOfCodeSmells(long id, long number) {

		try (Transaction tx = this.session.beginTransaction()) {
			LowNode node = new LowNode(PaprikaKeyWords.VERSIONLABEL);
			node.setId(id);
			tx.run(this.graph.matchPrefabs("n", node) + REL + ") set target.number=" + Long.toString(number));
			tx.success();
		}

	}

	/**
	 * Return the order of the version, for know if the version is on the good order.
	 * 
	 * @param id of the version.
	 * @return a number
	 */
	public long getOrder(long id) {
		StatementResult result;
		try (Transaction tx = this.session.beginTransaction()) {
			LowNode node = new LowNode(PaprikaKeyWords.VERSIONLABEL);
			node.setId(id);
			result = tx.run(this.graph.matchPrefabs("n", node) + " return n." + PaprikaKeyWords.ORDER);
			tx.success();
		}
		if (result.hasNext()) {
			Value value = result.next().get(PaprikaKeyWords.ORDER);
			if (value != null && !value.isNull()) {
				return value.asLong();
			}
		}
		return 0;
	}


	/**
	 * Return the name of the id of a unknow node if the node exist and than a name exist.
	 * @param idnode
	 * @return a name.
	 */
	public String receiveOf(long idnode) {
		return PaprikaFacade.getInstance().getParameter(idnode, PaprikaKeyWords.NAMEATTRIBUTE);
	}

}
