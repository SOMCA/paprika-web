package paprikaana.application;

import java.io.File;
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
	public static final Logger LOGGER = LogManager.getLogger();

	private static Driver driver = GraphDatabase.driver("bolt://" + getHostName() + ":7687",
			AuthTokens.basic("neo4j", "paprika"));

	private PaprikaAnalyzeMain() {
	}

	/**
	 * Prend le nom du container neo4j-praprika et renvoie son adresse.
	 * 
	 * @return
	 */
	private static String getHostName() {
		try {
			String str = InetAddress.getByName("neo4j-paprika").getHostAddress();
			PaprikaAnalyzeMain.LOGGER.trace(str);
			return str;
		} catch (final Exception e) {
			PaprikaAnalyzeMain.LOGGER.trace("Host of InetAddress 'neo4j-paprika' not found", e);
			return "localhost";
		}
	}

	public static Session getSession() {
		Session session = null;

		try {
			session = driver.session();
			LOGGER.trace("Open a new session.");
		} catch (ServiceUnavailableException e) {
			LOGGER.error("Driver problem, we re-open a driver.", e);
			driver.close();
			driver = GraphDatabase.driver("bolt://" + getHostName() + ":7687", AuthTokens.basic("neo4j", "paprika"));
			session = driver.session();
		}
		return session;
	}

	public static void main(String[] args) {
		int leng = args.length;
		for (int i = 0; i < leng; i++) {
			PaprikaAnalyzeMain.LOGGER.trace(args[i]);
		}
		if (leng != 5) {
			return;
		}
		PaprikaAnalyzeMain.LOGGER.trace("Launch Analyse");
		String fName = args[0];
		String user = args[1];
		Application application = new Application(args[2], Long.parseLong(args[3]));
		LowNode nodeVer = new LowNode(PaprikaKeyWords.VERSIONLABEL);
		nodeVer.setId(Long.parseLong(args[4]));
		String pathstr = "application/" + user + "/" + application.getName() + "/" + fName;

		ApkFile apkfile = null;
		PaprikaFacade facade = PaprikaFacade.getInstance();
		try {
			File file = new File(pathstr);
			long size = file.length();

			apkfile = new ApkFile(pathstr);
			String xml = apkfile.getManifestXml();
			apkfile.close();

			AnalyzeProcess anaThread = new AnalyzeProcess(xml, fName, application, user, size, nodeVer);

			facade.setParameterOnNode(nodeVer.getID(), PaprikaKeyWords.CODEA, "inprogress");
			anaThread.run();
		} catch (IOException e) {
			PaprikaAnalyzeMain.LOGGER.error("IOException error: File not found", e);
			facade.setParameterOnNode(nodeVer.getID(), PaprikaKeyWords.CODEA, "error");
			throw new AnalyseException();
		}
	}
}