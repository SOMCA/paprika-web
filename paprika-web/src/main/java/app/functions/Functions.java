package app.functions;

import org.neo4j.driver.v1.Session;
import app.application.PaprikaWebMain;
import app.utils.neo4j.Graph;

/**
 * Functions is a abstract class who are used when you need have a session.
 * 
 * @author guillaume
 *
 */
public abstract class Functions {
	protected Graph graph;
	protected Session session;

	/**
	 * Create a new Graph() and a new session.
	 */
	public Functions() {
		this.graph = new Graph();
		this.session = PaprikaWebMain.getSession();
	}
	

	/**
	 * @return a session
	 */
	public Session getSession(){
		return this.session;
	}
	/**
	 * @return a graph
	 */
	public Graph getGraph(){
		return this.graph;
	}
}
