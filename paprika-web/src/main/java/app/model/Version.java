package app.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;

import app.application.PaprikaFacade;
import app.application.PaprikaWebMain;
import app.functions.VersionFunctions;
import app.utils.PaprikaKeyWords;

public class Version extends Entity {

	private int analyzed;

	public Version(String name, long id) {
		super(name, id);
		this.checkAnalyzed();
	}

	public long getNumberCodeSmells() {
		VersionFunctions verfct = new VersionFunctions();
		long number = verfct.getNumberOfSmells(this.getID());

		if (number == 0) {
			Iterator<CodeSmells> iter = this.getAllCodeSmells();
			while (iter.hasNext()) {
				number += iter.next().getNumberOfSmells();
			}
			verfct.applyNumberOfCodeSmells(this.getID(), number);
		}
		return number;
	}

	public int checkAnalyzed() {
		PaprikaFacade facade = PaprikaFacade.getInstance();
		String ana = facade.getParameter(getID(), PaprikaKeyWords.CODEA);
		// null(0),loading(1) , inprogress(2), done(3)

		if (ana == null)
			this.analyzed = 0;
		else {
			switch (ana.charAt(0)) {
			case 'e':
				break;
			case 'l':
				this.analyzed = 1;
				break;
			case 'i':
				this.analyzed = 2;
				break;
			default:
				this.analyzed = 3;
				break;
			}
		}

		// PaprikaWebMain.LOGGER.trace(this.analyzed);
		if (this.analyzed == 3) {
			String path = facade.getParameter(getID(), "PathFile");
			if (path != null) {
				Path out = Paths.get(path);
				try {
					Files.deleteIfExists(out);
				} catch (IOException e) {
					PaprikaWebMain.LOGGER.error("deleteIfExists error", e);
				}
				removeUseless();
			}
		}
		// If pathfile is wrong or the file do not exist anymore
		if (ana != null && ana.charAt(0) == 'e') {
			removeUseless();
		}

		return this.analyzed;
	}

	private void removeUseless() {
		PaprikaFacade facade = PaprikaFacade.getInstance();

		facade.removeParameterOnNode(getID(), "PathFile");
		facade.removeParameterOnNode(getID(), "analyseInLoading");
		facade.removeContainer(facade.getParameter(getID(), "idContainer"));
		facade.removeParameterOnNode(getID(), "idContainer");
	}

	public int isAnalyzed() {
		return this.analyzed;
	}

	public String getAnalyseInLoading() {
		PaprikaFacade facade = PaprikaFacade.getInstance();
		return facade.getParameter(getID(), "analyseInLoading");
	}

	public long getOrder() {
		return new VersionFunctions().getOrder(this.getID());
	}

	public Iterator<CodeSmells> getAllCodeSmells() {

		List<CodeSmells> listNode = new ArrayList<>();

		StatementResult result = new VersionFunctions().loadDataCodeSmell(this);
		Record record;
		Node node;
		CodeSmells codesmell;
		String label;
		org.neo4j.driver.v1.Value number;
		while (result.hasNext()) {
			record = result.next();
			node = record.get(PaprikaKeyWords.NAMELABEL).asNode();
			number = node.get("number");

			if (number != null) {

				label = node.labels().iterator().next();
				codesmell = new CodeSmells(label, node.id(), number.asLong());

				listNode.add(codesmell);
			}

		}

		return listNode.iterator();
	}

}
