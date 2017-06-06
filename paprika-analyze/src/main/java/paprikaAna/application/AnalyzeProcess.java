package paprikaana.application;


import paprikaana.entities.PaprikaApp;
import paprikaana.functions.VersionFunctions;
import paprikaana.utils.neo4j.LowNode;
import paprikaana.utils.neo4j.ModelToGraphBolt;
import paprikaana.utils.neo4j.PaprikaKeyWords;

public class AnalyzeProcess {

	private String xml;
	private String fName;
	private String project;
	private String user;
	private long size;
	private LowNode nodeVer;

	public AnalyzeProcess(String xml, String fName, String project, String user, long size,
			LowNode nodeVer) {
		this.xml = xml;
		this.fName = fName;
		this.user = user;
		this.project = project;
		this.size = size;
		this.nodeVer = nodeVer;
	}
	public void run() {
		Analyse ana = new Analyse();
		PaprikaAnalyzeMain.LOGGER.trace("Analyse part");
		this.runPartAnalyse(ana);
		PaprikaAnalyzeMain.LOGGER.trace("Query part");
		this.runPartQuery(ana);
	}

	private void runPartAnalyse(Analyse ana) {
		PaprikaFacade facade = PaprikaFacade.getInstance();
	
		
		String realname = fName.substring(0, fName.lastIndexOf('.'));
		String pathstr = "application/" + this.user + "/" + this.project + "/" + fName;

		try {
			String attributepackage = "package=";
			String attributeVersionName = "android:versionName=";
			String attributeVersionCode = "android:versionCode=\"400\"=";

			int indexP = xml.indexOf(attributepackage);
			int indexVn = xml.indexOf(attributeVersionName);
			int indexVc = xml.indexOf(attributeVersionCode);
			if (indexP != -1 && indexVn != -1) {

				indexVn += attributeVersionName.length() + 1;
				String strversionname = realname + "_" + xml.substring(indexVn, xml.indexOf('"', indexVn));

		
					indexVc += attributeVersionCode.length() + 1;
					String strversioncode = xml.substring(indexVc, xml.indexOf('"', indexVc));
					indexP += attributepackage.length() + 1;
					String strpackage = xml.substring(indexP, xml.indexOf('"', indexP));
					String databasekey = this.user+ "/" + this.project  + "/" + strversionname;
					String[] args = { "analyse", "-a", "android-platforms/", "-n", realname, "-p", strpackage, "-k",
							databasekey, "-dev", "unknowDevelopper", "-cat", "unknowCategory", "-nd", "1000", "-d",
							"1990-01-01", "-r", "250", "-s", Long.toString(size), "-u", "unsafe", "-omp", "True", "-vn",
							strversionname, "-vc", strversioncode, pathstr };

					facade.setParameterOnNode(nodeVer.getID(),PaprikaKeyWords.ANALYSEINLOAD, "10");
					PaprikaApp paprikaapp;
					paprikaapp = ana.runAnalysis(args);

					facade.setParameterOnNode(nodeVer.getID(),PaprikaKeyWords.ANALYSEINLOAD, "50");

					ModelToGraphBolt modelToGraph = new ModelToGraphBolt();
					long idApp = modelToGraph.insertApp(paprikaapp, nodeVer).getID();

					new VersionFunctions().writeAnalyzeOnVersion(nodeVer, idApp);

					facade.setParameterOnNode(nodeVer.getID(), PaprikaKeyWords.APPKEY, Long.toString(nodeVer.getID()));

	
					

			}
		} catch (AnalyseException e) {
			PaprikaAnalyzeMain.LOGGER.error("runPartAnalyse: AnalyseException", e);
			throw new AnalyseException();
		}

	}

	private void runPartQuery(Analyse ana) {
		PaprikaFacade facade = PaprikaFacade.getInstance();

		long keyApp = nodeVer.getID();
			String[] args = { "query", "-k", Long.toString(keyApp), "-r", "ALLAP" };
			new VersionFunctions().writeQueryOnVersion(nodeVer, keyApp);
			ana.runQueryMode(args);
			facade.setParameterOnNode(nodeVer.getID(), PaprikaKeyWords.CODEA, "done");
			facade.setParameterOnNode(nodeVer.getID(), "analyseInLoading", "100");
	}

	

}