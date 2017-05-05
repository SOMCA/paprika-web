package app.functions;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.types.Node;
import app.utils.PaprikaKeyWords;
import app.utils.neo4j.LowNode;

public class ApplicationFunctions extends Functions {


	/**
	 * Retrouve l'id de l'application ciblé
	 * 
	 * @param email
	 * @param application
	 * @return
	 */
	public long receiveIDOfApplication(String email, String application) {
		LowNode nodeUser = new LowNode(PaprikaKeyWords.LABELUSER);
		nodeUser.addParameter(PaprikaKeyWords.ATTRIBUTE_EMAIL, email);

		LowNode nodeApp = new LowNode(PaprikaKeyWords.LABELPROJECT);
		nodeApp.addParameter(PaprikaKeyWords.NAMEATTRIBUTE, application);

		return this.receiveIDOfApplication(nodeUser, nodeApp);
	}


	/**
	 * Retrouve l'id de l'application ciblé
	 * 
	 * @param email
	 * @param application
	 * @return
	 */
	public long receiveIDOfApplication(LowNode nodeUser, LowNode nodeApp) {
		StatementResult result;
		try (Transaction tx = this.session.beginTransaction()) {
			result = tx.run(graph.matchSee(nodeUser, nodeApp, PaprikaKeyWords.REL_USER_PROJECT));
			tx.success();
		}
		return this.graph.getID(result,PaprikaKeyWords.NAMELABEL);
	}

	/**
	 * Prend un node application ou version, et incrémente de 1 à l'attribut en
	 * prenant en compte qu'il s'agit d'un long
	 * 
	 * @param label
	 * @param parameter
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
	 * Enregistre une application dans l'utilisateur ciblé.
	 * 
	 * @param email
	 * @param application
	 */
	public void writeApplicationOnUser(String email, String application) {

		StatementResult result;
		Record record;
		Node node;

		LowNode nodeUser = new LowNode(PaprikaKeyWords.LABELUSER);
		nodeUser.addParameter(PaprikaKeyWords.ATTRIBUTE_EMAIL, email);

		// Incrémente le nombre d'applications dans l'utilisateur:
		try (Transaction tx = this.session.beginTransaction()) {

			result = tx.run(graph.matchSee(nodeUser));
			record = result.next();
			node = record.get(PaprikaKeyWords.NAMELABEL).asNode();
			System.out.println(node.get(PaprikaKeyWords.ATTRIBUTE_NB_APP));
			LowNode nodeApp = new LowNode(PaprikaKeyWords.LABELPROJECT);
			nodeApp.addParameter(PaprikaKeyWords.NAMEATTRIBUTE, application);
			nodeApp.addParameter(PaprikaKeyWords.ATTRIBUTE_NB_VERSION, 0);
			String incr = this.increment(nodeUser, PaprikaKeyWords.ATTRIBUTE_NB_APP,
					node.get(PaprikaKeyWords.ATTRIBUTE_NB_APP).asLong(), tx);
			nodeApp.addParameter(PaprikaKeyWords.ORDER, incr);

			// Créer une application node:
			result = tx.run(graph.create(nodeApp));
			long id = this.graph.getID(result, PaprikaKeyWords.NAMELABEL);
			// Récupère son id:
			nodeApp.setId(id);

			tx.run(graph.relation(nodeUser, nodeApp, PaprikaKeyWords.REL_USER_PROJECT));
			tx.success();
		}
	}
}
