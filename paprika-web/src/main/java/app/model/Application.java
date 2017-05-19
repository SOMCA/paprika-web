package app.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.types.Node;

import app.application.PaprikaFacade;
import app.application.PaprikaWebMain;
import app.utils.PaprikaKeyWords;
import app.utils.neo4j.LowNode;

public class Application extends Entity {

	/*
	 * listOfVersion est chargé uniquement quand on appelle une des fonctions de
	 * l'applications. Si la page Version est chargé, il charge aussi
	 * l'application, mais sans créer la liste de versions
	 */
	private List<Version> listofVersion;
	/*
	 * reload est remis à false quand celui ci à besoin de mettre à jour la
	 * liste, donc à chaque recharge de la page d'index, permet d'avoir à
	 * charger qu'une liste pour l'ensemble des méthodes de l'appli.
	 */
	private boolean reload;

	public Application(String name, long id) {
		super(name, id);
		this.reload = false;

	}

	/**
	 * Transforme juste en string le nombre.
	 * 
	 * @return
	 */
	public String getNumberOfVersion() {
		return Integer.toString(this.getNumberOfVersionReal());
	}

	/**
	 * Renvoie la taille de la liste de versions, comme celle ci peut être
	 * nulle, si nulle, il renvoie 0
	 * 
	 * On n'utilise pas le nombre de Version du node application, car sinon, si on supprime des versions
	 * on devra changer leur ordre à chacun ce qui sera long, alors le nombre de versions ne fait que grossir dans l'application
	 * 
	 * @return
	 */
	public int getNumberOfVersionReal() {
		if(reload) {
			return this.listofVersion.size();
		}
		else {
			return this.getListVersionApplications().size();
		}
	}

	public void needReload() {
		this.reload = false;
	}

	/**
	 * Renvoie la liste des versions de l'applications
	 * 
	 * @param application
	 * @return
	 */
	public List<Version> getListVersionApplications() {
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
			Collections.sort(versions, new Comparator<Version>() {
				@Override
				public int compare(Version v1, Version v2) {
					if (v1.getOrder() < v2.getOrder()) {
						return 1;
					}
					return (v1.getOrder() > v2.getOrder()) ? -1 : 0;
				}
			});
			this.listofVersion = versions;
			return versions;
		} else
			return this.listofVersion;

	}

	/**
	 * Retourne X versions
	 * 
	 * @param number
	 * @return
	 */
	public List<Version> getLastXVersion(int number) {
		Iterator<Version> versions = getListVersionApplications().iterator();
		List<Version> lastVersion = new ArrayList<>();
		int numberVersion = this.getNumberOfVersionReal();
		Version version;

		while (versions.hasNext()) {
			version = versions.next();
			if (version == null || numberVersion <= 0)
				return lastVersion;
			if (version.isAnalyzed()==3) {
				lastVersion.add(version);
				numberVersion--;
			}
		}
		return lastVersion;
	}

	public Iterator<Version> getVersions() {
		if (reload)
			return listofVersion.iterator();
		else
			return this.getListVersionApplications().iterator();
	}

	public int getNumberOfAnalysedVersion() {
		int i = 0;
		if (!reload) {
			return i;
		} else if (this.getNumberOfVersionReal() > 0) {
			for (Version version : listofVersion) {

				if (version.isAnalyzed()==3)
					i += 1;
			}
		}
		return i;
	}

	private List<Map<String, Long>> getDataGraph(Iterator<Version> versions) {

		Version version;
		List<Map<String, Long>> datas = new ArrayList<>();
		Map<String, Long> data;

		// Partie 1, je récupère toutes les données dans une map.
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
	 * Retourne uniquement l'ensemble des clés code smells qui existe de la
	 * donnée passé
	 * 
	 * @return
	 */
	private String[] getKey(List<Map<String, Long>> datas) {
		Iterator<Map<String, Long>> dataiter = datas.iterator();
		Set<String> allkey = new HashSet<>();

		// Partie 2, où je prends l'ensemble des clés qui existe.
		while (dataiter.hasNext()) {
			allkey.addAll(dataiter.next().keySet());

		}
		// Puis transforme le tout en tableau
		return allkey.toArray(new String[allkey.size()]);
	}
	
	public String getGraph(String renderGraph,int numberVersion){
		List<Version> versions = getLastXVersion(numberVersion);
		List<Map<String, Long>> datas = getDataGraph(versions.iterator());
		String[] allkeyArray = getKey(datas);
		if(allkeyArray.length==0) {
			return "";
		}

		Iterator<Map<String, Long>> dataiter = datas.iterator();

		if("radar".equals(renderGraph)){
			return radarD3(dataiter,allkeyArray).toString();
		}
		else if ("area".equals(renderGraph)){
			return areaChart(dataiter,allkeyArray,versions).toString();
		}
		return "";

	}
	
	private StringBuilder radarD3(Iterator<Map<String, Long>> dataiter,String[] allkeyArray){
		String line;
		long value;
		StringBuilder array;
		int i;
		String key;
		Map<String, Long> data;
		StringBuilder str= new StringBuilder();
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
		return str;
	}
	
	private StringBuilder areaChart(Iterator<Map<String, Long>> dataiter,String[] allkeyArray,List<Version> versions){
		Map<String, Long> data;
		String key;
		int i;
		StringBuilder array;
		String line;
		long value;
		Iterator<Version> versionsIter = versions.iterator();
		String name;
		StringBuilder str= new StringBuilder();

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
		PaprikaWebMain.LOGGER.trace(str);
		return str;
	}
	
	@Override
	public String toString() {
		return "[" + this.getName() + "," + this.getID() + "," + this.getNumberOfVersion() + "]";

	}
}
