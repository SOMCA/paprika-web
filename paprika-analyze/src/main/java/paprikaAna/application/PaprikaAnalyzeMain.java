package paprikaAna.application;


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



	public static void main(String[] args) {
		int leng=args.length;
		if(leng!=8) return;
		
		String fName = args[0];
		long size = Long.parseLong(args[1]);
		System.out.println(args[1]);
	
		User user = new User(args[2],Long.parseLong(args[3]),args[4]);
		Application application = new Application(args[5], Long.parseLong(args[6]));
		LowNode nodeVer = new LowNode(PaprikaKeyWords.VERSIONLABEL);
		nodeVer.setId(Long.parseLong(args[7]));
		String pathstr = "application/" + user.getName() + "/" + application.getName() + "/" + fName;

		ApkFile apkfile = null;
		boolean flag = false;
		try {
			apkfile = new ApkFile(pathstr);
			flag = true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		if (flag) {

			PaprikaFacade facade = PaprikaFacade.getInstance();
			AnalyzeProcess anaThread = new AnalyzeProcess(apkfile, fName, application, user, size, nodeVer);
			facade.setParameterOnNode(nodeVer, PaprikaKeyWords.CODEA, "loading");
			anaThread.run();
		}
	}
}