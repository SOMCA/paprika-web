package app.functions;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.types.Node;

import app.application.PaprikaWebMain;

public class CodeSmellsFunctions extends Functions {

	public Node getNode(String labelname) {
		String label = labelname;
		String fuzzy = "_NO_FUZZY";
		if (label.endsWith(fuzzy)) {
			label = labelname.substring(0, labelname.length() - fuzzy.length());
		}
		StatementResult result;
		Node node = null;
		try (Transaction tx = this.session.beginTransaction()) {
			result = tx.run("MATCH (d:Description)-[:INFO]->(target:" + label + ") RETURN target");
			if (result.hasNext()) {
				Record record = result.next();
				node = record.get("target").asNode();
			}
			tx.success();
		}
		return node;
	}

	/**
	 * Renvoie la commande lié au codesmell pour obtenir les résultats du graph,
	 * mais aussi "search" pour savoir si on aura des classes,methodes ou
	 * autres.
	 * 
	 * @param nameLabel
	 * @param id
	 * @return
	 */
	public String[] getToSearch(String nameLabel, long id) {

		final String matchmatchclass = "MATCH(l:Class) MATCH(cs:";
		final String matchmatchmethod = "MATCH(l:Method) MATCH(cs:";
		final String classSearch = "Class";
		final String methodSearch = "Method";
		final String beginCommand = nameLabel + ") WHERE ID(cs)=" + Long.toString(id)
				+ "  MATCH(cs)-[:HAS_CODESMELL]->(l) ";
		final String beginCommandclass = matchmatchclass + beginCommand;
		final String beginCommandmethod = matchmatchmethod + beginCommand;

		String command = null;
		String search = null;
		switch (nameLabel) {
		case "BLOB_NO_FUZZY":
		case "BLOB":
			command = beginCommandclass
					+ " RETURN l.name as Location,l.modifier as Modifier, l.lack_of_cohesion_in_methods as Lack_of_cohesion_in_methods,l.number_of_attributes as Number_of_attributes ,l.number_of_methods as Number_of_methods";
			search = classSearch;
			break;
		case "CC_NO_FUZZY":
		case "CC":
			command = beginCommandclass
					+ " RETURN l.name as Location,l.modifier as Modifier, l.class_complexity as Class_complexity, l.npath_complexity as Npath_complexity";
			search = classSearch;
			break;
		case "UHA":
		case "HMU":
		case "HAS_NO_FUZZY":
		case "HAS":
		case "HBR_NO_FUZZY":
		case "HBR":
		case "HSS_NO_FUZZY":
		case "HSS":
			command = beginCommandmethod
					+ " RETURN l.full_name as Location,l.modifier as Modifier,l.return_type as Type";
			search = methodSearch;
			break;
		case "ARGB8888":
		case "IGS":
		case "IOD":
		case "IWR":
		case "UIO":
		case "THI":
			break;
		case "LM_NO_FUZZY":
		case "LM":
			command = beginCommandmethod
					+ " RETURN l.full_name as Location,l.modifier as Modifier,l.return_type as Type, l.number_of_instructions as Number_of_line";
			search = methodSearch;
			break;
		case "MIM":
			command = beginCommandmethod
					+ " RETURN l.full_name as Location,l.modifier as Modifier,l.return_type as Type, l.number_of_direct_calls as Number_of_direct_calls";
			search = methodSearch;
			break;
		case "LIC":
		case "NLMR":
			command = beginCommandclass + " RETURN l.name as Location,l.modifier as Modifier";
			search = classSearch;
			break;

		case "SAK_NO_FUZZY":
		case "SAK":
			command = beginCommandclass
					+ " RETURN l.name as Location,l.modifier as Modifier,l.number_of_methods as Number_of_methods";
			search = classSearch;
			break;

		default:
			PaprikaWebMain.LOGGER.trace("Problem");
			break;
		}

		String[] strs = { search, command };
		return strs;
	}

	public StatementResult getPreciseDataForEachCodeSmells(String command) {
		StatementResult result = null;
		if (command != null)
			try (Transaction tx = this.session.beginTransaction()) {
				result = tx.run(command);
				tx.success();
			}
		return result;
	}

}
