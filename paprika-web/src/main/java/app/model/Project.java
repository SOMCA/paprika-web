package app.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.types.Node;

import app.application.PaprikaFacade;
import app.utils.PaprikaKeyWords;
import app.utils.neo4j.LowNode;

/**
 * Project is a project of User, who contains many methods used on velocity
 * 
 * @author guillaume
 *
 */
public class Project extends Entity {

	/*
	 * listOfVersion est chargé uniquement quand on appelle une des fonctions de
	 * l'projects. Si la page Version est chargé, il charge aussi
	 * l'project, mais sans créer la liste de versions
	 */
	protected List<Version> listofVersion;
	/*
	 * reload est remis à false quand celui ci à besoin de mettre à jour la
	 * liste, donc à chaque recharge de la page d'index, permet d'avoir à
	 * charger qu'une liste pour l'ensemble des méthodes de l'appli.
	 */
	protected boolean reload;

	/**
	 * 
	 * @param name
	 *            The name of the project.
	 * @param id
	 *            The id of the project.
	 */
	public Project(String name, long id) {
		super(name, id);
		this.reload = false;

	}

	/**
	 * Get the number of version on string.
	 * 
	 * @return number of version
	 */
	public String getNumberOfVersion() {
		return Integer.toString(this.getNumberOfVersionReal());
	}

	/**
	 * Return the number of version. This is the size of real number of version,
	 * not the number of version on neo4J who is used for the order.
	 * 
	 * @return the number of version
	 */
	public int getNumberOfVersionReal() {
		return this.getListVersionProjects().size();
	}

	/**
	 * Put the reload to false, for than the next time a function call this
	 * flag, he reload the list.
	 */
	public void needReload() {
		this.reload = false;
	}

	/**
	 * Return the versions list of the current project.
	 * 
	 * @return a list of Version
	 */
	public List<Version> getListVersionProjects() {
		if (!this.reload) {

			this.reload = true;
			List<Version> versions = new ArrayList<>();
			Record record;
			String name;
			PaprikaFacade facade = PaprikaFacade.getInstance();
			LowNode lownode = new LowNode(PaprikaKeyWords.LABELPROJECT);
			lownode.setId(this.getID());
			List<Record> bigdata = facade.loadChildrenOfNode(lownode, PaprikaKeyWords.REL_PROJECT_VERSION,
					PaprikaKeyWords.VERSIONLABEL);

			Iterator<Record> iter = bigdata.iterator();
			Node node;
			while (iter.hasNext()) {
				record = iter.next();
				node = record.get(PaprikaKeyWords.NAMELABEL).asNode();

				name = node.get(PaprikaKeyWords.NAMEATTRIBUTE).asString();
				versions.add(new Version(name, node.id()));
			}
			// A sort for be sure than all versions are sort with order
			versions.sort((Version v1, Version v2) -> (int) v2.getOrder() - (int) v1.getOrder());

			this.listofVersion = versions;
			return versions;
		} else
			return this.listofVersion;

	}

	/**
	 * Return number version of the list on a new list.
	 * 
	 * @param number
	 *            the number of Version than you want.
	 * @return a subpart of the parameter list
	 */
	public List<Version> getLastXVersion(int number) {
		Iterator<Version> versions = getListVersionProjects().iterator();
		List<Version> lastVersion = new ArrayList<>();
		int numberVersion = this.getNumberOfVersionReal();

		// number cannot be lower than 1
		if (number > 0 && number < numberVersion) {
			numberVersion = number;
		}
		Version version;

		while (versions.hasNext()) {
			version = versions.next();
			if (version == null || numberVersion <= 0)
				return lastVersion;
			if (version.isAnalyzed() == 3) {
				lastVersion.add(version);
				numberVersion--;
			}
		}
		return lastVersion;
	}

	/**
	 * Return the iterator of the list of versions
	 * 
	 * @return the iterator of the parameter listversion.
	 */
	public Iterator<Version> getVersions() {
		return this.getListVersionProjects().iterator();
	}

	/**
	 * Return the number of versions who have be analyzed on the versions list
	 * if reload is false, return 0;
	 * 
	 * @return the number of versions who have be analyzed
	 */
	public int getNumberOfAnalysedVersion() {
		int i = 0;
		if (!reload) {
			return i;
		} else if (this.getNumberOfVersionReal() > 0) {
			for (Version version : listofVersion) {

				if (version.isAnalyzed() == 3)
					i += 1;
			}
		}
		return i;
	}

