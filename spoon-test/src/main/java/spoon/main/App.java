package spoon.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.*;
import org.json.JSONObject;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import spoon.Launcher;
import spoon.processing.ProcessInterruption;

/**
 * 
 * 
 * @author guillaume
 *
 */
public class App {
	public static String url;
	public static String input;
	public static String out;
	public static String name;
	public static String nameUser;
	public static final String nameBot = "SnrashaBot";
	public static String branch;
	public static String token;
	public static String cloneUrl;

	public static void main(String[] args) {
		if (args.length > 1)
			return;
		InputStream is;
		try {
			is = new FileInputStream("./info.json");
			String jsonTxt;
			jsonTxt = IOUtils.toString(is);
			System.out.println(jsonTxt);
			JSONObject json = new JSONObject(jsonTxt);
			App.token = json.getString("token");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (args.length == 1) {
			App.url = args[0];

		} else {
			App.url = "https://github.com/Snrasha/spoon-test.git";
		}
		String[] split = url.split("/");
		if (split.length < 5) {
			System.out.println("Error: The format is https://github.com/User/project.git");
			return;
		}
		if (!"https".equals(split[0])) {
			System.out.println("Error: The format is https://github.com/User/project.git");
			return;
		}
		if (split[1].length() != 0) {
			System.out.println("Error: The format is https://github.com/User/project.git");
			return;
		}
		if (!"github.com".equals(split[2])) {
			System.out.println("Error: The format is https://github.com/User/project.git");
			return;
		}
		if (!split[4].contains(".git") && split[4].length()>4) {
			System.out.println("Error: The format is https://github.com/User/project.git");
			return;
		}
		App.name = split[4].substring(0, split[4].length() - 4);

		App.nameUser = split[3];
		App.input = "./input/" + App.name;
		App.out = "./output/" + App.name;

		run();

	}

	private static void run() {
		try {
			before();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		try {
			after();
		} catch (IOException e) {
			e.printStackTrace();
			return;

		}

	}

	private static void remove(String path) throws IOException {
		FileUtils.deleteDirectory(new File(path));
	}

	private static Git cloneRepo() throws InvalidRemoteException, TransportException, GitAPIException {
		Set<String> set = new HashSet<>();
		set.add("refs/heads/" + App.branch);
		CloneCommand clone = Git.cloneRepository();
		return clone.setDirectory(new File(input)).setURI(App.cloneUrl).setBranchesToClone(set)
				.setBranch("refs/heads/" + App.branch).call();
	}

	private static void before() throws IOException {

		try {

			RepositoryService service = new RepositoryService();
			service.getClient().setCredentials("token", App.token);
			RepositoryId toBeForked = new RepositoryId(App.nameUser, App.name);

			Repository repo = service.forkRepository(toBeForked);

			App.branch = repo.getMasterBranch();
			App.cloneUrl = repo.getCloneUrl();
			System.out.println(App.cloneUrl);
			// Clone the repo of the url.
			Git git = cloneRepo();

			// Create a new branch for edit.
			// createBranch(git);
			// Go on the new branch.
			// changeBranch(git);
			git.close();

		} catch (InvalidRemoteException e) {
			e.printStackTrace();
		} catch (TransportException e) {
			e.printStackTrace();
		} catch (GitAPIException e) {
			e.printStackTrace();

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
			launcher.run();
		} catch (ProcessInterruption e) {
			e.printStackTrace();
		}

	}

	private static void addRepo(Git git) throws NoFilepatternException, GitAPIException {
		git.add().addFilepattern(".").call();
	}

	private static void commitRepo(Git git) throws NoHeadException, NoMessageException, UnmergedPathsException,
			ConcurrentRefUpdateException, WrongRepositoryStateException, AbortedByHookException, GitAPIException {
		git.commit().setMessage("Commit Insert Annotations").call();
	}

	private static void after() throws IOException {

		if (App.cloneUrl == null)
			return;

		// Move output on the input
		FileUtils.copyDirectory(new File(out), new File(input + "/src/main/java"));

		Git git = Git.open(new File(App.input));

		try {
			addRepo(git);
			commitRepo(git);
		} catch (NoFilepatternException e) {
			e.printStackTrace();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}

		try {
			PushCommand push = git.push();
			push.setRemote("origin").setPushAll()
					.setCredentialsProvider(new UsernamePasswordCredentialsProvider("token", token)).call();
			pullCall();
		} catch (Exception e) {
			e.printStackTrace();
		}
		git.close();
		try {
			remove(out);
			remove(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void pullCall() throws IllegalStateException, IOException {
		String url = "https://api.github.com/repos/" + App.nameUser + "/" + App.name + "/pulls";
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);

		InputStream is = new FileInputStream("./info.json");
		String jsonTxt = IOUtils.toString(is);
		System.out.println(jsonTxt);
		JSONObject json = new JSONObject(jsonTxt);

		post.addHeader("Authorization", "token " + json.getString("token"));

		StringEntity params = new StringEntity(
				"{ \"title\": \"Paprika Analyze\"," + "\"body\": \"Do not merge! Look just annotations, then close!\","
						+ "\"head\": \"" + App.nameBot + ":" + App.branch + "\"," + "\"base\": \"" + "master" + "\" }",
				ContentType.APPLICATION_JSON);
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
