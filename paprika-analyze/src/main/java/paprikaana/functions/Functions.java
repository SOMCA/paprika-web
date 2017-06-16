package paprikaana.functions;

import org.neo4j.driver.v1.Session;

import paprika.neo4jBolt.DriverBolt;
import paprika.neo4jBolt.Graph;

public abstract class Functions {
	protected Graph graph;
	protected Session session;

	public Functions() {
		this.graph = new Graph();
		this.session = DriverBolt.getSession();
	}
	

	public Session getSession(){
		return this.session;
	}
	public Graph getGraph(){
		return this.graph;
	}
	
}
