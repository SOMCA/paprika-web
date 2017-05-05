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
	 * @return
	 */
	public int getNumberOfVersionReal() {
		if (reload)
			return listofVersion.size();
		else
			return getListVersionApplications().size();
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
				versions.add(facade.version(this, name));
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
		/*
		while (numberVersion > number) {
			version = versions.next();
			numberVersion--;
		}
		*/
		while (versions.hasNext()) {
			version = versions.next();
			if (version == null || numberVersion<=0)
				return lastVersion;
			if (version.isAnalyzed()) {
				System.out.println(version.getName());
				lastVersion.add(version);
				numberVersion--;
			}
		}
		System.out.println("---");
		return lastVersion;
	}

	public boolean isApplicationHaveAnalysedVersion() {
		if (!reload) {
			return false;
		} else if (this.getNumberOfVersionReal() != 0) {
			for (Version version : listofVersion) {
				if (version.isAnalyzed())
					return true;
			}
		}
		return false;
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

	/**
	 * Besoin de refaire en beaucoup beaucoup plus simple
	 * 
	 * @return
	 */
	public String getRadarD3() {
		StringBuilder str = new StringBuilder();
		List<Version> versions = getLastXVersion(3);
		List<Map<String, Long>> datas = getDataGraph(versions.iterator());
		Map<String, Long> data;
		String key;
		int i;
		StringBuilder array;
		String line;
		long value;
		String[] allkeyArray = getKey(datas);
		Iterator<Map<String, Long>> dataiter = datas.iterator();

		// Partie 3, création du string
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

		return str.toString();
	}

	public String getAreaChart() {
		StringBuilder str = new StringBuilder();

		List<Version> versions = getLastXVersion(10);
		List<Map<String, Long>> datas = getDataGraph(versions.iterator());
		if (datas.isEmpty())
			return "";
		Map<String, Long> data;
		int i;
		StringBuilder array = new StringBuilder("['Version'");
		String[] allkeyArray = getKey(datas);
		Iterator<Map<String, Long>> dataiter = datas.iterator();
		for (i = 0; i < allkeyArray.length; i++) {
			array.append(",'" + allkeyArray[i] + "'");
		}
		array.append("],");
		str.append(array);

		Long value;
		String name;
		int length;
		int lengthstr = str.length();
		Iterator<Version> versionsiter = versions.iterator();
		while (versionsiter.hasNext()) {
			name = versionsiter.next().getName();
			length = name.length();
			if (length > 10) {
				name = ".." + name.substring(length - 7, length);
			}
			array = new StringBuilder("['" + name + "'");
			data = dataiter.next();
			for (i = 0; i < allkeyArray.length; i++) {
				value = data.get(allkeyArray[i]);
				if (value == null)
					value = Long.valueOf(0);
				array.append("," + value.toString() + "");

			}
			array.append("],");
			// Pour insérer dans le sens inverse
			str.insert(lengthstr, array);
			// str.append(array);
		}

		// ['Version', 'LM', 'BLOB','CC'],
		// ['test v1.0', 50, 1,2],
		// ['test v1.1', 60, 3,6],
		// ['test v1.3', 24, 2,4],
		// ['test v1.6', 10, 0,0],
		// ['test v2.0', 60, 3,6],
		// ['test v2.2', 24, 2,4]
		return str.toString();
	}
}
