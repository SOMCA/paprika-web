package paprikaana.application;


import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;

import app.application.PaprikaFacade;
import app.model.*;
import app.utils.PaprikaKeyWords;
import app.utils.neo4j.LowNode;
import net.dongliu.apk.parser.ApkFile;

public class PaprikaAnalyzeMain {

	public static final Driver driver = GraphDatabase.driver("bolt://localhost:7687",
			AuthTokens.basic("neo4j", "neo4j"));

	public static final Logger LOGGER = Logger.getLogger(PaprikaAnalyzeMain.class.getName());

	private PaprikaAnalyzeMain(){
		
	}

	public static void main(String[] args) {
		int leng=args.length;
		if(leng!=8) {
			return;
		}
		
		String fName = args[0];
		long size = Long.parseLong(args[1]);

		User user = new User(args[2],Long.parseLong(args[3]),args[4]);
		Application application = new Application(args[5], Long.parseLong(args[6]));
		LowNode nodeVer = new LowNode(PaprikaKeyWords.VERSIONLABEL);
		nodeVer.setId(Long.parseLong(args[7]));
		String pathstr = "application/" + user.getName() + "/" + application.getName() + "/" + fName;

		ApkFile apkfile = null;
		try {
			apkfile = new ApkFile(pathstr);
			PaprikaFacade facade = PaprikaFacade.getInstance();
			AnalyzeProcess anaThread = new AnalyzeProcess(apkfile, fName, application, user, size, nodeVer);
			facade.setParameterOnNode(nodeVer, PaprikaKeyWords.CODEA, "loading");
			anaThread.run();
		} catch (IOException e) {
			PaprikaAnalyzeMain.LOGGER.log(Level.SEVERE,"main: IOException",e);
			throw new AnalyseException();
		}
	}
}