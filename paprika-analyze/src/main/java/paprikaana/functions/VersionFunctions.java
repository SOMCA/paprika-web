package paprikaana.functions;

import org.neo4j.driver.v1.Transaction;

import paprika.neo4jBolt.Graph;
import paprika.neo4jBolt.LowNode;


/**
 * Versions Functions for paprika-analyze.
 * 
 * @author guillaume
 *
 */
public class VersionFunctions extends Functions {



	

	/**
	 * Create the Code smells node with a relation to the nodeVersion. 
	 * 
	 * @param nodeVer the lowNode of the nodeVer.
	 */
	public void writeQueryOnVersion(LowNode nodeVer) {

		LowNode nodeResult = new LowNode(Graph.LABELQUERY);
		// J'utilise nameattribute car le query a pour nom sa clé, pour moi.
		nodeResult.addParameter(Graph.APPKEY, nodeVer.getID());
		try (Transaction tx = this.session.beginTransaction()) {

			tx.run(this.graph.create(nodeResult));

			tx.run(this.graph.relation(nodeVer, nodeResult, Graph.REL_VERSION_CODESMELLS));
			tx.success();
		}
	}

	/**
	 * Write a relation between the Version node and the Application node.
	 * 
	 * @param nodeVer
	 * @param idApp 
	 */
	public void writeAnalyzeOnVersion(LowNode nodeVer, long idApp) {

		LowNode nodeResult = new LowNode(Graph.LABELAPP);
		nodeResult.setId(idApp);
		try (Transaction tx = this.session.beginTransaction()) {

		tx.run(graph.relation(nodeVer, nodeResult, Graph.REL_VERSION_CODE));
		tx.success();
		}
	}
	
	
	/**
	 * Apply or create a new value on the node of the idnode.
	 * @param idnode 
	 * @param parameter 
	 * @param attribute 
	 * 
	 */
	public void setParameterOnNode(long idnode, String parameter, String attribute) {
		if (idnode == -1) {
			return;
		}
		try (Transaction tx =  this.session.beginTransaction()) {
			tx.run("MATCH (n) WHERE ID(n)= "+idnode+" SET n+={"+parameter+":\""+attribute+"\"}");
			
			tx.success();
		}
	}

}
