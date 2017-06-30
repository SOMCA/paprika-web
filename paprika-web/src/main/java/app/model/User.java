package app.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.neo4j.driver.v1.Record;

import org.neo4j.driver.v1.types.Node;

import app.application.PaprikaFacade;
import app.application.PaprikaWebMain;
import app.functions.UserFunctions;
import app.utils.PaprikaKeyWords;
import app.utils.neo4j.LowNode;

/**
 * User is the current User connected, who contains many methods used on velocity
 * @author guillaume
 *
 */
public class User extends Entity{
	protected final String hashedPassword;
	protected final boolean active;

	/**
	 * 
	 * @param email name of the User
	 * @param id id of the User
	 * @param hashedPassword hidden password of the User
	 * @param active if true, so the user can be connected, else, cannot. Used for the login.
	 */
	public User(String email,long id, String hashedPassword,boolean active) {
		super(email,id);
		this.active=active;
		this.hashedPassword = hashedPassword;
		PaprikaFacade facade = PaprikaFacade.getInstance();
		LowNode lownode= new LowNode(PaprikaKeyWords.LABELUSER);
		lownode.addParameter(PaprikaKeyWords.ATTRIBUTE_EMAIL, email);
        List<Record> bigdata = facade.loadChildrenOfNode(lownode,  PaprikaKeyWords.REL_USER_PROJECT, PaprikaKeyWords.LABELPROJECT);
		if (bigdata == null || bigdata.isEmpty()) {
			PaprikaWebMain.LOGGER.trace(email);
			UserFunctions userFct= new UserFunctions();
			//long idproject=facade.addProject(this, "Example");
			userFct.writeExample(this.getName());
		}
	}

	/**
	 * Return the hashed password
	 * @return the hashed password
	 */
	public String getHashedPassword() {
		return this.hashedPassword;
	}
	/**
	 * Return the current state of the user.
	 * @return Return true if the account have be enabled.
	 */
	public boolean getActive() {
		return this.active;
	}

	/**
	 * Return the name of the email without string after '@'
	 * @return the first subpart of the email
	 */
	public String getUsername() {
		return this.getName().split("@")[0];
	}

	
	
	
	/**
	 * Return the iterator of the project list of the User
	 * 
	 * @return all project of the user
	 */
	public Iterator<Project> getDataProjects() {
		List<Project> projects = new ArrayList<>();
		Record record;
		String name;

		PaprikaFacade facade = PaprikaFacade.getInstance();
		LowNode lownode= new LowNode(PaprikaKeyWords.LABELUSER);
		lownode.addParameter(PaprikaKeyWords.ATTRIBUTE_EMAIL, this.getName());
        List<Record> bigdata = facade.loadChildrenOfNode(lownode,  PaprikaKeyWords.REL_USER_PROJECT, PaprikaKeyWords.LABELPROJECT);
		
		if (bigdata == null)
			return projects.iterator();

		Iterator<Record> iter = bigdata.iterator();
		Node node;
		while (iter.hasNext()) {
			record = iter.next();
			node = record.get(PaprikaKeyWords.NAMELABEL).asNode();

			name = node.get(PaprikaKeyWords.NAMEATTRIBUTE).asString();
			projects.add(new Project(name,node.id()));
		}
		return projects.iterator();
	}
}
