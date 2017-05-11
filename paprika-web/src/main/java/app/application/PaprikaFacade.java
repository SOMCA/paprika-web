package app.application;

import java.net.InetAddress;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.RegistryAuth;

import app.functions.ApplicationFunctions;
import app.functions.UserFunctions;
import app.functions.VersionFunctions;
import app.model.*;
import app.utils.PaprikaKeyWords;
import app.utils.neo4j.Graph;
import app.utils.neo4j.LowNode;

public final class PaprikaFacade {

	/** Constructeur privé */
	private PaprikaFacade() {
	}

	/** Holder */
	private static class SingletonHolder {

		/** Instance unique non préinitialisée */
		private static final PaprikaFacade instance = new PaprikaFacade();

		private SingletonHolder() {
		}
	}

	/** Point d'accès pour l'instance unique du singleton */
	public static PaprikaFacade getInstance() {
		return SingletonHolder.instance;
	}

	public String salt() {
		Graph graph = new Graph();
		String salt = new UserFunctions().retrieveSalt();
		if (salt == null) {

			try (Transaction tx = PaprikaWebMain.getSession().beginTransaction()) {
				LowNode nodeKey = new LowNode(PaprikaKeyWords.LABELKEY);
				nodeKey.addParameter(PaprikaKeyWords.ATTRIBUTE_SALT, BCrypt.gensalt());

				tx.run(graph.create(nodeKey));
				tx.success();
			}
			salt = new UserFunctions().retrieveSalt();
		}
		return salt;
	}

	public User user(String email) {
		return new UserFunctions().foundUser(email);
	}

	public Application application(User user, String application) {
		ApplicationFunctions appFct = new ApplicationFunctions();

		return new Application(application, appFct.receiveIDOfApplication(user.getName(), application));
	}

	public Version version(Application application, String version) {
		VersionFunctions verFct = new VersionFunctions();

		return new Version(version, verFct.receiveIDOfVersion(application.getID(), version));
	}

	/**
	 * Ajoute une application de nom "project"
	 * 
	 * @param user
	 * @param project
	 */
	public void addProject(User user, String project) {
		ApplicationFunctions appFct = new ApplicationFunctions();

		if (project != null && appFct.receiveIDOfApplication(user.getName(), project) == -1) {
			appFct.writeApplicationOnUser(user.getName(), project);
		}

		return;
	}

	public void needReloadApp(Application application) {
		if (application != null)
			application.needReload();
	}

	/**
	 * Ajoute une version dans l'application d'id "idproject"
	 * 
	 * @param user
	 * @param tab
	 */
	public void addVersion(long idproject, String version) {
		VersionFunctions verFct = new VersionFunctions();
		if (version != null && verFct.receiveIDOfVersion(idproject, version) == -1) {
			verFct.writeVersion(idproject, version);
		}

		return;
	}

	/**
	 * Retourne la valeur d'un paramètre String donné d'un node qui contient un
	 * id.
	 * 
	 * @param idVersion
	 * @param parameter
	 * @return
	 */
	public String getParameter(LowNode lownode, String parameter) {
		StatementResult result;
		Graph graph = new Graph();
		try (Transaction tx = PaprikaWebMain.getSession().beginTransaction()) {
			result = tx.run(graph.matchSee(lownode));
			tx.success();
		}
		if (result != null && result.hasNext()) {
			Record record = result.next();
			Node node = record.get(PaprikaKeyWords.NAMELABEL).asNode();
			Value val = node.get(parameter);
			if (val != null && !val.isNull())
				return val.asString();
		}
		return null;
	}

	/**
	 * Applique ou créer une nouvelle valeur dans le node en question. Le node
	 * doit contenir une Id pour fonctionner.
	 * 
	 * @param nodeVer
	 * @param parameter
	 * @param key
	 */
	public void setParameterOnNode(LowNode node, String parameter, String attribute) {
		LowNode node2 = new LowNode(node.getLabel());
		Graph graph = new Graph();
		long id = node.getID();
		if (id == -1) {
			return;
		}
		node2.setId(id);
		node2.addParameter(parameter, attribute);
		try (Transaction tx = PaprikaWebMain.getSession().beginTransaction()) {
			tx.run(graph.set(node, node2));
			tx.success();
		}
	}

	/**
	 * Renvoie sous forme de liste, les fils d'un noeud à l'aide de son lownode
	 * et de la relation qui lie le noeud et ces fils de tels labels
	 * 
	 * @param email
	 */
	public List<Record> loadChildrenOfNode(LowNode lownode, String relation, String childrenLabel) {
		StatementResult result;
		LowNode nodeChildren = new LowNode(childrenLabel);
		Graph graph = new Graph();
		try (Transaction tx = PaprikaWebMain.getSession().beginTransaction()) {
			result = tx.run(graph.matchSee(lownode, nodeChildren, relation));
			tx.success();
		}
		return result.list();
	}

	/**
	 * signUp vérifie si l'utilisateur n'existe pas avant, puis signUp prend la
	 * clé de neo4J , créer le hashcode du mot de passe et ensuite créer le
	 * nouveau utilisateur.
	 * 
	 * @param email
	 * @param password
	 * @return
	 */
	public boolean signUp(String email, String password) {
		UserFunctions usrFct = new UserFunctions();
		if (usrFct.foundUser(email) != null) {
			return false;
		}
		String salt = this.salt();
		if (salt == null) {
			return false;
		}
		String newHashedPassword = BCrypt.hashpw(password, salt);

		usrFct.writeUser(email, newHashedPassword);

		return true;
	}


	public void callAnalyzeThread(LowNode nodeVer, String fname, Application application, User user, long size,boolean dockerContainer) {
		try {
			System.out.println("callAnalyzeThread:");
			String command = "java -jar Paprika-analyze.jar " + fname + " " + Long.toString(size) + " " + user.getName()
					+ " " + application.getName() + " " + Long.toString(application.getID()) + " "
					+ Long.toString(nodeVer.getID());
			System.out.println(command);
			
			if(!dockerContainer){
			Runtime.getRuntime().exec(command);
			System.out.println("Processus created");
			}
			else{
			RegistryAuth registryAuth = RegistryAuth.builder().serverAddress(getHostName()).build();
			DockerClient docker = DefaultDockerClient.fromEnv().dockerAuth(false).registryAuth(registryAuth).build();
			final HostConfig hostConfig = HostConfig.builder()
					.networkMode("paprikaweb_default")
					.links("neo4j-paprika","web-paprika")
					.volumesFrom("web-paprika")
					.build();
			ContainerConfig containerConfig = ContainerConfig.builder()
					.hostConfig(hostConfig)
					.image("paprika-analyze:latest")
			//fortest		.cmd("sh", "-c", "while :; do sleep 1; done")
					.cmd("java","-jar","Paprika-analyze.jar",
							fname,Long.toString(size), user.getName()
					, application.getName(),Long.toString(application.getID()),
					Long.toString(nodeVer.getID()))
					.workingDir("/dock")
					.build();
			ContainerCreation creation = docker.createContainer(containerConfig);
			
			String id = creation.id();
			docker.startContainer(id);
			docker.close();
			System.out.println("container create and start success");
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw new RuntimeException(e);

		}
	}

	private static String getHostName() {
		try {
			String str = InetAddress.getByName("web-paprika").getHostAddress();
			System.out.println(str);
			return str;
		} catch (final Exception e) {
			throw new Error(e);
		}
	}

}
