package app.functions;

import java.util.List;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Value;

import app.utils.DataSave;

/**
 * All method linked to unique node dataSave, who contains many values not very
 * important.
 * 
 * @author guillaume
 */
public class DataSaveFunctions extends Functions {

	/**
	 * Use on the Paprika timer, for update the number. Each 5minutes.
	 */
	public void updateData() {

		try (Transaction tx = this.session.beginTransaction()) {
			tx.run("match (p:Project) match(d:DataSave) WITH d, count(p) AS cp SET d.nbProject = cp");
			tx.run("match (v:Version) match(d:DataSave) WITH d, count(v) AS cv SET d.nbVersion = cv");
			tx.run("match (u:User) match(d:DataSave) WITH d, count(u) AS cu SET d.nbUser = cu");
			tx.success();
		}
		updateStatic();
	}

	/**
	 * 
	 */
	public void updateStatic() {

		try (Transaction tx = this.session.beginTransaction()) {
			StatementResult result = tx.run("match(d:DataSave) return d.nbUser as user,d.nbProject as project,d.nbVersion as version");
			if (result.hasNext()) {
				Record record = result.next();
				DataSave data = new DataSave();

				Value value = record.get("user");
				if (!value.isNull()) {
					data.setNbUser(value.asLong());
				} else data.setNbUser(0);
				value = record.get("project");
				if (!value.isNull()) {
					data.setNbProject(value.asLong());
				} else data.setNbProject(0);
				value = record.get("version");
				if (!value.isNull()) {
					data.setNbVersion(value.asLong());
				} else data.setNbVersion(0);

			}

			tx.success();
		}

	}

	public String[] searchContainer(String[] containerRun) {

		try (Transaction tx = this.session.beginTransaction()) {
			StatementResult result = tx.run("MATCH(n:DataSave) return n.containerRun");
			if (result.hasNext()) {
				Record record = result.next();
				List<Value> values = record.values();
				if (!values.isEmpty()) {
					if (values.size() == containerRun.length) {
						for (int i = 0; i < containerRun.length; i++) {
							Value value = values.get(i);
							if (value != null && !value.isNull()) {
								containerRun[i] = value.asString();
							}
						}
					}
				}
			} else {

				tx.run("CREATE (n:DataSave)");
			}
			tx.success();
		}
		return containerRun;
	}

	public void runCommand(String command) {

		try (Transaction tx = this.session.beginTransaction()) {
			tx.run(command);
			tx.success();
		}
	}
}
