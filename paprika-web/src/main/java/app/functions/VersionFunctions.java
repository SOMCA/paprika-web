package app.functions;

import org.neo4j.driver.v1.Record;

import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;


import app.model.Version;
import app.utils.PaprikaKeyWords;

import app.utils.neo4j.LowNode;

public class VersionFunctions extends Functions {


	/**
	 * Prend un node version, et incrémente de 1 à l'attribut en prenant en
	 * compte qu'il s'agit d'un long
	 * 
	 * @param label
	 * @param parameter
	 * @return
	 */
	private String increment(LowNode lowNode, String attribute, long value, Transaction tx) {

		long size = value;
		size += 1;
		LowNode nodePut = new LowNode(PaprikaKeyWords.LABELUSER);
		nodePut.addParameter(attribute, size);

		tx.run(graph.set(lowNode, nodePut));
		return Long.toString(size);
	}

	/**
	 * 
	 * Enregistre une nouvelle version dans l'application ciblé de l'utilisateur
	 * ciblé.
	 * 
	 * @param email
	 * @param application
	 * @param version
	 */
	public LowNode writeVersion(long idapplication, String version) {

		StatementResult result;
		Record record;
		Node node;

		LowNode nodeApp = new LowNode(PaprikaKeyWords.LABELPROJECT);
		nodeApp.setId(idapplication);

		// Incrémente le nombre de versions dans l'application:
		LowNode nodeVer;
		try (Transaction tx = this.session.beginTransaction()) {

			result = tx.run(graph.matchSee(nodeApp));
			record = result.next();
			node = record.get(PaprikaKeyWords.NAMELABEL).asNode();
			nodeVer = new LowNode(PaprikaKeyWords.VERSIONLABEL);
			nodeVer.addParameter(PaprikaKeyWords.NAMEATTRIBUTE, version);
			nodeVer.addParameter(PaprikaKeyWords.ORDER, this.increment(nodeApp, PaprikaKeyWords.ATTRIBUTE_NB_VERSION,
					node.get(PaprikaKeyWords.ATTRIBUTE_NB_VERSION).asLong(), tx));
			// Créer une version node:
			result = tx.run(graph.create(nodeVer));
			long id = this.graph.getID(result,PaprikaKeyWords.NAMELABEL);
			nodeVer.setId(id);

			tx.run(graph.relation(nodeApp, nodeVer, PaprikaKeyWords.REL_PROJECT_VERSION));
			tx.success();
		}
		return nodeVer;
	}

	public StatementResult loadDataCodeSmell(Version version) {
		StatementResult result;
		try (Transaction tx = this.session.beginTransaction()) {
			result = tx.run("MATCH (ver:" + PaprikaKeyWords.VERSIONLABEL + ") WHERE ID(ver)="
					+ version.getID() + " MATCH (ver)-[:" + PaprikaKeyWords.REL_VERSION_CODESMELLS
					+ "]->(codesmells) MATCH (codesmells)-[:" + PaprikaKeyWords.REL_CODESMELLS_CAS
					+ "]->(target) RETURN target ");
			tx.success();
		}
		return result;

	}
	

	/**
	 * Créer le noeud Query, avec sa relation au noeud de la Version, pour cela,
	 * il faut le noeud de la version et la clé de Query, pour pouvoir relier
	 * ensuite, les données du Query à ce noeud Query
	 * 
	 * @param nodeVer
	 * @param keyQuery
	 */
	public void writeQueryOnVersion(LowNode nodeVer, String keyQuery) {

		LowNode nodeResult = new LowNode(PaprikaKeyWords.LABELQUERY);
		// J'utilise nameattribute car le query a pour nom sa clé, pour moi.
		nodeResult.addParameter(PaprikaKeyWords.APPKEY, keyQuery);
		try (Transaction tx = this.session.beginTransaction()) {

			tx.run(this.graph.create(nodeResult));

			tx.run(this.graph.relation(nodeVer, nodeResult, PaprikaKeyWords.REL_VERSION_CODESMELLS));
			tx.success();
		}
	}

