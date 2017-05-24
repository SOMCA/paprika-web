package app.functions;


import org.neo4j.driver.v1.Record;

import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.types.Node;

import app.application.PaprikaWebMain;
import app.model.User;
import app.utils.PaprikaKeyWords;

import app.utils.neo4j.LowNode;
/**
 * UserFunctions is a utils class linked to User class but use neo4j
 * @author guillaume
 *
 */
public class UserFunctions extends Functions {
	private static final String ATTRIBUTE_KEY = "hashpwd";
		/**
	 * Return the salt of the first node of label "key" If not found, return
	 * null
	 * @return a salt code
	 */
	public String retrieveSalt() {

		try (Transaction tx = this.session.beginTransaction()) {

			StatementResult result = tx.run(this.graph.matchSee(new LowNode(PaprikaKeyWords.LABELKEY)));

			if (result.hasNext()) {
				Record record = result.next();
				Node node = record.get(PaprikaKeyWords.NAMELABEL).asNode();
				PaprikaWebMain.LOGGER.trace(node.toString());
				return node.get(PaprikaKeyWords.ATTRIBUTE_SALT).asString();

			}

			tx.success();
		}
		return null;
	}

	/**
	 * Return a User if the email(who is unique) correspond to a email on the neo4j database.
	 * Else null.
	 * 
	 * @param email email of a user.
	 * @return a User of the email
	 */
	public User foundUser(String email) {
		StatementResult result;
		Record record;
		Node node;

		LowNode nodeUser = new LowNode(PaprikaKeyWords.LABELUSER);
		nodeUser.addParameter(PaprikaKeyWords.ATTRIBUTE_EMAIL, email);
		LowNode nodeKey = new LowNode(PaprikaKeyWords.LABELKEY);
		nodeKey.addParameter(PaprikaKeyWords.ATTRIBUTE_SALT, this.retrieveSalt());
		try (Transaction tx = this.session.beginTransaction()) {

			result = tx.run(this.graph.matchSee(nodeKey, nodeUser, PaprikaKeyWords.REL_USER_PROJECT));
			tx.success();
		}
		while (result.hasNext()) {

			record = result.next();

			node = record.get(PaprikaKeyWords.NAMELABEL).asNode();

			if (node.get(PaprikaKeyWords.ATTRIBUTE_EMAIL).asString().equals(email)) {

				String hashpwd = node.get(ATTRIBUTE_KEY).asString();
				long id = node.id();
				return new User(email, id, hashpwd);
			}
		}

		return null;
	}

	/**
	 * Create a new User with the email and the hashed password
	 * 
	 * @param email
	 * @param newHashedPassword
	 */
	public void writeUser(String email, String newHashedPassword) {
		LowNode nodeUser = new LowNode(PaprikaKeyWords.LABELUSER);
		nodeUser.addParameter(PaprikaKeyWords.ATTRIBUTE_EMAIL, email);
		nodeUser.addParameter(ATTRIBUTE_KEY, newHashedPassword);
		nodeUser.addParameter(PaprikaKeyWords.ATTRIBUTE_NB_APP, 0);

		LowNode nodeKey = new LowNode(PaprikaKeyWords.LABELKEY);
		nodeKey.addParameter(PaprikaKeyWords.ATTRIBUTE_SALT, this.retrieveSalt());
		try (Transaction tx = this.session.beginTransaction()) {
			tx.run(graph.create(nodeUser));
			tx.run(graph.relation(nodeKey, nodeUser, PaprikaKeyWords.REL_USER_PROJECT));
			tx.success();
		}
	}
	

}
