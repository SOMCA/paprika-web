package paprikaana.application;

import java.util.List;


import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import paprikaana.utils.neo4j.Graph;
import paprikaana.utils.neo4j.LowNode;

public final class PaprikaFacade {

	/** Constructeur privé */
	private PaprikaFacade() {
	}

	/** Holder */
	private static class SingletonHolder {

		/** Instance unique non préinitialisée */
		private static final PaprikaFacade instance = new PaprikaFacade();

		private SingletonHolder() {
		}
	}

	/** Point d'accès pour l'instance unique du singleton */
	public static PaprikaFacade getInstance() {
		return SingletonHolder.instance;
	}

	/**
	 * Applique ou créer une nouvelle valeur dans le node en question. Le node
	 * doit contenir une Id pour fonctionner.
	 * 
	 * @param nodeVer
	 * @param parameter
	 * @param key
	 */
	public void setParameterOnNode(long idnode, String parameter, String attribute) {
		if (idnode == -1) {
			return;
		}
		try (Transaction tx = PaprikaAnalyzeMain.getSession().beginTransaction()) {
			tx.run("MATCH (n) WHERE ID(n)= "+idnode+" SET n+={"+parameter+":\""+attribute+"\"}");
			
			tx.success();
		}
	}

	/**
	 * Renvoie sous forme de liste, les fils d'un noeud à l'aide de son lownode
	 * et de la relation qui lie le noeud et ces fils de tels labels
	 * 
	 * @param email
	 */
	public List<Record> loadChildrenOfNode(LowNode lownode, String relation, String childrenLabel) {
		StatementResult result;
		LowNode nodeChildren = new LowNode(childrenLabel);
		Graph graph = new Graph();
		try (Transaction tx = PaprikaAnalyzeMain.getSession().beginTransaction()) {
			result = tx.run(graph.matchSee(lownode, nodeChildren, relation));
			tx.success();
		}
		return result.list();
	}


}
