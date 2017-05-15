package app.functions;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Value;

import app.application.PaprikaWebMain;
import app.utils.neo4j.Graph;

public abstract class Functions {
	protected Graph graph;
	protected Session session;

	public Functions() {
		this.graph = new Graph();
		this.session = PaprikaWebMain.getSession();
	}
	

	public Session getSession(){
		return this.session;
	}
	public Graph getGraph(){
		return this.graph;
	}
	
	/**
	 * Retrouve un string de l'id node
	 * 
	 * @param email
	 * @param application
	 * @return
	 */
	public String receiveNameOf(long idnode, String label) {
		StatementResult result;
		try (Transaction tx = this.session.beginTransaction()) {
			result = tx.run("MATCH (n) where ID(n) = "+Long.toString(idnode)+" return n."+label+" as name");
			tx.success();
		}
		if(result.hasNext()){
			Record record= result.next();
			Value name= record.get("name");
			if(!name.isNull()) return name.asString();
		}
		return null;
	}

}
