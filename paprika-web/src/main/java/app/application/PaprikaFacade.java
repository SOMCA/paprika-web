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

/**
 * PaprikaFacade is a singleton + Facade. All controller who need use method of
 * the Paprika-web pass per this facade. This singleton keep many problem of
 * refactoring except the size of this class.
 * 
 * @author guillaume
 * 
 */
public final class PaprikaFacade {

	/** Private constructor*/
	private PaprikaFacade() {
	}

	/** Holder */
	private static class SingletonHolder {

		/** Unique instance no pre-initialized */
		private static final PaprikaFacade instance = new PaprikaFacade();

		private SingletonHolder() {
		}
	}

	/**
	 * Access point to the unique instance of this singleton
	 * @return the class.
	 */
	public static PaprikaFacade getInstance() {
		return SingletonHolder.instance;
	}

	/**
	 * return the salt of the Paprika-web, if do not exist, create a new salt.
	 * 
	 * @return the salt.
	 */
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

	/**
	 * Found the User of id email
	 * 
	 * @param email
	 *            the email of the user.
	 * @return the user of the email
	 */
	public User user(String email) {
		return new UserFunctions().foundUser(email);
	}

	/**
	 * Found the Application of id project
	 * 
	 * @param application
	 *            id of the project.
	 * @return the application of the id
	 */
	public Application application(long application) {
		ApplicationFunctions appFct = new ApplicationFunctions();

		return new Application(appFct.receiveOf(application), application);
	}

	/**
	 * Found the Version of id version
	 * 
	 * @param version
	 *            id of the version.
	 * @return the version of the id
	 */
	public Version version(long version) {
		VersionFunctions verFct = new VersionFunctions();

		return new Version(verFct.receiveOf(version), version);
	}

	/**
	 * Add a project of name project for the user.
	 * 
	 * @param user
	 *            CurrentUser.
	 * @param project
	 *            Name of the project
	 * @return return the id of the project.
	 */
	public long addProject(User user, String project) {
		ApplicationFunctions appFct = new ApplicationFunctions();
		long idProject = -1;
		if (project != null && appFct.receiveIDOfApplication(user.getName(), project) == -1) {
			idProject = appFct.writeApplicationOnUser(user.getName(), project);
		}

		return idProject;
	}

	/**
	 * Put the project to need be reload.
	 * 
	 * @param application
	 *            the project.
	 */
	public void needReloadApp(Application application) {
		if (application != null)
			application.needReload();
	}

	/**
	 * Reload the version for update the page.
	 * 
	 * @param version
	 *            The version to update.
	 */
	public void reloadVersion(Version version) {
		if (version != null)
			version.checkAnalyzed();
	}

	/**
	 * Add a new Version on the project of id idproject.
	 * 
	 * @param idproject
	 *            the id of the project
	 * @param version
	 *            the name of the new version.
	 * 
	 */
	public void addVersion(long idproject, String version) {
		VersionFunctions verFct = new VersionFunctions();
		if (version != null && verFct.receiveIDOfVersion(idproject, version) == -1) {
			verFct.writeVersion(idproject, version);
		}

		return;
	}

	/**
	 * Return the value of the properties put on parameter of the unknow node of
	 * id idNode.
	 * 
	 * @param idNode
	 *            id of a node.
	 * @param parameter
	 *            properties where you want obtain the value.
	 * @return The value as a String
	 * 
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
	 * Return the method getName of Entity
	 * 
	 * @param entity
	 *            Entity correctly create.
	 * @return The name of the Entity
	 */
	public String getEntityName(Entity entity) {
		return entity.getName();
	}

	/**
	 * Return the method getID of Entity
	 * 
	 * @param entity
	 *            Entity correctly create.
	 * @return The id of the Entity
	 */
	public long getEntityID(Entity entity) {
		return entity.getID();
	}

	/**
	 * Return the method getHashedPassword of User
	 * 
	 * @param user
	 *            User correctly create.
	 * @return The hashedPassword
	 */
	public String getUserHash(User user) {
		return user.getHashedPassword();
	}

	/**
	 * Return the method isAnalyzed of Version.
	 * 
	 * @param version
	 *            Version correctly create.
	 * @return true if the version have be analyzed, else false.
	 */
	public int getVersionAnalyzed(Version version) {
		return version.isAnalyzed();
	}

