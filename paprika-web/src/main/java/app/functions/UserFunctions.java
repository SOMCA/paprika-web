package app.functions;


import org.neo4j.driver.v1.Record;

import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.types.Node;


import app.model.User;
import app.utils.PaprikaKeyWords;

import app.utils.neo4j.LowNode;

public class UserFunctions extends Functions {

	/**
	 * Return the salt of the first node of label "key" If not found, return
	 * null
	 * 
	 * @param user
	 * @return
	 */
	public String retrieveSalt() {

		try (Transaction tx = this.session.beginTransaction()) {

			StatementResult result = tx.run(this.graph.matchSee(new LowNode(PaprikaKeyWords.LABELKEY)));

			if (result.hasNext()) {
				Record record = result.next();
				Node node = record.get(PaprikaKeyWords.NAMELABEL).asNode();
				System.out.println(node.toString());
				return node.get(PaprikaKeyWords.ATTRIBUTE_SALT).asString();

			}

			tx.success();
		}
		return null;
	}

	/**
	 * readData prend un string email et regarde si il y a un utilisateur ayant
	 * le même email, il regarde d'abord la clé et regarde si cette utilisateur
	 * existe et est lié à la clé. Sinon, readData renvoie null ce qui inclut
	 * donc qu'aucun utilisateur de cette email existe, sinon, il renvoit un
	 * User qui contient l'email, le hashcode du mot de passe et le hashcode de
	 * la clé.
	 * 
	 * @param email
	 * @return
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

				String hashpwd = node.get(PaprikaKeyWords.ATTRIBUTE_PWD).asString();
				long id = node.id();
				return new User(email, id, hashpwd);
			}
		}

		return null;
	}

	/**
	 * Writedata prend l'email et le newhashedpassword et crée l'utilisateur en
	 * plus d'être relié à une tel clé. Il inclut aussi le nombre d'application
	 * qui commence donc par 0.
	 * 
	 * @param email
	 * @param newHashedPassword
	 */
	public void writeUser(String email, String newHashedPassword) {
		LowNode nodeUser = new LowNode(PaprikaKeyWords.LABELUSER);
		nodeUser.addParameter(PaprikaKeyWords.ATTRIBUTE_EMAIL, email);
		nodeUser.addParameter(PaprikaKeyWords.ATTRIBUTE_PWD, newHashedPassword);
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
