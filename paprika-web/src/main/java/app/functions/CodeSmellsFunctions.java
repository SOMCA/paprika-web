package app.functions;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.types.Node;

import app.application.PaprikaWebMain;

public class CodeSmellsFunctions extends Functions {

	public Node getNode(String labelname) {

		String fuzzy = "_NO_FUZZY";
		if (labelname.endsWith(fuzzy)) {
			labelname = labelname.substring(0, labelname.length() - fuzzy.length());
			PaprikaWebMain.LOGGER.trace(labelname);
		}
		StatementResult result;
		Node node = null;
		try (Transaction tx = this.session.beginTransaction()) {
			result = tx.run("MATCH (d:Description)-[:INFO]->(target:" + labelname + ") RETURN target");
			if (result.hasNext()) {
				Record record = result.next();
				node = record.get("target").asNode();
			}
			tx.success();
		}
		return node;
	}
	
	private static final String MATCHCS="  MATCH(cs)-[:HAS_CODESMELL]->(l)";
	private static final String WHEREID=") WHERE ID(cs)=";
	private static final String MATCHMATCHCLASS="MATCH(l:Class) MATCH(cs:";
	private static final String MATCHMATCHMETHOD="MATCH(l:Method) MATCH(cs:";
	
	/**
	 * Renvoie la commande lié au codesmell pour obtenir les résultats du graph, mais aussi "search" pour savoir si on aura des classes,methodes ou autres.
	 * @param nameLabel
	 * @param id
	 * @return
	 */
	public String[] getToSearch(String nameLabel, long id) {
		
		String command = null;
		String search=null;
		switch (nameLabel) {
		case "ARGB8888":
			// Pas à faire.
			break;
		case "BLOB_NO_FUZZY":
		case "BLOB":
			command =MATCHMATCHCLASS+nameLabel+WHEREID+Long.toString(id)+MATCHCS
					+ " RETURN l.name as Location,l.modifier as Modifier, l.lack_of_cohesion_in_methods as Lack_of_cohesion_in_methods,l.number_of_attributes as Number_of_attributes ,l.number_of_methods as Number_of_methods";
			search="Class";

			break;
		case "CC_NO_FUZZY":
		case "CC":
			command =MATCHMATCHCLASS+nameLabel+WHEREID+Long.toString(id)+MATCHCS
					+ " RETURN l.name as Location,l.modifier as Modifier, l.class_complexity as Class_complexity, l.npath_complexity as Npath_complexity";
			search="Class";
			break;
		case "UHA":
		case "HMU":
		case "HAS_NO_FUZZY":
		case "HAS":
		case "HBR_NO_FUZZY":
		case "HBR":
		case "HSS_NO_FUZZY":
		case "HSS":
			command = MATCHMATCHMETHOD + nameLabel +WHEREID+ Long.toString(id) + MATCHCS
					+ " RETURN l.full_name as Location,l.modifier as Modifier,l.return_type as Type";
			search="Method";
			break;
		case "IGS":
			break;
		case "IOD":
			break;
		case "IWR":
			break;
		case "LM_NO_FUZZY":
		case "LM":
			command = MATCHMATCHMETHOD+ nameLabel +WHEREID+ Long.toString(id) +  " MATCH(cs)-[:HAS_CODESMELL]->(l) "
					+ " RETURN l.full_name as Location,l.modifier as Modifier,l.return_type as Type, l.number_of_instructions as Number_of_line";
			search="Method";
			break;
		case "MIM":
			command = MATCHMATCHMETHOD + nameLabel +WHEREID+ Long.toString(id) +  " MATCH(cs)-[:HAS_CODESMELL]->(l) "
					+ " RETURN l.full_name as Location,l.modifier as Modifier,l.return_type as Type, l.number_of_direct_calls as Number_of_direct_calls";
			search="Method";
			break;
		case "LIC":
		case "NLMR":
			command =MATCHMATCHCLASS+nameLabel+WHEREID+Long.toString(id)+MATCHCS
					+ " RETURN l.name as Location,l.modifier as Modifier";
			search="Class";
			break;
		case "UIO":
			break;
		case "SAK_NO_FUZZY":
		case "SAK":
			command ="MATCH (l:Class) MATCH(cs:"+nameLabel+WHEREID+Long.toString(id)+MATCHCS
					+ " RETURN l.name as Location,l.modifier as Modifier,l.number_of_methods as Number_of_methods";
			search="Class";
			break;
		case "THI":
			break;

		default:
			PaprikaWebMain.LOGGER.trace("Problem");
			break;
		}
		String[] strs={search,command};
		return strs;
	}
	
	public StatementResult getPreciseDataForEachCodeSmells(String command){
		StatementResult result = null;
		if (command != null)
			try (Transaction tx = this.session.beginTransaction()) {
				result = tx.run(command);
				tx.success();
			}
		return result;
	}

}
