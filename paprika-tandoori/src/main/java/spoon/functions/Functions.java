package spoon.functions;

import org.neo4j.driver.v1.Session;

import neo4jBolt.Graph;
import spoon.main.PaprikaTandooriMain;


public abstract class Functions {
	protected Graph graph;
	protected Session session;

	public Functions() {
		this.graph = new Graph();
		this.session = Graph.getSession();
	}
	

	public Session getSession(){
		return this.session;
	}
	public Graph getGraph(){
		return this.graph;
	}
	
}
