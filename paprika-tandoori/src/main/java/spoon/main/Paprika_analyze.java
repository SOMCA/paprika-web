package spoon.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.AbortedByHookException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.json.JSONObject;

import neo4jBolt.Graph;
import spoon.Launcher;
import spoon.functions.VersionFunctions;
import spoon.processing.ProcessInterruption;

/**
 * Paprika_analyse use multiple library like Git or Github.
 * 
 * Egit for fork.
 * HttpsRequest for delete and pull.
 * JGit for clone and push
 * 
 * @author guillaume
 *
 */
public class Paprika_analyze {
	private String url;
	private String input;
	private String out;
	private String name;
	private String nameUser;
	private final String nameBot = "SnrashaBot";
	private String branch;
	private String token;
	private String cloneUrl;
	private long idNode;
	private VersionFunctions verFct;

	public Paprika_analyze(String github, long idnode) throws IOException {

		InputStream is;
		is = new FileInputStream("./info.json");
		String jsonTxt;
		jsonTxt = IOUtils.toString(is);
		// System.out.println(jsonTxt);
		JSONObject json = new JSONObject(jsonTxt);
		this.token = json.getString("token");

		if (github == null) {
			this.url = "https://github.com/Snrasha/spoon-test.git";

		} else {
			// The link is checked on Paprika-web container, so useless to
			// retry, no?
			this.url = github;
		}
		this.idNode = idnode;

		String[] split = url.split("/");

		this.name = split[4].substring(0, split[4].length() - 4);

		this.nameUser = split[3];
		// The container is unique per analyze and do not use a shared volume,
		// so no problem.
		this.input = "./input/" + this.name;
		this.out = "./output/" + this.name;
		this.verFct = new VersionFunctions();
	}

