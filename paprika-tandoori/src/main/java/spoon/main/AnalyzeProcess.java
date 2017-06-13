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

import entities.PaprikaApp;
import neo4jBolt.Graph;
import neo4jBolt.LowNode;
import neo4jBolt.ModelToGraphBolt;
import spoon.functions.VersionFunctions;
import spoon.main.processor.AnnotateProcessor;

public class AnalyzeProcess {

	private String fName;
	private LowNode nodeVer;
	private String github;
	private final String nameBot = "SnrashaBot";
	private String token;
	private String cloneUrl;
	private String input;
	private String output;
	private String nameDir;
	private String nameUser;
	private String branch;

	public AnalyzeProcess(String fName, LowNode nodeVer, String github) throws IOException {
		this.github = github;

		this.fName = fName;
		this.nodeVer = nodeVer;

		String[] split = github.split("/");

		this.nameDir = split[4].substring(0, split[4].length() - 4);

		this.nameUser = split[3];
		this.input = "./input/" + this.nameDir;
		this.output = "./output/" + this.nameDir;

		
		
		
		InputStream is;
		is = new FileInputStream("./info.json");
		String jsonTxt;
		jsonTxt = IOUtils.toString(is);
		JSONObject json = new JSONObject(jsonTxt);
		this.token = json.getString("token");

	}

	public void run() {
		Analyse ana = new Analyse();
		this.runPartAnalyse(ana);
		this.runPartQuery(ana);

	}

	private void runPartAnalyse(Analyse ana) {
		VersionFunctions verFct = new VersionFunctions();

		try {

			RepositoryService service = new RepositoryService();
			service.getClient().setCredentials("token", this.token);
			RepositoryId toBeForked = new RepositoryId(this.nameUser, this.nameDir);

			Repository repo = service.forkRepository(toBeForked);

			this.branch = repo.getMasterBranch();
			this.cloneUrl = repo.getCloneUrl();
			System.out.println(this.cloneUrl);
			// Clone the repo of the url.
			Git git = cloneRepo();

			git.close();
			
			
			
			
			String[] args = {input, output, this.fName, "android-platforms/", Long.toString(this.nodeVer.getID()) };

			verFct.setParameterOnNode(nodeVer.getID(), Graph.ANALYSEINLOAD, "10");
			PaprikaApp paprikaapp;
			paprikaapp = ana.runAnalysis(args);

			verFct.setParameterOnNode(nodeVer.getID(), Graph.ANALYSEINLOAD, "50");

			ModelToGraphBolt modelToGraph = new ModelToGraphBolt();
			long idApp = modelToGraph.insertApp(paprikaapp, nodeVer).getID();

			verFct.writeAnalyzeOnVersion(nodeVer, idApp);

			verFct.setParameterOnNode(nodeVer.getID(), Graph.APPKEY, Long.toString(nodeVer.getID()));

			
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void runPartQuery(Analyse ana) {
		VersionFunctions verFct = new VersionFunctions();

		long keyApp = nodeVer.getID();
		String[] args = { "query", "-k", Long.toString(keyApp), "-r", "ALLAP" };
		verFct.writeQueryOnVersion(nodeVer, keyApp);
		ana.runQueryMode(args);
		verFct.setParameterOnNode(nodeVer.getID(), Graph.CODEA, "done");
		verFct.setParameterOnNode(nodeVer.getID(), "analyseInLoading", "100");
		
		
		
		AnnotateProcessor annote= new AnnotateProcessor(this.input,this.output);
		annote.process();
		try {
			after();
		} catch (IOException e) {
			e.printStackTrace();
		}
		deleteRepo();
		
	}
	
	

	private Git cloneRepo() throws InvalidRemoteException, TransportException, GitAPIException {
		Set<String> set = new HashSet<>();
		set.add("refs/heads/" + this.branch);
		CloneCommand clone = Git.cloneRepository();
		return clone.setCredentialsProvider(new UsernamePasswordCredentialsProvider("token", this.token))
				.setDirectory(new File(input)).setURI(this.cloneUrl).setBranchesToClone(set)
				.setBranch("refs/heads/" + this.branch).call();
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
		Map<String, String> javaOutput = fileSearch.run(this.output);
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

		} catch (Exception e) {
			e.printStackTrace();
			git.close();
			return false;
		}
		git.close();


		return true;
	}

	/**
	 * Delete a repositery if he exist of the Paprika_analyze
	 */
	private void deleteRepo() {
		String url = "https://api.github.com/repos/" + this.nameBot + "/" + this.nameDir;
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
		String url = "https://api.github.com/repos/" + this.nameUser + "/" + this.nameDir + "/pulls";
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