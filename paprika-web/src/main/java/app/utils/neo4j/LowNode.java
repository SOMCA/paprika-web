package app.utils.neo4j;

import java.util.HashMap;
import java.util.Iterator;

/**
 * LowNode is a custom Node who contain a label, can contain a id and can
 * contains many properties
 * 
 * @author guillaume
 *
 */
public class LowNode {
	private String label;
	private HashMap<String, String> map;
	private long id;

	/**
	 * LowNode is a custom Node who contain a label, can contain a id and can
	 * contains many properties
	 * 
	 * @param label
	 *            the label of the node.
	 */
	public LowNode(String label) {
		this.label = label;
		this.map = new HashMap<>();
		this.id = -1;

	}

	/**
	 * get the Label of the node.
	 * 
	 * @return the label
	 */
	public String getLabel() {
		return this.label;
	}

	/**
	 * get the id of the node
	 * 
	 * @return the id
	 */
	public long getID() {
		return this.id;
	}

	/**
	 * Put the propertie with a string value who need be overloap per two ""
	 * 
	 * @param attributeName
	 * @param value
	 */
	public void addParameter(String attributeName, String value) {
		this.map.put(attributeName, overString(value));
	}

	/**
	 * Put the propertie with a Object value where we call the toString()
	 * 
	 * @param attributeName
	 * @param value
	 */
	public void addParameter(String attributeName, Object value) {
		this.map.put(attributeName, value.toString());
	}

	/**
	 * Put the propertie with a int value where we call the toString() of
	 * Integer
	 * 
	 * @param attributeName
	 * @param value
	 */
	public void addParameter(String attributeName, int value) {
		this.map.put(attributeName, Integer.toString(value));
	}

	/**
	 * Put the propertie with a int value where we call the toString() of Long
	 * 
	 * @param attributeName
	 * @param value
	 */
	public void addParameter(String attributeName, long value) {
		this.map.put(attributeName, Long.toString(value));

	}

	/**
	 * Put the propertie with a int value where we call the toString() of Double
	 * 
	 * @param attributeName
	 * @param value
	 */
	public void addParameter(String attributeName, double value) {
		this.map.put(attributeName, Double.toString(value));
	}

	/**
	 * get the propertie of name attributeName
	 * 
	 * @param attributeName
	 * @return a string value
	 */
	public String getParameter(String attributeName) {
		return this.map.get(attributeName);
	}

	private String overString(String string) {
		return "\"" + string + "\"";
	}

	/**
	 * set a new ID for the node.
	 * 
	 * @param id
	 *            id of a node.
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Render a StringBuilder who contain a neo4j cypher data ex: "{name :
	 * guillaume }"
	 * 
	 * Warning, the order is not sort, same if this is not important for what it
	 * is used.
	 * 
	 * @return return a data for neo4j cypher
	 */
	public StringBuilder parametertoData() {
		if (this.map.size() == 0)
			return new StringBuilder(" ");
		String begin;
		String end;
		begin = " {";
		end = "} ";
		String key;
		String value;
		Boolean onetime = false;
		StringBuilder data = new StringBuilder(begin);
		Iterator<String> iter = this.map.keySet().iterator();
		while (iter.hasNext()) {
			key = iter.next();
			value = this.map.get(key);
			if (onetime)
				data.append(" , ");
			else
				onetime = true;

			data.append(key + ":" + value);
		}
		data.append(end);

		return data;
	}
}