	private boolean process() {
		this.verFct.setParameterOnNode(this.idNode, Graph.ANALYSEINLOAD, "10");

		boolean isContinue;
		try {
			isContinue = before();

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		if (!isContinue)
			return false;
		this.verFct.setParameterOnNode(this.idNode, Graph.ANALYSEINLOAD, "80");

		try {
			isContinue = after();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		if (!isContinue)
			return false;
		return true;
	}

	/**
	 * Than process fail or success, this is not important, we finish the
	 * analyse.
	 */
	public boolean run() {
		// deleteRepo();

		boolean flag = this.process();
		verFct.setParameterOnNode(this.idNode, Graph.CODEA, "done");
		verFct.setParameterOnNode(this.idNode, "analyseInLoading", "100");

		deleteRepo();
		return flag;

	}

	private void remove(String path) throws IOException {
		FileUtils.deleteDirectory(new File(path));
	}

	private Git cloneRepo() throws InvalidRemoteException, TransportException, GitAPIException {
		Set<String> set = new HashSet<>();
		set.add("refs/heads/" + this.branch);
		CloneCommand clone = Git.cloneRepository();
		return clone.setCredentialsProvider(new UsernamePasswordCredentialsProvider("token", this.token))
				.setDirectory(new File(input)).setURI(this.cloneUrl).setBranchesToClone(set)
				.setBranch("refs/heads/" + this.branch).call();
	}

	private boolean before() throws IOException {

		try {

			RepositoryService service = new RepositoryService();
			service.getClient().setCredentials("token", this.token);
			RepositoryId toBeForked = new RepositoryId(this.nameUser, this.name);

			Repository repo = service.forkRepository(toBeForked);

			this.branch = repo.getMasterBranch();
			this.cloneUrl = repo.getCloneUrl();
			System.out.println(this.cloneUrl);
			// Clone the repo of the url.
			Git git = cloneRepo();

			git.close();
			this.verFct.setParameterOnNode(this.idNode, Graph.ANALYSEINLOAD, "15");

			if (this.cloneUrl == null)
				return false;

		} catch (InvalidRemoteException e) {
			e.printStackTrace();
			return false;
		} catch (TransportException e) {
			e.printStackTrace();
			return false;
		} catch (GitAPIException e) {
			e.printStackTrace();
			return false;

		}
		// Remove the out folder
		remove(this.out);

		// Launch Paprika-Spoon
		final Launcher launcher = new Launcher();
		launcher.getEnvironment().setNoClasspath(true);

		launcher.addInputResource(this.input);

		launcher.setSourceOutputDirectory(this.out);
		launcher.getEnvironment().setAutoImports(true);
/*
		final MethodProcessor methodprocessor = new MethodProcessor();
		launcher.addProcessor(methodprocessor);
		final ClassProcessor classprocessor = new ClassProcessor();
		launcher.addProcessor(classprocessor);
		final InterfaceProcessor interfaceProcessor = new InterfaceProcessor();
		launcher.addProcessor(interfaceProcessor);*/
		try {
			this.verFct.setParameterOnNode(this.idNode, Graph.ANALYSEINLOAD, "20");
			launcher.run();
		} catch (ProcessInterruption e) {
			e.printStackTrace();
			return false;

		}
		return true;
	}

	private void addRepo(Git git) throws NoFilepatternException, GitAPIException {
		git.add().addFilepattern(".").call();
	}

	private static void commitRepo(Git git) throws NoHeadException, NoMessageException, UnmergedPathsException,
			ConcurrentRefUpdateException, WrongRepositoryStateException, AbortedByHookException, GitAPIException {
		git.commit().setMessage("Commit Insert Annotations").call();
	}

	private boolean after() throws IOException {

		if (this.cloneUrl == null)
			return false;

		// Move output on the input
		// FileUtils.copyDirectory(new File(out), new File(input));

		// Move only file who is a .java

		DirectorySearch fileSearch = new DirectorySearch(".java");
		Map<String, String> javaInput = fileSearch.run(this.input);
		Map<String, String> javaOutput = fileSearch.run(this.out);
		int count = javaInput.size();
		if (count == 0) {
			System.out.println("\nNo result found!");
		} else {
			System.out.println("\nFound " + count + " result!\n");
			for (String matched : javaInput.keySet()) {
				String pathoutput = javaOutput.get(matched);
				if (pathoutput == null)
					continue;
				String pathinput = javaInput.get(matched);
				if (pathinput == null)
					continue;
				System.out.println("Output: " + pathoutput + " On Input: " + pathinput);
				// String newInput = "./in" + matched.substring(5);
				// System.out.println("Moved on" + newInput);
				FileUtils.copyFile(new File(pathoutput), new File(pathinput), false);

			}
		}
		Git git = Git.open(new File(this.input));

		try {
			addRepo(git);
			commitRepo(git);
			this.verFct.setParameterOnNode(this.idNode, Graph.ANALYSEINLOAD, "90");

		} catch (NoFilepatternException e) {
			e.printStackTrace();
			git.close();
			return false;

		} catch (GitAPIException e) {
			e.printStackTrace();
			git.close();
			return false;
		}

		try {

			PushCommand push = git.push();
			push.setRemote("origin").setPushAll()
					.setCredentialsProvider(new UsernamePasswordCredentialsProvider("token", token)).call();
			pullCall();
			this.verFct.setParameterOnNode(this.idNode, Graph.ANALYSEINLOAD, "95");

		} catch (Exception e) {
			e.printStackTrace();
			git.close();
			return false;
		}
		git.close();
		try {
			remove(out);
			remove(input);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Delete a repositery if he exist of the Paprika_analyze
	 */
	private void deleteRepo() {
		String url = "https://api.github.com/repos/" + this.nameBot + "/" + this.name;
		HttpClient client = HttpClientBuilder.create().build();
		HttpDelete delete = new HttpDelete(url);
		delete.addHeader("Authorization", "token " + this.token);
		try {
			client.execute(delete);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void pullCall() throws ClientProtocolException, IOException {
		String url = "https://api.github.com/repos/" + this.nameUser + "/" + this.name + "/pulls";
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);

		post.addHeader("Authorization", "token " + this.token);

		StringEntity params = new StringEntity("{ \"title\": \"Paprika Analyze\","
				+ "\"body\": \"Do not merge! Look just annotations, then close!\"," + "\"head\": \"" + this.nameBot
				+ ":" + this.branch + "\"," + "\"base\": \"" + "master" + "\" }", ContentType.APPLICATION_JSON);
		post.setEntity(params);
		client.execute(post);

	}

}
