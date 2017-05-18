package paprikaana.application;


import java.io.IOException;
import java.net.InetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;
import paprikaana.model.*;

import net.dongliu.apk.parser.ApkFile;
import paprikaana.utils.neo4j.LowNode;
import paprikaana.utils.neo4j.PaprikaKeyWords;

public class PaprikaAnalyzeMain {

	private static Driver driver = GraphDatabase.driver("bolt://" + getHostName() + ":7687",
			AuthTokens.basic("neo4j", "paprika"));

	/**
	 * Prend le nom du container neo4j-praprika et renvoie son adresse.
	 * 
	 * @return
	 */
	private static String getHostName() {
		try {
			String str=InetAddress.getByName("neo4j-paprika").getHostAddress();
			System.out.println(str);
			return str;
		} catch (Exception e) {
			return "localhost";
		}
	}
	public static final Logger LOGGER = LogManager.getLogger(PaprikaAnalyzeMain.class);//.getLog(PaprikaAnalyzeMain.class.getName());//.getLogger(PaprikaAnalyzeMain.class.getName());

	private PaprikaAnalyzeMain(){
	}
	
	public static Session getSession(){
		Session session=null;

		try{
		 session =driver.session();
		 LOGGER.trace("Open a new session.");
		}
		catch(ServiceUnavailableException e){
			LOGGER.error("Driver problem, we re-open a driver.");
			driver.close();
			driver = GraphDatabase.driver("bolt://" + getHostName() + ":7687",
					AuthTokens.basic("neo4j", "paprika"));
			 session =driver.session();
		}
		return session;
	}

	public static void main(String[] args) {
		int leng=args.length;
		for(int i=0;i<leng;i++)System.out.println(args[i]);
		if(leng!=6) {
			return;
		}
		LOGGER.trace("Launch Analyse");
		String fName = args[0];
		long size = Long.parseLong(args[1]);

		String user = args[2];
		Application application = new Application(args[3], Long.parseLong(args[4]));
		LowNode nodeVer = new LowNode(PaprikaKeyWords.VERSIONLABEL);
		nodeVer.setId(Long.parseLong(args[5]));
		String pathstr = "application/" + user + "/" + application.getName() + "/" + fName;

		ApkFile apkfile = null;
		PaprikaFacade facade = PaprikaFacade.getInstance();
		try {
			apkfile = new ApkFile(pathstr);
			
			AnalyzeProcess anaThread = new AnalyzeProcess(apkfile, fName, application, user, size, nodeVer);
			facade.setParameterOnNode(nodeVer.getID(), PaprikaKeyWords.CODEA, "inprogress");
			anaThread.run();
		} catch (IOException e) {
			LOGGER.error("IOException error: File not found");
			facade.setParameterOnNode(nodeVer.getID(), PaprikaKeyWords.CODEA, "error");
			throw new AnalyseException();
		}
	}
}