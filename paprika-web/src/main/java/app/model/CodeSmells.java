package app.model;

import java.util.Iterator;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;

import app.functions.CodeSmellsFunctions;

public class CodeSmells extends Entity {

	private long numberOfSmells;
	private String description;
	private String longName;
	
	private static final String[] arrayHtml= {"<tr>","</tr>","<td>","</td>","<th>","</th>"};

	public CodeSmells(String name, long id, long numberOfSmells) {
		super(name, id);
		this.numberOfSmells = numberOfSmells;
		Node node = new CodeSmellsFunctions().getNode(name);
		if (node != null) {
			this.description = node.get("info").asString();
			this.longName = node.get("name").asString();
		} else {
			this.description = "No informations on this code smells";
			this.longName = "";
		}

	}

	public long getNumberOfSmells() {
		return this.numberOfSmells;
	}

	public String getDescription() {
		return this.description;
	}

	public String getLongName() {
		return this.longName;
	}

	/**
	 * LM seulement, pour le moment
	 * 
	 * @return
	 */
	public String getLineList() {
		StringBuilder arrayData = new StringBuilder("");
		CodeSmellsFunctions csFct= new CodeSmellsFunctions();
		String[] somedata= csFct.getToSearch(this.getName(), this.getID());
		StatementResult result =csFct.getPreciseDataForEachCodeSmells(somedata[1]);
		if (result != null) {
			Record record;
			boolean method=!("Class".equals(somedata[0]));
			if (result.hasNext()) {
				record = result.next();
				arrayData.append(this.getParameter(record, somedata[0]));
				
				StringBuilder line;
				if(method){
					line=this.getLineMethod(record);
				}
				else line=this.getLineClass(record);
				
				arrayData.append(line);
			}
			while (result.hasNext()) {
				record = result.next();
				StringBuilder line;
				if(method){
					line=this.getLineMethod(record);
				}
				else line=this.getLineClass(record);
				arrayData.append(line);
			}
		}

		return arrayData.toString();
	}
	
	private StringBuilder getParameter(Record record, String object) {
		StringBuilder line = new StringBuilder(arrayHtml[0]);

			line.append(arrayHtml[4]+object+arrayHtml[5]);
		// style='text-align: center;'
		Iterator<String> recordIter = record.keys().iterator();
		while (recordIter.hasNext()) {
			line.append(arrayHtml[4]);
			line.append(recordIter.next());
			line.append(arrayHtml[5]);
		}
		line.append(arrayHtml[1]);
		return line;
	}

	private StringBuilder getLineMethod(Record record) {
		StringBuilder line = new StringBuilder(arrayHtml[0]);
		Iterator<Value> recordIter = record.values().iterator();
		String value;
		String newvalue;

		if (recordIter.hasNext()) {
			
			value = recordIter.next().asString();
			String[] values = value.split("#");
			if ("<init>".equals(values[0])) {
				newvalue = value.substring(value.lastIndexOf('.')+1, value.length());
			}else newvalue=values[0];
			line.append(arrayHtml[2]);
			line.append(newvalue);
			line.append(arrayHtml[3]);
			
			line.append(arrayHtml[2]);
			line.append(values[1]);
			line.append(arrayHtml[3]);
		}

		while (recordIter.hasNext()) {
			value = recordIter.next().toString();
			
			line.append(arrayHtml[2]);
			line.append(value);
			line.append(arrayHtml[3]);
			
		}
		line.append(arrayHtml[1]);
		return line;
	}
	
	private StringBuilder getLineClass(Record record) {
		StringBuilder line = new StringBuilder(arrayHtml[0]);
		Iterator<Value> recordIter = record.values().iterator();
		String value;

		if (recordIter.hasNext()) {
			
			value = recordIter.next().asString();
			String newvalue = value.substring(value.lastIndexOf('.')+1, value.length());
			line.append(arrayHtml[2]);
			line.append(newvalue);
			line.append(arrayHtml[3]);
			
			line.append(arrayHtml[2]);
			line.append(value);
			line.append(arrayHtml[3]);
		}

		while (recordIter.hasNext()) {
			value = recordIter.next().toString();
			
			line.append(arrayHtml[2]);
			line.append(value);
			line.append(arrayHtml[3]);
			
		}
		line.append(arrayHtml[1]);
		return line;
	}
	
}
