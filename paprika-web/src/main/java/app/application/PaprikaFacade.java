package app.application;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mindrot.jbcrypt.BCrypt;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
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

	public Application application(long application) {
		ApplicationFunctions appFct = new ApplicationFunctions();

		return new Application(appFct.receiveOf(application), application);
	}

	public Version version(long version) {
		VersionFunctions verFct = new VersionFunctions();

		return new Version(verFct.receiveOf(version), version);
	}

	/**
	 * Ajoute une application de nom "project"
	 * 
	 * @param user
	 * @param project
	 */
	public long addProject(User user, String project) {
		ApplicationFunctions appFct = new ApplicationFunctions();
		long idProject = -1;
		if (project != null && appFct.receiveIDOfApplication(user.getName(), project) == -1) {
			idProject = appFct.writeApplicationOnUser(user.getName(), project);
		}

		return idProject;
	}

	public void needReloadApp(Application application) {
		if (application != null)
			application.needReload();
	}
	public void reloadVersion(Version version){
		version.checkAnalyzed();
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
	public String getParameter(long idNode, String parameter) {
		StatementResult result = null;
		try (Transaction tx = PaprikaWebMain.getSession().beginTransaction()) {
			result = tx.run("MATCH (n) WHERE ID(n) = " + idNode + " RETURN n");
			tx.success();
		}
		if (result != null && result.hasNext()) {
			Record record = result.next();
			if (!record.get("n").isNull()) {
				Node node = record.get("n").asNode();
				Value val = node.get(parameter);
				if (!val.isNull())
					return val.asString();
			}
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
	public void setParameterOnNode(long idnode, String parameter, String attribute) {
		if (idnode == -1) {
			return;
		}
		try (Transaction tx = PaprikaWebMain.getSession().beginTransaction()) {
			tx.run("MATCH (n) WHERE ID(n)= "+idnode+" SET n+={"+parameter+":\""+attribute+"\"}");
			
			tx.success();
		}
	}
	/**
	 * Retire une propriété du node.
	 * 
	 * @param idnode
	 * @param parameter
	 */
	public void removeParameterOnNode(long idnode, String parameter) {
		if (idnode == -1) {
			return;
		}
		try (Transaction tx = PaprikaWebMain.getSession().beginTransaction()) {
			tx.run("MATCH (n) WHERE ID(n)= "+idnode+" REMOVE n."+parameter);
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

	public void callAnalyzeThread(long idNode, String fname, Application application, User user, long size,
			boolean dockerContainer) {
		this.setParameterOnNode(idNode, PaprikaKeyWords.CODEA, "loading");
		this.setParameterOnNode(idNode, "analyseInLoading", "0");
		try {
			System.out.println("callAnalyzeThread:");
			String command = "java -jar Paprika-analyze.jar " + fname + " " + Long.toString(size) + " " + user.getName()
					+ " " + application.getName() + " " + Long.toString(application.getID()) + " "
					+ Long.toString(idNode);
			System.out.println(command);
			String pathstr = "application/" + user.getName() + "/" + application.getName()+ "/" + fname;
			this.setParameterOnNode(idNode,"PathFile", pathstr);
			if (!dockerContainer) {
				Runtime.getRuntime().exec(command);
				System.out.println("Processus created");
			} else {
				RegistryAuth registryAuth = RegistryAuth.builder().serverAddress(getHostName()).build();
				DockerClient docker = DefaultDockerClient.fromEnv().dockerAuth(false).registryAuth(registryAuth)
						.build();
			
				final HostConfig hostConfig = HostConfig.builder().networkMode("paprikaweb_default")
						.links("neo4j-paprika", "web-paprika")
						.binds("/tmp/application:/dock/application:ro")
						//
						//.volumesFrom("web-paprika")
						.build();
				ContainerConfig containerConfig = ContainerConfig.builder().hostConfig(hostConfig)
						.image("paprika-analyze:latest")
						// fortest .cmd("sh", "-c", "while :; do sleep 1; done")
						.cmd("java", "-jar", "Paprika-analyze.jar", fname, Long.toString(size), user.getName(),
								application.getName(), Long.toString(application.getID()), Long.toString(idNode))
						.workingDir("/dock").build();
				ContainerCreation creation = docker.createContainer(containerConfig);

				String id = creation.id();
				docker.startContainer(id);

				docker.close();
				System.out.println("container create and start success");
				this.setParameterOnNode(idNode, "idContainer",id);
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw new RuntimeException(e);
		}
	}
	public void removeContainer(String id){
		RegistryAuth registryAuth = RegistryAuth.builder().serverAddress(getHostName()).build();
		DockerClient docker;
		try {
			docker = DefaultDockerClient.fromEnv().dockerAuth(false).registryAuth(registryAuth)
					.build();
			docker.removeContainer(id);
		} catch (DockerCertificateException e) {
			System.out.println("DockerCertificateException");
		} catch (DockerException e) {
			System.out.println("DockerException");
		} catch (InterruptedException e) {
			System.out.println("InterruptedException");
		}
		
	}

	public void deleteOnDataBase(Set<String> setOfId) {
		Set<String> versionsToDelete = new HashSet<>();
		StatementResult result;
		Record record;
		String begin;
		String print;

		try (Transaction tx = PaprikaWebMain.getSession().beginTransaction()) {
			for (String idAppli : setOfId) {
				begin = "MATCH (n:Project) WHERE ID(n) = " + idAppli;
				result = tx.run(begin + " RETURN n");
				if (result.hasNext()) {
					// Implique que l'id provient d'un projet
					print = begin + " MATCH (n)-[:" + PaprikaKeyWords.REL_PROJECT_VERSION + "]->(v) RETURN v";
					System.out.println(print);
					result = tx.run(print);

					while (result.hasNext()) {
						record = result.next();
						Value value = record.get("v");

						if (!value.isNull()) {
							String idv = Long.toString(value.asNode().id());
							System.out.println(idv);
							versionsToDelete.add(idv);
						}
					}
					print = begin + " MATCH(:" + PaprikaKeyWords.LABELUSER + ")-[r:" + PaprikaKeyWords.REL_USER_PROJECT
							+ "]->(n) DELETE r";
					System.out.println(print);
					tx.run(print);
					print = begin + " MATCH (n)-[r:" + PaprikaKeyWords.REL_PROJECT_VERSION + "]->(v:Version) DELETE r";
					System.out.println(print);
					tx.run(print);

					tx.run(begin + " DELETE n");

				} else
					versionsToDelete.add(idAppli);

			}
			/*
			 * Dû au fait qu'on ne change pas le "nb_ver" est normal, sinon on
			 * doit aussi modifier l'ordre de toutes les versions restantes.
			 * Mais surtout, pas mal de transaction qui coûtent.
			 */

			for (String idVersion : versionsToDelete) {
				String path=this.getParameter(Long.parseLong(idVersion), "PathFile");
				if(path!=null){
					Path out = Paths.get(path);
					try {
						Files.deleteIfExists(out);
					} catch (IOException e) {
					}
				}
				
				
				print = "MATCH (p) WHERE ID(p)= " + idVersion + " MATCH(:" + PaprikaKeyWords.LABELPROJECT + ")-[r:"
						+ PaprikaKeyWords.REL_PROJECT_VERSION + "]->(p) DELETE r";
				System.out.println(print);
				tx.run(print);
				print = "MATCH (p) WHERE p.app_key=" + idVersion + " DETACH DELETE p";
				System.out.println(print);
				tx.run(print);
				
				print = "MATCH (p:Version) WHERE ID(p)=" + idVersion + " DELETE p";
				System.out.println(print);
				tx.run(print);
			}

			tx.success();
		}

		/*
		 * On sépare application et versions, On ajoute les versions de
		 * l'application dans le set des versions et supprime l'application.
		 * puis on supprime les versions avec aussi leur
		 * clé(ex:app_key:idVersion), si elle existe. pour supprimer les classes
		 * non reliés(rare, mais cela existe)
		 */
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
