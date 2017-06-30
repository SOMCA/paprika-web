package app.model.example;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.types.Node;

import app.application.PaprikaFacade;
import app.model.Project;
import app.model.Version;
import app.utils.PaprikaKeyWords;
import app.utils.neo4j.LowNode;

/**
 * ProjectExample is used when a new account have be created, for put a example.
 * 
 * 
 * @author guillaume
 *
 */
@SuppressWarnings("javadoc")
public class ProjectExample extends Project {
	public ProjectExample(String name, long id) {
		super(name, id);
	}

	@Override
	public String getGraph(String renderGraph, int numberVersion) {
		List<Version> versions = this.getLastXVersion(2);

		boolean example0 = false;
		boolean example1 = false;

		String str = "";
		for (Version version : versions) {

			if (version.getName().endsWith("0")) {
				example0 = true;
			}

			if (version.getName().endsWith("1")) {
				example1 = true;
			}
		}
		if ("radar".equals(renderGraph)) {
			if (example0) {
				str += "[{axis:\"MIM\",value:5},{axis:\"CC\",value:5},{axis:\"LM\",value:24},{axis:\"BLOB\",value:2},{axis:\"IGS\",value:2},{axis:\"NLMR\",value:1},{axis:\"LIC\",value:18},],";
			}
			if (example1) {
				str += "[{axis:\"MIM\",value:5},{axis:\"CC\",value:5},{axis:\"LM\",value:26},{axis:\"BLOB\",value:2},{axis:\"IGS\",value:2},{axis:\"NLMR\",value:1},{axis:\"LIC\",value:20},],";
			}
		} else if ("area".equals(renderGraph)) {
			if (example0) {
				str += "{version: 'Example_0', mim: 5, cc: 5, lm: 24, blob: 2, igs: 2, nlmr: 1, lic: 18, },";
			}
			if (example1) {
				str += "{version: 'Example_1', mim: 5, cc: 5, lm: 26, blob: 2, igs: 2, nlmr: 1, lic: 20, },";
			}
			str += "],   ykeys:['mim','cc','lm','blob','igs','nlmr','lic',],labels: ['MIM','CC','LM','BLOB','IGS','NLMR','LIC',],";
		}

		return str;

	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + this.getName() + "," + this.getID() + "," + this.getNumberOfVersion() + "]";
	}

	@Override
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
			boolean isFirst = true;
			while (iter.hasNext()) {
				record = iter.next();
				node = record.get(PaprikaKeyWords.NAMELABEL).asNode();

				name = node.get(PaprikaKeyWords.NAMEATTRIBUTE).asString();
				versions.add(new VersionExample(name, node.id(), isFirst));
				isFirst = false;
			}
			// A sort for be sure than all versions are sort with order
			versions.sort((Version v1, Version v2) -> (int) v2.getOrder() - (int) v1.getOrder());

			this.listofVersion = versions;
			return versions;
		} else
			return this.listofVersion;

	}
}
