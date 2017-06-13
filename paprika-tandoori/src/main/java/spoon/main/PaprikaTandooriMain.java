package spoon.main;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;

import spoon.functions.VersionFunctions;
import spoon.utils.neo4j.LowNode;
import spoon.utils.neo4j.PaprikaKeyWords;

/**
 * 
 * 
 * @author guillaume
 *
 */
public class PaprikaTandooriMain {

	private static Driver driver = GraphDatabase.driver("bolt://" + getHostName() + ":7687",
			AuthTokens.basic("neo4j", "paprika"));

	private PaprikaTandooriMain() {
	}

	/**
	 * Prend le nom du container neo4j-praprika et renvoie son adresse.
	 * 
	 * @return
	 */
	private static String getHostName() {
		try {
			String str = InetAddress.getByName("neo4j-paprika").getHostAddress();
			return str;
		} catch (final Exception e) {
			return "localhost";
		}
	}

	public static Session getSession() {
		Session session = null;

		try {
			session = driver.session();
		} catch (ServiceUnavailableException e) {
			driver.close();
			driver = GraphDatabase.driver("bolt://" + getHostName() + ":7687", AuthTokens.basic("neo4j", "paprika"));
			session = driver.session();
		}
		return session;
	}

	public static void main(String[] args) {
		// 0 : fname
		// 1 : version node id
		// 2 : github link

		if (args.length != 3)
			return;

		String fName = args[0];
		LowNode nodeVer = new LowNode(PaprikaKeyWords.VERSIONLABEL);
		nodeVer.setId(Long.parseLong(args[1]));

		String github = args[2];

		VersionFunctions verFct = new VersionFunctions();

		AnalyzeProcess anaThread;
		try {
			anaThread = new AnalyzeProcess(fName, nodeVer, github);
			verFct.setParameterOnNode(nodeVer.getID(), PaprikaKeyWords.CODEA, "inprogress");
			anaThread.run();
		} catch (IOException e) {
			verFct.setParameterOnNode(nodeVer.getID(), PaprikaKeyWords.CODEA, "error");
		}

		// VersionFunctions verFct = new VersionFunctions();
		// Paprika_analyze analyze = null;
		//
		// try {
		// analyze = new Paprika_analyze(args[0], Long.parseLong(args[1]));
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// if (analyze != null) {
		// verFct.setParameterOnNode(Long.parseLong(args[1]),
		// PaprikaKeyWords.CODEA, "inprogress");
		// if (analyze.run()) {
		// verFct.setParameterOnNode(Long.parseLong(args[1]),
		// PaprikaKeyWords.CODEA, "error");
		// }
		// } else {
		// verFct.setParameterOnNode(Long.parseLong(args[1]),
		// PaprikaKeyWords.CODEA, "error");
		//
		// }

	}

}