	/**
	 * Create or set a property with the value on the node of idnode.
	 * 
	 * @param idnode
	 *            id of the node.
	 * @param parameter
	 *            properties.
	 * @param attribute
	 *            value of properties.
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
	 * Retire a property of the id Node.
	 * 
	 * @param idnode
	 *            the id of the node
	 * @param parameter
	 *            the property to delete.
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
	 * Return a Record list of each children of the node find per lownode who
	 * have the param relation and the label.
	 * 
	 * @param lownode
	 *            custom node for found the node.
	 * @param relation
	 *            used for take only children than you want, who have the
	 *            relation with the node. Can be null.
	 * @param childrenLabel
	 *            used for take only children who have the same label. Can be
	 *            null.
	 * @return k
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
	 * signUp create a new User with the email and the password, except if the
	 * email is already used.
	 * 
	 * @param email
	 *            the email put per the new User.
	 * @param password
	 *            a password.
	 * @return true if success else false;
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

	/**
	 * Create the Paprika-Analyze container and launch with a java command. They
	 * use a paprika-analyse image already put on the same docker than
	 * paprika-web
	 * 
	 * @param idNode
	 *            Id of the version than you want analyze
	 * @param fname
	 *            The name of the version.
	 * @param application
	 *            The name of the project of the version.
	 * @param user
	 *            The current user who request the analyse.
	 */
	public void callAnalyzeThread(long idNode, String fname, Application application, User user) {
		this.setParameterOnNode(idNode, PaprikaKeyWords.CODEA, "loading");
		this.setParameterOnNode(idNode, "analyseInLoading", "0");
		try {
			PaprikaWebMain.LOGGER.trace("callAnalyzeThread:");
			String command = "java -jar Paprika-analyze.jar " + fname + " " + user.getName() + " "
					+ application.getName() + " " + Long.toString(application.getID()) + " " + Long.toString(idNode);
			PaprikaWebMain.LOGGER.trace(command);
			String pathstr = "application/" + user.getName() + "/" + application.getName() + "/" + fname;
			this.setParameterOnNode(idNode, "PathFile", pathstr);
			RegistryAuth registryAuth = RegistryAuth.builder().serverAddress(getHostName()).build();
			DockerClient docker = DefaultDockerClient.fromEnv().dockerAuth(false).registryAuth(registryAuth).build();

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

		} catch (Exception e) {
			PaprikaWebMain.LOGGER.error(e.getMessage(), e);
			throw new PapWebRunTimeException(e.getMessage());
		}
	}

	/**
	 * Remove the finished container on Docker ( shell: docker ps -a, for see
	 * the id, then docker rm id)
	 * 
	 * @param id
	 *            id of the container to delete, found on the version node of
	 *            Neo4J
	 */
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

	/**
	 * Delete versions/project nodes of the neo4j database
	 * 
	 * @param setOfId
	 *            set who contains all ID of unknow node than user want delete
	 * @throws IOException
	 *             If a problem with File system.
	 */
	public void deleteOnDataBase(Set<String> setOfId) throws IOException {
		Set<String> versionsToDelete = new HashSet<>();

		try (Transaction tx = PaprikaWebMain.getSession().beginTransaction()) {
			for (String idAppli : setOfId) {
				versionsToDelete = deleteAppliOnDataBase(tx, idAppli, versionsToDelete);
			}
			deleteVersionsOnDataBase(tx, versionsToDelete);

			tx.success();
		}
	}

	/**
	 * Delete the project node of the idproject and put all versions of the
	 * project on the set If the node is not a project node, so he put the node
	 * on the set
	 * 
	 * @param tx
	 *            transaction of session of neo4j
	 * @param id
	 *            the id of a node
	 * @param versionsToDelete
	 *            set of ids to delete
	 * @return the versionsToDelete with more id of version nodes
	 */
	private Set<String> deleteAppliOnDataBase(Transaction tx, String id, Set<String> versionsToDelete) {
		StatementResult result;
		Record record;
		String begin;
		begin = "MATCH (n:Project) WHERE ID(n) = " + id;
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
			versionsToDelete.add(id);

		return versionsToDelete;
	}

	/**
	 * Delete all versions nodes and all childrens/external children of the id
	 * set
	 * 
	 * @param tx
	 *            transaction of session of neo4j
	 * @param versionsToDelete
	 *            set of ids to delete
	 * @throws IOException
	 *             If a problem with File system.
	 */
	private void deleteVersionsOnDataBase(Transaction tx, Set<String> versionsToDelete) throws IOException {
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

	/**
	 * Get the host container of web-paprika.
	 * 
	 * @return the ID of the host of container.
	 * @throws UnknownHostException
	 *             If the host of the container is not found.
	 */
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

	/**
	 * Add a new file on the container. param is here for the unique pathname of
	 * the file
	 * 
	 * @param currentUser
	 *            E-mail of the currentUser (Unique)
	 * @param application
	 *            name of the project where you put the file(unique for each
	 *            user)
	 * @param fName
	 *            name of the file.
	 * @param uploadedFile
	 *            The file.
	 * @param realname
	 *            the name without the format.
	 * @throws IOException
	 *             If the file system have a problem
	 */
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
