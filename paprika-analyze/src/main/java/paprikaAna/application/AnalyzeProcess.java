package paprikaAna.application;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import app.application.PaprikaFacade;
import app.functions.VersionFunctions;
import app.model.Application;
import app.model.User;
import app.utils.PaprikaKeyWords;
import app.utils.neo4j.LowNode;
import net.dongliu.apk.parser.ApkFile;
import paprikaAna.entities.PaprikaApp;
import paprikaAna.utils.neo4j.ModelToGraphBolt;

public class AnalyzeProcess{

	private ApkFile apkfile;
	private String fName;
	private Application application;
	private User user;
	private long size;
	private LowNode nodeVer;

	public AnalyzeProcess(ApkFile apkfile, String fName, Application application, User user, long size,
			LowNode nodeVer) {
		this.apkfile = apkfile;
		this.fName = fName;
		this.user = user;
		this.application = application;
		this.size = size;
		this.nodeVer = nodeVer;
	}


	public void run() {
		Analyse ana= new Analyse();
		this.runPartAnalyse(ana);
		this.runPartQuery(ana);
	}

	private void runPartAnalyse(Analyse ana) {
		PaprikaFacade facade = PaprikaFacade.getInstance();
		String appname = this.application.getName();

		System.out.println("=====================================");
		facade.setParameterOnNode(nodeVer, "analyseInLoading", "0");
		String realname = fName.substring(0, fName.lastIndexOf('.'));
		String pathstr = "application/" + this.user.getName() + "/" + appname + "/" + fName;
		String xml;
		try {
			xml = apkfile.getManifestXml();

			System.out.println(xml);

			String attributepackage = "package=";
			String attributeVersionName = "android:versionName=";
			String attributeVersionCode = "android:versionCode=\"400\"=";

			int indexP = xml.indexOf(attributepackage);
			int indexVn = xml.indexOf(attributeVersionName);
			int indexVc = xml.indexOf(attributeVersionCode);
			if (indexP != -1 && indexVn != -1) {

				indexVn += attributeVersionName.length() + 1;
				String strversionname = realname + "_" + xml.substring(indexVn, xml.indexOf('"', indexVn));

				if (!this.versionAlreadyExist(this.application.getID(), strversionname)) {

					indexVc += attributeVersionCode.length() + 1;
					String strversioncode = xml.substring(indexVc, xml.indexOf('"', indexVc));
					indexP += attributepackage.length() + 1;
					String strpackage = xml.substring(indexP, xml.indexOf('"', indexP));
					System.out.println("---");
					System.out.println(strpackage);
					String databasekey = this.user.getName() + "/" + appname + "/" + strversionname;
					String[] args = { "analyse", "-a", "android-platforms/", "-n", realname, "-p", strpackage, "-k",
							databasekey, "-dev", "unknowDevelopper", "-cat", "unknowCategory", "-nd", "1000", "-d",
							"1990-01-01", "-r", "250", "-s", Long.toString(size), "-u", "unsafe", "-omp", "True", "-vn",
							strversionname, "-vc", strversioncode, pathstr };

					facade.setParameterOnNode(nodeVer, "analyseInLoading", "10");
					PaprikaApp paprikaapp = ana.runAnalysis(args);

					
					facade.setParameterOnNode(nodeVer, "analyseInLoading", "50");

					System.out.println("Saving into database");
					ModelToGraphBolt modelToGraph = new ModelToGraphBolt();
					long idApp = modelToGraph.insertApp(paprikaapp, nodeVer).getID();

					System.out.println("Done");

					new VersionFunctions().writeAnalyzeOnVersion(nodeVer, idApp);
					
					facade.setParameterOnNode(nodeVer, PaprikaKeyWords.APPKEY, databasekey);
					Path out = Paths.get(pathstr);
					Files.deleteIfExists(out);
					apkfile.close();

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	private void runPartQuery(Analyse ana) {
		PaprikaFacade facade = PaprikaFacade.getInstance();

		String keyApp = facade.getParameter(nodeVer, PaprikaKeyWords.APPKEY);
		if (keyApp != null) {
			System.out.println("=====================================");

			try {

				String[] args = { "query", "-k", keyApp, "-r", "ALLAP" };

				new VersionFunctions().writeQueryOnVersion(nodeVer, keyApp);
				 ana.runQueryMode(args);
				// args[4] ="STATS";
				// app.application.HelloWorld.synchroQuery.runQueryMode(args);
				facade.setParameterOnNode(nodeVer, PaprikaKeyWords.CODEA, "done");
				facade.setParameterOnNode(nodeVer, "analyseInLoading", "100");

			} catch (Exception e) {

				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	private boolean versionAlreadyExist(long idapplication, String version) {
		if (new VersionFunctions().receiveIDOfVersion(idapplication, version) == -1) {
			return false;
		}

		return true;
	}

}