	/**
	 * Put all codesmells who do not have a value to 0 on a Map for each versions.
	 * 
	 * @param versions list of analyzed versions
	 * @return a list who contains a map for each version and who contains key
	 *         for each codesmells
	 */
	private List<Map<String, Long>> getDataGraph(Iterator<Version> versions) {

		Version version;
		List<Map<String, Long>> datas = new ArrayList<>();
		Map<String, Long> data;

		while (versions.hasNext()) {
			version = versions.next();
			Iterator<CodeSmells> css = version.getAllCodeSmells();
			data = new HashMap<>();
			while (css.hasNext()) {
				CodeSmells codesmell = css.next();
				if (codesmell.getNumberOfSmells() != 0) {
					data.put(codesmell.getName(), codesmell.getNumberOfSmells());
				}

			}
			datas.add(data);
		}
		return datas;
	}

	/**
	 * Return a key's arrays who contains all code smells on each Map of the List
	 * @param datas the data
	 * @return a array who contains key code smells.
	 */
	private String[] getKey(List<Map<String, Long>> datas) {
		Iterator<Map<String, Long>> dataiter = datas.iterator();
		Set<String> allkey = new HashSet<>();

		while (dataiter.hasNext()) {
			allkey.addAll(dataiter.next().keySet());

		}
		return allkey.toArray(new String[allkey.size()]);
	}

	/**
	 * getGraph is a method used per velocity who take a string for know what he need to render and a int for know how many versions it needed.
	 * And return a special string for the "renderGraph"
	 * 
	 * @param renderGraph a string like "area" or "radar"
	 * @param numberVersion the number of version to put on the return.
	 * @return special string very huge.
	 */
	public String getGraph(String renderGraph, int numberVersion) {
		List<Version> versions = getLastXVersion(numberVersion);
		List<Map<String, Long>> datas = getDataGraph(versions.iterator());
		String[] allkeyArray = getKey(datas);
		if (allkeyArray.length == 0) {
			return "";
		}

		Iterator<Map<String, Long>> dataiter = datas.iterator();


		if ("radar".equals(renderGraph)) {
			return radarD3(dataiter, allkeyArray).toString();
		} else if ("area".equals(renderGraph)) {
			return areaChart(dataiter, allkeyArray, versions).toString();
		}
		return "";

	}

	private StringBuilder radarD3(Iterator<Map<String, Long>> dataiter, String[] allkeyArray) {
		String line;
		long value;
		StringBuilder array;
		int i;
		String key;
		Map<String, Long> data;
		StringBuilder str = new StringBuilder();
		while (dataiter.hasNext()) {
			data = dataiter.next();
			array = new StringBuilder("[");
			for (i = 0; i < allkeyArray.length; i++) {
				key = allkeyArray[i];
				if (data.containsKey(key)) {
					value = data.get(key);
				} else
					value = 0;
				line = "{axis:\"" + key + "\",value:" + value + "},";
				array.append(line);
			}
			array.append("],");
			str.insert(0, array);
		}
		//System.out.println("Radar: "+str);
		return str;
	}

	private StringBuilder areaChart(Iterator<Map<String, Long>> dataiter, String[] allkeyArray,
			List<Version> versions) {
		Map<String, Long> data;
		String key;
		int i;
		StringBuilder array;
		String line;
		long value;
		Iterator<Version> versionsIter = versions.iterator();
		String name;
		StringBuilder str = new StringBuilder();

		// Partie 3, création du string
		while (versionsIter.hasNext()) {
			name = versionsIter.next().getName();
			data = dataiter.next();
			array = new StringBuilder("{version: '" + name + "', ");
			for (i = 0; i < allkeyArray.length; i++) {
				key = allkeyArray[i];
				if (data.containsKey(key)) {
					value = data.get(key);
				} else
					value = 0;
				line = key.toLowerCase() + ": " + value + ", ";
				array.append(line);
			}
			array.append("},");
			// On change l'ordre de l'inclusion
			str.insert(0, array);
		}
		// Du aux autres options, on a du rajouter des choses non liés au data

		StringBuilder xkeys = new StringBuilder();
		StringBuilder labels = new StringBuilder();

		for (i = 0; i < (allkeyArray.length - 1); i++) {
			key = "'" + allkeyArray[i] + "',";
			xkeys.append(key.toLowerCase());
			labels.append(key);
		}
		key = "'" + allkeyArray[allkeyArray.length - 1] + "',";
		xkeys.append(key.toLowerCase());
		labels.append(key);

		str.append("],   ykeys:[" + xkeys + "],labels: [" + labels + "],");
	//	System.out.println("Area: "+str);
		return str;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + this.getName() + "," + this.getID() + "," + this.getNumberOfVersion() + "]";

	}
}
