package app.application;

import java.io.File;
import java.io.FileInputStream;
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

import org.apache.commons.io.IOUtils;
import org.apache.commons.mail.*;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.RegistryAuth;

import app.exception.PapWebRunTimeException;
import app.functions.ProjectFunctions;
import app.functions.UserFunctions;
import app.functions.VersionFunctions;
import app.model.*;
import app.model.example.ProjectExample;
import app.model.example.VersionExample;
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

	/** Private constructor */
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
	 * 
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
	 * Found the Project of id project
	 * 
	 * @param project
	 *            id of the project.
	 * @return the project of the id
	 */
	public Project project(long project) {
		ProjectFunctions appFct = new ProjectFunctions();

		if (this.getParameter(project, PaprikaKeyWords.EXAMPLE) != null) {
			return new ProjectExample(appFct.receiveOf(project), project);
		}

		return new Project(appFct.receiveOf(project), project);
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
		String name = verFct.receiveOf(version);
		if (this.getParameter(version, PaprikaKeyWords.EXAMPLE) != null) {
			return new VersionExample(name, version, (name.endsWith("0")));
		}

		return new Version(name, version);
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
		ProjectFunctions appFct = new ProjectFunctions();
		long idProject = -1;
		if (project != null && appFct.receiveIDOfProject(user.getName(), project) == -1) {
			idProject = appFct.writeProjectOnUser(user.getName(), project);
		}

		return idProject;
	}

	/**
	 * Put the project to need be reload.
	 * 
	 * @param project
	 *            the project.
	 */
	public void needReloadApp(Project project) {
		if (project != null)
			project.needReload();
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
	 * @return return true if success, else false;
	 * 
	 */
	public long addVersion(long idproject, String version) {

		// If the example Project is a real example, so user cannot add version.
		if (this.getParameter(idproject, PaprikaKeyWords.EXAMPLE) != null)
			return -1;

		VersionFunctions verFct = new VersionFunctions();
		if (version != null && verFct.receiveIDOfVersion(idproject, version) == -1) {
			LowNode node = verFct.writeVersion(idproject, version);
			return node.getID();
		}

		return -1;
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
			tx.run("MATCH (n) WHERE ID(n)= " + Long.toString(idnode) + " SET n+={" + parameter + ":\"" + attribute
					+ "\"}");

			tx.success();
		}
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
	public void setParameterOnNode(String idnode, String parameter, String attribute) {
		if (Long.parseLong(idnode) == -1) {
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
	 * @param project
	 *            The name of the project of the version.
	 * @param user
	 *            The current user who request the analyse.
	 * 
	 */
	public void callAnalyzeThread(long idNode, String fname, String project, User user) {

		this.setParameterOnNode(idNode, PaprikaKeyWords.CODEA, "loading");
		this.setParameterOnNode(idNode, "analyseInLoading", "0");
		try {
			String command;
			PaprikaWebMain.LOGGER.trace("callAnalyzeThread:");
			String github = this.getParameter(idNode, "GitHub");
			boolean isGitHub = github != null;
			if (isGitHub) {
				// length 6
				command = "java -jar Paprika-Tandoori.jar " + fname + " " + Long.toString(idNode) + " " + github;
			} else {
				// length 7
				command = "java -jar Paprika-analyze.jar " + fname + " " + user.getName() + " " + project + " "
						+ Long.toString(idNode);
				PaprikaWebMain.LOGGER.trace(command);
				String pathstr = "application/" + user.getName() + "/" + project + "/" + fname;
				this.setParameterOnNode(idNode, "PathFile", pathstr);
			}
			RegistryAuth registryAuth = RegistryAuth.builder().serverAddress(getHostName()).build();
			DockerClient docker = DefaultDockerClient.fromEnv().dockerAuth(false).registryAuth(registryAuth).build();

			// Need and Contains always the id node of the version, on always
			// the last index.
			PaprikaWebMain.getContainerqueue().offer(command.split(" "));
			docker.close();

		} catch (

		Exception e) {
			PaprikaWebMain.LOGGER.error(e.getMessage(), e);
			throw new PapWebRunTimeException(e.getMessage());
		}
	}

	/**
	 * Return true if the container do not exist anymore.
	 * 
	 * 
	 * @param id
	 *            id of the container
	 * @return true if the container do not exist
	 */
	public boolean isRemoved(String id) {
		boolean removed = true;
		DockerClient docker = null;
		try {
			RegistryAuth registryAuth = RegistryAuth.builder().serverAddress(getHostName()).build();

			docker = DefaultDockerClient.fromEnv().dockerAuth(false).registryAuth(registryAuth).build();
			ContainerInfo info = docker.inspectContainer(id);

			if (info != null && docker.inspectContainer(id).state().running()) {
				removed = false;
			}

		} catch (Exception e) {
			removed = false;
		}
		if (docker != null) {
			docker.close();
		}
		return removed;
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
	public String getHostName() throws UnknownHostException {
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
	 * @param project
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
	public void addFile(String currentUser, Project project, String fName, Part uploadedFile, String realname)
			throws IOException {
		String pathstr = PaprikaKeyWords.REPERTORY + currentUser + '/' + project.getName() + '/' + fName;
		Path out = Paths.get(pathstr);
		File file = new File(pathstr);
		file.mkdirs();
		file = null;
		Files.deleteIfExists(out);
		try (final InputStream in = uploadedFile.getInputStream()) {
			Files.copy(in, out);
			uploadedFile.delete();
		}

		this.addVersion(project.getID(), realname);
		this.needReloadApp(project);
	}

	/**
	 * Add a new version with a github link, he download the link on the
	 * analyze, contrary to the method addFile.
	 * 
	 * 
	 * @param project
	 *            name of the project where you put the file(unique for each
	 *            user)
	 * @param linkGithub
	 *            link of the github for the download the source code
	 */
	public void addGithub(Project project, String linkGithub) {

		String numberV = project.getNumberOfVersion();
		long idProject = project.getID();
		String nameV = project.getName() + "_" + numberV;
		boolean successAdd = (this.addVersion(idProject, nameV) != -1);
		if (successAdd) {
			VersionFunctions verFct = new VersionFunctions();
			long idV = verFct.receiveIDOfVersion(idProject, nameV);
			this.setParameterOnNode(idV, "GitHub", linkGithub);
			this.needReloadApp(project);
		}
	}
	
	

	/**
	 * Send a bot email to a email.
	 * 
	 * @throws EmailException
	 * @throws IOException
	 */
	private void sendEmail(String mail,String title,String message) throws EmailException, IOException {

		InputStream is;
		is = new FileInputStream("./info.json");
		String jsonTxt;
		jsonTxt = IOUtils.toString(is);
		JSONObject json = new JSONObject(jsonTxt);

		String token_pwd = json.getString("email_pwd");
		
	
		
		String token_username;
		if(!json.isNull("email_username"))
			token_username = json.getString("email_username");
		else token_username= "snrashabot";
	
		
		
		String smtp;
		if(!json.isNull("email_smtp"))
			smtp = json.getString("email_smtp");
		else smtp= "smtp.gmail.com";
		
		String from;
		if(!json.isNull("email_email"))
		   from = json.getString("email_email");
		else from= "snrashabot@gmail.com";
	
		
		String to = mail;
		
	
		Email email = new SimpleEmail();
		email.setHostName(smtp);
		email.setSmtpPort(465);
		email.setAuthenticator(new DefaultAuthenticator(token_username, token_pwd));
		email.setSSLOnConnect(true);
		email.setFrom(from);
		email.setSubject(title);
		email.setMsg(message);
		email.addTo(to);
		email.send();

	}
	/**
	 * Send a active code for activate account.
	 * @param email
	 * @throws EmailException
	 * @throws IOException
	 */
	public void sendActiveCode(String email) throws EmailException, IOException{
		UserFunctions userfct= new UserFunctions();
		String code=userfct.generateRandomActivationCode(email,false);

		String title="Paprika: Active account.";
		String message="   Hi " + email.split("@")[0] + ", \n" +
		        "Thanks for registering at Paprika-Web.  To activate your email address click the link below! \n\n"
		        + "\nLink for enable account: https://spirals-somca.lille.inria.fr/paprika/enableAccount/ \n\n  And insert this Activation code: "+code;
	
		sendEmail(email,title,message);
	}
	
	/**
	 * Active the account of the user if the code is good.
	 * @param email email of the user(unique)
	 * @param code a random code generated before.
	 * @return 0 if the user of the email do not exist or not good code, 1 is already active, 2 is have be enabled.
	 */
	public int activeAccount(String email, String code){
		User user=this.user(email);
		if(user==null) return 0;
		if(user.getActive()) 
			return 1;
		if(this.getParameter(user.getID(), "activation").equals(code)){
			this.setParameterOnNode(user.getID(), "enabled", "1");
			this.removeParameterOnNode(user.getID(), "activation");
			return 2;
			
		}
			
		
		return 0;
	}
	/**
	 * Send a email for obtain a new password
	 * @param email
	 * @throws EmailException
	 * @throws IOException
	 */
	public void sendnewPwd(String email) throws EmailException, IOException{
		UserFunctions userfct= new UserFunctions();
		String code=userfct.generateRandomActivationCode(email,true);
		if(userfct.foundUser(email)==null) return;

		String title="Paprika: Reset Password";
		String message="   Hi " + email.split("@")[0] + ", \n" +
		        "Thanks for have use the service at Paprika-Web."
		        + "\n  To reset your password click the link below!"
		        + "\n\nLink for reset your password: https://spirals-somca.lille.inria.fr/paprika/reset/change/ \n\n  And insert this reset code: "+code
		        +"\n E-mail is not secured, so you need to fastly use the reset code.";
	
		sendEmail(email,title,message);
	}
	/**
	 * Reset the password the user with the new pwd.
	 * @param email the email of the user
	 * @param code the code for active the reset
	 * @param pwd the new password
	 * @return true if success, false if do not found user or the code is not good.
	 */
	public boolean resetpwd(String email, String code,String pwd){
		User user=this.user(email);
		if(user==null) return false;
		if(this.getParameter(user.getID(), "activation").equals(code)){
			String salt=this.salt();
			String newHashedPassword = BCrypt.hashpw(pwd, salt);
			this.setParameterOnNode(user.getID(), "hashpwd", newHashedPassword);
			this.setParameterOnNode(user.getID(), "enabled", "1");
			this.removeParameterOnNode(user.getID(), "activation");
			return true;
			
		}
			
		
		return false;
	}

}
