package spoon.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
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

import spoon.Launcher;
import spoon.functions.VersionFunctions;
import spoon.main.processor.*;
import spoon.processing.ProcessInterruption;
import spoon.utils.neo4j.PaprikaKeyWords;

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
		System.out.println(jsonTxt);
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

	public void run() {
		this.verFct.setParameterOnNode(this.idNode,PaprikaKeyWords.ANALYSEINLOAD, "10");

		boolean isContinue;
		try {
			isContinue = before();

		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		if (!isContinue)
			return;
		this.verFct.setParameterOnNode(this.idNode,PaprikaKeyWords.ANALYSEINLOAD, "80");

		try {
			isContinue = after();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		if (!isContinue)
			return;
		verFct.setParameterOnNode(this.idNode, PaprikaKeyWords.CODEA, "done");
		verFct.setParameterOnNode(this.idNode, "analyseInLoading", "100");
		
		
	}

	private void remove(String path) throws IOException {
		FileUtils.deleteDirectory(new File(path));
	}

	private Git cloneRepo() throws InvalidRemoteException, TransportException, GitAPIException {
		Set<String> set = new HashSet<>();
		set.add("refs/heads/" + this.branch);
		CloneCommand clone = Git.cloneRepository();
		return clone.setDirectory(new File(input)).setURI(this.cloneUrl).setBranchesToClone(set)
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

			// Create a new branch for edit.
			// createBranch(git);
			// Go on the new branch.
			// changeBranch(git);
			git.close();
			this.verFct.setParameterOnNode(this.idNode,PaprikaKeyWords.ANALYSEINLOAD, "15");

			
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
		remove(out);

		// Launch Paprika-Spoon
		final Launcher launcher = new Launcher();
		launcher.getEnvironment().setNoClasspath(true);

		launcher.addInputResource(input + "/src/main/java");

		launcher.setSourceOutputDirectory(out);
		// launcher.getEnvironment().setCommentEnabled(true);

		final MethodProcessor methodprocessor = new MethodProcessor();
		launcher.addProcessor(methodprocessor);
		final ClassProcessor classprocessor = new ClassProcessor();
		launcher.addProcessor(classprocessor);
		final InterfaceProcessor interfaceProcessor = new InterfaceProcessor();
		launcher.addProcessor(interfaceProcessor);
		try {
			this.verFct.setParameterOnNode(this.idNode,PaprikaKeyWords.ANALYSEINLOAD, "20");

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
		FileUtils.copyDirectory(new File(out), new File(input + "/src/main/java"));

		Git git = Git.open(new File(this.input));

		try {
			addRepo(git);
			commitRepo(git);
			this.verFct.setParameterOnNode(this.idNode,PaprikaKeyWords.ANALYSEINLOAD, "90");

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
			this.verFct.setParameterOnNode(this.idNode,PaprikaKeyWords.ANALYSEINLOAD, "95");

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

	private void pullCall() throws IllegalStateException, IOException {
		String url = "https://api.github.com/repos/" + this.nameUser + "/" + this.name + "/pulls";
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);

		InputStream is = new FileInputStream("./info.json");
		String jsonTxt = IOUtils.toString(is);
		System.out.println(jsonTxt);
		JSONObject json = new JSONObject(jsonTxt);

		post.addHeader("Authorization", "token " + json.getString("token"));

		StringEntity params = new StringEntity("{ \"title\": \"Paprika Analyze\","
				+ "\"body\": \"Do not merge! Look just annotations, then close!\"," + "\"head\": \"" + this.nameBot
				+ ":" + this.branch + "\"," + "\"base\": \"" + "master" + "\" }", ContentType.APPLICATION_JSON);
		post.setEntity(params);

		HttpResponse response = client.execute(post);
		System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
		System.out.flush();
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		System.out.println(result.toString());
		System.out.flush();
	}

}
