package app.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.neo4j.driver.v1.Record;

import org.neo4j.driver.v1.types.Node;

import app.application.PaprikaFacade;
import app.functions.ApplicationFunctions;
import app.utils.PaprikaKeyWords;
import app.utils.neo4j.LowNode;

public class User extends Entity{
	protected final String hashedPassword;

	public User(String email,long id, String hashedPassword) {
		super(email,id);
		this.hashedPassword = hashedPassword;
		PaprikaFacade facade = PaprikaFacade.getInstance();
		LowNode lownode= new LowNode(PaprikaKeyWords.LABELUSER);
		lownode.addParameter(PaprikaKeyWords.ATTRIBUTE_EMAIL, email);
        List<Record> bigdata = facade.loadChildrenOfNode(lownode,  PaprikaKeyWords.REL_USER_PROJECT, PaprikaKeyWords.LABELPROJECT);
		if (bigdata == null || bigdata.isEmpty()) {
			System.out.println(email);
		
			for (int i = 0; i < 3; i++)
				facade.addProject(this, "Example_" + i);


		}
	}

	public String getHashedPassword() {
		return this.hashedPassword;
	}


	public String getUsername() {
		return this.getName().split("@")[0];
	}

	
	
	
	public Iterator<String> getDataApplications() {
		List<String> applications = new ArrayList<>();
		Record record;
		String name;

		PaprikaFacade facade = PaprikaFacade.getInstance();
		LowNode lownode= new LowNode(PaprikaKeyWords.LABELUSER);
		lownode.addParameter(PaprikaKeyWords.ATTRIBUTE_EMAIL, this.getName());
        List<Record> bigdata = facade.loadChildrenOfNode(lownode,  PaprikaKeyWords.REL_USER_PROJECT, PaprikaKeyWords.LABELPROJECT);
		
		if (bigdata == null)
			return applications.iterator();

		Iterator<Record> iter = bigdata.iterator();
		Node node;
		while (iter.hasNext()) {
			record = iter.next();
			node = record.get(PaprikaKeyWords.NAMELABEL).asNode();

			name = node.get(PaprikaKeyWords.NAMEATTRIBUTE).asString();
			applications.add(name);
		}
		return applications.iterator();

	}

	/**
	 * Renvoie la liste des versions de l'application
	 * @param application
	 * @return
	 */
	
	public List<String> getListVersionApplications(String application) {
		List<String> applications = new ArrayList<>();
		Record record;
		String name;
		PaprikaFacade facade = PaprikaFacade.getInstance();
		LowNode lownode= new LowNode(PaprikaKeyWords.LABELPROJECT);
		lownode.setId(new ApplicationFunctions().receiveIDOfApplication(this.getName(), application));
        List<Record> bigdata = facade.loadChildrenOfNode(lownode,  PaprikaKeyWords.REL_PROJECT_VERSION, PaprikaKeyWords.VERSIONLABEL);
		
		Iterator<Record> iter = bigdata.iterator();
		Node node;
		while (iter.hasNext()) {
			record = iter.next();
			node = record.get(PaprikaKeyWords.NAMELABEL).asNode();

			name = node.get(PaprikaKeyWords.NAMEATTRIBUTE).asString();
			applications.add(name);
		}
		return applications;
	}

	public Iterator<String> getVersionApplications(String application) {
		return getListVersionApplications(application).iterator();
	}
	
	


}