	/**
	 * Ecrit la relation de l'analyse de paprika de la version. En y insérant
	 * aussi la clé au passage. (Pour supprimer, dans le futur)
	 * 
	 * @param nodeVer
	 */
	public void writeAnalyzeOnVersion(LowNode nodeVer, long idApp) {

		LowNode nodeResult = new LowNode(PaprikaKeyWords.LABELAPP);
		nodeResult.setId(idApp);
		try (Transaction tx = this.session.beginTransaction()) {

		tx.run(graph.relation(nodeVer, nodeResult, PaprikaKeyWords.REL_VERSION_CODE));
		tx.success();
		}
		

	}

	/**
	 * Retrouve l'id de la version de l'application ciblé en utilisant des
	 * informations de bases.
	 * 
	 * @param email
	 * @param application
	 * @param version
	 * @return
	 */
	public long receiveIDOfVersion(long idapplication, String version) {
		LowNode nodeApp = new LowNode(PaprikaKeyWords.LABELPROJECT);
		nodeApp.setId(idapplication);
		LowNode nodeVer = new LowNode(PaprikaKeyWords.VERSIONLABEL);
		nodeVer.addParameter(PaprikaKeyWords.NAMEATTRIBUTE, version);

		return receiveIDOfVersion(nodeApp, nodeVer);
	}

	/**
	 * Retrouve l'id de la version de l'application ciblé à partir du node de
	 * l'application qui contient l'id et de quelques données dans le node de la
	 * version
	 * 
	 * @param nodeApp
	 * @param nodeVer
	 * @return
	 */
	public long receiveIDOfVersion(LowNode nodeApp, LowNode nodeVer) {
		StatementResult result;
		try (Transaction tx = this.session.beginTransaction()) {
		result = tx.run(graph.matchSee(nodeApp, nodeVer, PaprikaKeyWords.REL_PROJECT_VERSION));
		tx.success();
		}
		if (result.hasNext()) {
			return this.graph.getID(result,PaprikaKeyWords.NAMELABEL);
		}
		return -1;
	}
	
	/**
	 * Donne le nombre de codesmell contenu dans le noeud codesmell
	 * @param id
	 * @return
	 */
	public long getNumberOfSmells(long id){
		StatementResult result;
		try (Transaction tx = this.session.beginTransaction()) {
		result = tx.run("MATCH (n:"+PaprikaKeyWords.VERSIONLABEL+") where ID(n)="+id
				+ " MATCH (n)-[:"+PaprikaKeyWords.REL_VERSION_CODESMELLS+"]->(target:"+PaprikaKeyWords.LABELQUERY+")"
						+ " return target.number");
		tx.success();
		}
		if(result.hasNext()){
			Value value=result.next().get("number");
			if(value!=null && !value.isNull()){
				return value.asLong();
			}
		}
		return 0;
	}
	
	
	/**
	 * applique le nombre de code smell trouvé, dans le noeud codesmell
	 * @param id
	 * @param number
	 */
	public void applyNumberOfCodeSmells(long id,long number){

		try (Transaction tx = this.session.beginTransaction()) {
		tx.run("MATCH (n:"+PaprikaKeyWords.VERSIONLABEL+") where ID(n)="+Long.toString(id)
				+ " MATCH (n)-[:"+PaprikaKeyWords.REL_VERSION_CODESMELLS+"]->(target:"+PaprikaKeyWords.LABELQUERY+")"
						+ "set target.number="+Long.toString(number));
		tx.success();
		}

	}
	
	public long getOrder(long id){
		StatementResult result;
		try (Transaction tx = this.session.beginTransaction()) {
		result = tx.run("MATCH (n:"+PaprikaKeyWords.VERSIONLABEL+") where ID(n)="+id+" return n."+PaprikaKeyWords.ORDER);
		tx.success();
		}
		if(result.hasNext()){
			Value value=result.next().get(PaprikaKeyWords.ORDER);
			if(value!=null && !value.isNull()){
				return value.asLong();
			}
		}
		return 0;
	}



}
