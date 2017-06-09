package spoon.main;

import java.io.IOException;
import java.net.InetAddress;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;

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
		if (args.length != 2)
			return;

		Paprika_analyze analyze = null;
		try {
			analyze = new Paprika_analyze(args[0], args[1]);
			analyze.run();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}


	}

}