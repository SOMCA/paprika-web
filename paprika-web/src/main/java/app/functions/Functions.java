package app.functions;

import org.neo4j.driver.v1.Session;

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
	
}
