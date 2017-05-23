package app.application;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.Part;

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

import app.exception.PapWebRunTimeException;
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

	public void reloadVersion(Version version) {
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
	 * Retourne le nom de l'entitée.
	 * 
	 * @param entity
	 * @return
	 */
	public String getEntityName(Entity entity) {
		return entity.getName();
	}

	/**
	 * Retourne l'id de l'entitée.
	 * 
	 * @param entity
	 * @return
	 */
	public long getEntityID(Entity entity) {
		return entity.getID();
	}

	public String getUserHash(User user) {
		return user.getHashedPassword();
	}

	public int getVersionAnalyzed(Version version) {
		return version.isAnalyzed();
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
			tx.run("MATCH (n) WHERE ID(n)= " + idnode + " SET n+={" + parameter + ":\"" + attribute + "\"}");

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
			tx.run("MATCH (n) WHERE ID(n)= " + idnode + " REMOVE n." + parameter);
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

	public void callAnalyzeThread(long idNode, String fname, Application application, User user,
			boolean dockerContainer) {
		this.setParameterOnNode(idNode, PaprikaKeyWords.CODEA, "loading");
		this.setParameterOnNode(idNode, "analyseInLoading", "0");
		try {
			PaprikaWebMain.LOGGER.trace("callAnalyzeThread:");
			String command = "java -jar Paprika-analyze.jar " + fname + " " + user.getName() + " "
					+ application.getName() + " " + Long.toString(application.getID()) + " " + Long.toString(idNode);
			PaprikaWebMain.LOGGER.trace(command);
			String pathstr = "application/" + user.getName() + "/" + application.getName() + "/" + fname;
			this.setParameterOnNode(idNode, "PathFile", pathstr);
			if (!dockerContainer) {
				Runtime.getRuntime().exec(command);
				PaprikaWebMain.LOGGER.trace("Processus created");
			} else {
				RegistryAuth registryAuth = RegistryAuth.builder().serverAddress(getHostName()).build();
				DockerClient docker = DefaultDockerClient.fromEnv().dockerAuth(false).registryAuth(registryAuth)
						.build();

				final HostConfig hostConfig = HostConfig.builder().networkMode("paprikaweb_default")
						.links("neo4j-paprika", "web-paprika").binds("/tmp/application:/dock/application:ro")
						//
						// .volumesFrom("web-paprika")
						.build();
				ContainerConfig containerConfig = ContainerConfig.builder().hostConfig(hostConfig)
						.image("paprika-analyze:latest")
						// fortest .cmd("sh", "-c", "while :; do sleep 1; done")
						.cmd("java", "-jar", "Paprika-analyze.jar", fname, user.getName(), application.getName(),
								Long.toString(application.getID()), Long.toString(idNode))
						.workingDir("/dock").build();
				ContainerCreation creation = docker.createContainer(containerConfig);

				String id = creation.id();
				docker.startContainer(id);

				docker.close();
				PaprikaWebMain.LOGGER.trace("container create and start success");
				this.setParameterOnNode(idNode, "idContainer", id);
			}
		} catch (Exception e) {
			PaprikaWebMain.LOGGER.error(e.getMessage(), e);
			throw new PapWebRunTimeException(e.getMessage());
		}
	}

	public void removeContainer(String id) {
		try {
			RegistryAuth registryAuth = RegistryAuth.builder().serverAddress(getHostName()).build();
			DockerClient docker;

			docker = DefaultDockerClient.fromEnv().dockerAuth(false).registryAuth(registryAuth).build();
			docker.removeContainer(id);

		} catch (Exception e) {
			PaprikaWebMain.LOGGER.error(e.getMessage(), e);
			throw new PapWebRunTimeException(e.getMessage());
		}
		PaprikaWebMain.LOGGER.trace("Work?");
	}

	public void deleteOnDataBase(Set<String> setOfId) throws IOException {
		Set<String> versionsToDelete = new HashSet<>();

		try (Transaction tx = PaprikaWebMain.getSession().beginTransaction()) {
			for (String idAppli : setOfId) {
				versionsToDelete = deleteAppliOnDataBase(tx, idAppli, versionsToDelete);
			}
			deleteVersionsOnDataBase(tx, versionsToDelete);

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

	private Set<String> deleteAppliOnDataBase(Transaction tx, String idproject, Set<String> versionsToDelete) {
		StatementResult result;
		Record record;
		String begin;
		begin = "MATCH (n:Project) WHERE ID(n) = " + idproject;
		result = tx.run(begin + " RETURN n");
		if (result.hasNext()) {
			result = tx.run(begin + " MATCH (n)-[:" + PaprikaKeyWords.REL_PROJECT_VERSION + "]->(v) RETURN v");

			while (result.hasNext()) {
				record = result.next();
				Value value = record.get("v");

				if (!value.isNull()) {
					String idv = Long.toString(value.asNode().id());
					PaprikaWebMain.LOGGER.trace(idv);
					versionsToDelete.add(idv);
				}
			}
			tx.run(begin + " MATCH(:" + PaprikaKeyWords.LABELUSER + ")-[r:" + PaprikaKeyWords.REL_USER_PROJECT
					+ "]->(n) DELETE r");

			tx.run(begin + " MATCH (n)-[r:" + PaprikaKeyWords.REL_PROJECT_VERSION + "]->(v:Version) DELETE r");

			tx.run(begin + " DELETE n");
		} else
			versionsToDelete.add(idproject);

		return versionsToDelete;
	}

	/**
	 * Supprime toutes les versions du set dans neo4J et tous leurs sous-fils.
	 * 
	 * @param tx
	 * @param versionsToDelete
	 * @throws IOException
	 */
	private void deleteVersionsOnDataBase(Transaction tx, Set<String> versionsToDelete) throws IOException {
		/*
		 * Dû au fait qu'on ne change pas le "nb_ver" est normal, sinon on doit
		 * aussi modifier l'ordre de toutes les versions restantes. Mais
		 * surtout, pas mal de transaction qui coûtent.
		 */

		for (String idVersion : versionsToDelete) {
			String path = this.getParameter(Long.parseLong(idVersion), "PathFile");
			if (path != null) {
				Path out = Paths.get(path);
				try {
					Files.deleteIfExists(out);
				} catch (IOException e) {
					PaprikaWebMain.LOGGER.error("Files.deleteIfExists", e);
					throw new IOException(e);
				}
			}

			tx.run("MATCH (p) WHERE ID(p)= " + idVersion + " MATCH(:" + PaprikaKeyWords.LABELPROJECT + ")-[r:"
					+ PaprikaKeyWords.REL_PROJECT_VERSION + "]->(p) DELETE r");
			tx.run("MATCH (p) WHERE p.app_key=" + idVersion + " DETACH DELETE p");

			tx.run("MATCH (p:Version) WHERE ID(p)=" + idVersion + " DELETE p");
		}

	}

	private String getHostName() throws UnknownHostException {
		try {
			String str = InetAddress.getByName("web-paprika").getHostAddress();
			PaprikaWebMain.LOGGER.trace(str);
			return str;
		} catch (UnknownHostException e) {
			PaprikaWebMain.LOGGER.error("InetAddress.getByName(string) Error", e);
			throw new UnknownHostException();
		}
	}

	public void addFile(String currentUser, Application application, String fName, Part uploadedFile, String realname)
			throws IOException {
		String pathstr = PaprikaKeyWords.REPERTORY + currentUser + '/' + application.getName() + '/' + fName;
		Path out = Paths.get(pathstr);
		File file = new File(pathstr);
		file.mkdirs();
		file = null;
		Files.deleteIfExists(out);
		try (final InputStream in = uploadedFile.getInputStream()) {
			Files.copy(in, out);
			uploadedFile.delete();
		}

		this.addVersion(application.getID(), realname);
		this.needReloadApp(application);
	}

}
