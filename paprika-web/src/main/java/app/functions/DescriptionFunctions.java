package app.functions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

import app.utils.neo4j.LowNode;
/**
 * DescriptionFunctions is a util class, used for build node of description.
 * @author guillaume
 *
 */
public class DescriptionFunctions extends Functions {

	/**
	 * Add all description of the list of dataDescription on multiple node.
	 */
	public void addAllClassicDescription() {

		StatementResult result;
		try (Transaction tx = this.session.beginTransaction()) {
			result = tx.run("MATCH (d:Description) return d");
			if (!result.hasNext()){
				tx.run("CREATE (d:Description)");
			}
			else
				return;

			LowNode nodeDec = new LowNode("Description");
			Iterator<LowNode> iterNode = this.dataDescription().iterator();
			LowNode node;
			while (iterNode.hasNext()) {
				node = iterNode.next();
				tx.run(this.graph.relationcreateRight(nodeDec, node, "INFO"));
			}
			tx.success();
		}

	}

	/**
	 * Not used, but already created, this method is use for create a new description.
	 * Need to be deleted, if you do not think add a function on velocity for add description.
	 * @param labelName the shortname of the code smell
	 * @param description the description of the code smell
	 * @param longName the longname of the code smell
	 */
	public void addDescription(String labelName, String description, String longName) {
		StatementResult result;
		try (Transaction tx = this.session.beginTransaction()) {
			result = tx.run("MATCH (d:Description) return d");
			if (!result.hasNext())
				tx.run("CREATE (d:Description) return d");

			LowNode nodeDec = new LowNode("Description");
			LowNode node = new LowNode(labelName);
			node.addParameter("info", description);
			node.addParameter("name", longName);
			tx.run(this.graph.relationcreateRight(nodeDec, node, "INFO"));
			tx.success();
		}
	}


	/**
	 * Util Method who return a data list of lowNode
	 * @return list of lowNode
	 */
	public List<LowNode> dataDescription() {
		List<LowNode> desclist = new ArrayList<>();

		LowNode node;
		final String frenchsite = "<br>See: <a href='https://tel.archives-ouvertes.fr/tel-01418158/document'>Code Smells PDF french</a>";

		node = getNodeDescription("ARGB8888", "bitmap configuration of Image",
				"Usage of a bad bitmap format. This is a minor problem, not a code smell"
						+ "<br>See: <a href='https://developer.android.com/reference/android/graphics/Bitmap.Config.html'>Bitmap Config</a>");

		desclist.add(node);

		node = getNodeDescription("BLOB", "God Object", "A class that has grown too large."
				+ "<br>See: <a href='https://en.wikipedia.org/wiki/God_object'>God Object</a>" + frenchsite);
		desclist.add(node);

		node = getNodeDescription("CC", "Complex Class/Cyclomatic complexity",
				"Too many branches or loops; this may indicate a function needs to be broken up into smaller functions, "
						+ "<br>or that it has potential for simplification."
						+ "<br>See: <a href='https://en.wikipedia.org/wiki/Cyclomatic_complexity'>Cyclomatic complexity</a>"
						+ frenchsite);
		desclist.add(node);

		node = getNodeDescription("HMU", "Hashmap Usage",
				"Usage of a HashMap for a small Map instead of ArrayMap who is better for small Map, contrary to HashMap reserved to big Map"
						+ frenchsite);
		desclist.add(node);

		node = getNodeDescription("HAS", "Heavy AsyncTask", "///" + frenchsite);
		desclist.add(node);

		node = getNodeDescription("HBR", "Heavy BroadcastReceiver","Usage of a onReceive of BroadCastReceiver for a long process on the main thread" + frenchsite);
		desclist.add(node);
		
		node = getNodeDescription("HSS","Heavy Service Start", "A method OnStartCommand have be called to the main thread." + frenchsite);
		desclist.add(node);

		node = getNodeDescription("IGS","Internal Getter/Setter", 				"Internal fields are accessed via getters and setters."
				+ "<br>But in Android virtual method are expensive."
				+ "<br>See: <a href='http://www.modelrefactoring.org/smell_catalog/smells/internal_getter_setter.html'>Internal Getter/Setter</a>"
				+ frenchsite);
		desclist.add(node);

		node = getNodeDescription("IOD","Init OnDraw", "Too long process for the method OnDraw of class who extends of View" + frenchsite);
		desclist.add(node);

		node = getNodeDescription("IWR","Invalidate Without Rect", "Bad pratice of the method Invalidate" + frenchsite);
		desclist.add(node);

		node = getNodeDescription("LIC", "Leaking Inner Class",				"Memory Efficiency"
				+ "<br>See: <a href='http://www.modelrefactoring.org/smell_catalog/smells/leaking_inner_class.html'>Leaking Inner Class</a>"
				+ frenchsite);
		desclist.add(node);

		node = getNodeDescription("LM","Long Method", "A method too long who can be simplified."
				+ "<br>See: <a href='https://en.wikipedia.org/wiki/Code_smell'>Code Smell Wikipedia</a>" + frenchsite);
		desclist.add(node);
		
		node = getNodeDescription("MIM","Member Ignoring Method",				"Non-static methods that don't access any property."
				+ "<br>See: <a href='http://www.modelrefactoring.org/smell_catalog/smells/member_ignoring_method.html'>Member Ignoring Method</a>"
				+ frenchsite);
		desclist.add(node);

		node = getNodeDescription("NLMR","No Low Memory Resolver", 				"Problem of memory of the Android, where it can automatic kill low process if developper have not implemented a onLowMemory()"
				+ "<br>See: <a href='http://www.modelrefactoring.org/smell_catalog/smells/no_low_memory_handler.html'>No Low Memory Resolver</a>"
				+ frenchsite);
		desclist.add(node);


		node = getNodeDescription("UIO", "UI Overdraw",
				"Generate a pixel hide per a other pixel"
						+ "<br>See: <a href='http://www.modelrefactoring.org/smell_catalog/smells/overdrawn_pixel.html'>Overdrawn Pixel</a>"
						+ frenchsite);
		desclist.add(node);

		node = getNodeDescription("SAK", "Swiss Army Knife",
				"A too huge interface for multiples classes who do not use all methods of the implementation"
						+ frenchsite);
		desclist.add(node);

		node = getNodeDescription("THI", "Tracking Hardware Id",
				"For some use cases it might be necessary to get a unique, reliable, unique device identifier."
						+ "<br>See: <a href='http://www.modelrefactoring.org/smell_catalog/smells/tracking_hardwareid.html'>Tracking Hardware Id</a>");
		desclist.add(node);

		node = getNodeDescription("UCS", "Unsuited LRU Cache Size",
				"Use a LRU(Least Recently Used) without use a getMemoryClass" + frenchsite);
		desclist.add(node);

		node = getNodeDescription("UHA", "Unsupported Hardware Acceleration",
				"Use drawPath instead of drawLine of the android.graphics.Canvas, drawPath is not very good for Android"
						+ "<br>See: <a href='http://www.modelrefactoring.org/smell_catalog/smells/tracking_hardwareid.html'>Tracking Hardware Id</a>");
		desclist.add(node);

		return desclist;

	}

	/**
	 * Create a lowNode with the shortName, LongName and the description.
	 * @param labelname The short Name of the code smell.
	 * @param name The long Name of the code smell.
	 * @param info The description.
	 * @return
	 */
	private LowNode getNodeDescription(String labelname, String name, String info) {
		LowNode node = new LowNode(labelname);
		node.addParameter("name", name);
		node.addParameter("info", info);
		return node;
	}



}
