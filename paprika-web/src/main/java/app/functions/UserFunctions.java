package app.functions;

import java.util.Random;

import org.mindrot.jbcrypt.BCrypt;
import org.neo4j.driver.v1.Record;

import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;

import app.application.PaprikaWebMain;
import app.model.User;
import app.utils.PaprikaKeyWords;

import app.utils.neo4j.LowNode;

/**
 * UserFunctions is a utils class linked to User class but use neo4j
 * 
 * @author guillaume
 *
 */
public class UserFunctions extends Functions {
	private static final String ATTRIBUTE_KEY = "hashpwd";
	private static final String ATTRIBUTE_ENABLED="enabled";

	/**
	 * Return the salt of the first node of label "key" If not found, return
	 * null
	 * 
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
	 * Return a User if the email(who is unique) correspond to a email on the
	 * neo4j database. Else null.
	 * 
	 * @param email
	 *            email of a user.
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
				
				
			    Value value=node.get(ATTRIBUTE_ENABLED);
			    
			    boolean active=(!value.isNull() && "1".equals(value.asString()));
			    
				User user=new User(email, id, hashpwd,active);
				
				return user;
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

	/**
	 * Create example of version when you create a new User
	 * 
	 * @param idproject
	 * @param version
	 * @param number
	 */
	public void writeExample(String email) {
		String nameProject = "Example";
		long id = -1;

		VersionFunctions verFct = new VersionFunctions();

		StatementResult result;
		Record record;
		Node node;

		LowNode nodeUser = new LowNode(PaprikaKeyWords.LABELUSER);
		nodeUser.addParameter(PaprikaKeyWords.ATTRIBUTE_EMAIL, email);

		try (Transaction tx = this.session.beginTransaction()) {

			result = tx.run(graph.matchSee(nodeUser));
			record = result.next();
			node = record.get(PaprikaKeyWords.NAMELABEL).asNode();
			PaprikaWebMain.LOGGER.trace(node.get(PaprikaKeyWords.ATTRIBUTE_NB_APP));
			LowNode nodeApp = new LowNode(PaprikaKeyWords.LABELPROJECT);
			nodeApp.addParameter(PaprikaKeyWords.NAMEATTRIBUTE, nameProject);
			nodeApp.addParameter(PaprikaKeyWords.EXAMPLE, "true");
			nodeApp.addParameter(PaprikaKeyWords.ATTRIBUTE_NB_VERSION, 0);
			String incr = new ProjectFunctions().increment(nodeUser, PaprikaKeyWords.ATTRIBUTE_NB_APP,
					node.get(PaprikaKeyWords.ATTRIBUTE_NB_APP).asLong(), tx);
			nodeApp.addParameter(PaprikaKeyWords.ORDER, incr);

			// Créer une project node:
			result = tx.run(graph.create(nodeApp));
			id = this.graph.getID(result, PaprikaKeyWords.NAMELABEL);
			// Récupère son id:
			nodeApp.setId(id);

			tx.run(graph.relation(nodeUser, nodeApp, PaprikaKeyWords.REL_USER_PROJECT));

			nodeApp = new LowNode(PaprikaKeyWords.LABELPROJECT);
			nodeApp.setId(id);
			// Incrémente le nombre de versions dans le project:
			LowNode nodeVer;
			for (int i = 0; i < 2; i++) {

				nodeVer = new LowNode(PaprikaKeyWords.VERSIONLABEL);
				nodeVer.addParameter(PaprikaKeyWords.NAMEATTRIBUTE, nameProject + "_" + i);
				nodeVer.addParameter(PaprikaKeyWords.ORDER,
						verFct.increment(nodeApp, PaprikaKeyWords.ATTRIBUTE_NB_VERSION, i, tx));
				nodeVer.addParameter(PaprikaKeyWords.EXAMPLE, "true");

				// Créer une version node:
				result = tx.run(graph.create(nodeVer));
				long idVer = this.graph.getID(result, PaprikaKeyWords.NAMELABEL);
				nodeVer.setId(idVer);

				tx.run(graph.relation(nodeApp, nodeVer, PaprikaKeyWords.REL_PROJECT_VERSION));
			}
			tx.success();
		}

	}

	public String generateRandomActivationCode(String email,boolean reset) {
		Random rand = new Random();
		int numRand;
		char charac;
		String activation = "";

		while (activation.length() < 6) {
			numRand = 48 + rand.nextInt(76);
			if ((numRand > 47 && numRand < 58) || (numRand > 64 && numRand < 91) || (numRand > 96 && numRand < 123)) {
				charac=(char)numRand;
				activation+=charac;
			}
		}
		try (Transaction tx = this.session.beginTransaction()) {
			String command="MATCH(u:"+PaprikaKeyWords.LABELUSER+" {email:\""+email+"\"}) SET u.activation=\""+activation+"\"";

			if(!reset){
				command+=", u."+ATTRIBUTE_ENABLED+"=\"0\"";
			}
			
			
			tx.run(command);
			tx.success();
		}
		

		return activation;
	}
}
