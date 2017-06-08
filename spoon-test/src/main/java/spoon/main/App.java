package spoon.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
 * On lance le téléchargement du Github de ce projet qu'on analyse, puis qu'on
 * pull request. Problème dans le futur, lié au collisions des noms(deux projets
 * de même nom et bim, aie) Sauf si on fait comme d'habitude avec un dossier du
 * nom de l'utilisateur qui contient un dossier du nom de son application Puis
 * un dossier de la version.
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
	public static final String nameBot="SnrashaBot";
	public static String branch;
	public static String token;
	public static String cloneUrl;

	public static void main(String[] args) {

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
		App.url = "https://github.com/Snrasha/spoon-test.git";
		String[] split = url.split("/");
		if (split.length < 5)
			return;
		if (split[4].contains(".git"))
			App.name = split[4].substring(0, split[4].length() - 4);
		else
			App.name = split[4];
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
/*
	private static void createBranch(Git git)
			throws RefAlreadyExistsException, RefNotFoundException, InvalidRefNameException, GitAPIException {
		git.branchCreate().setName(App.branch).call();
	}

	private static void changeBranch(Git git) throws RefAlreadyExistsException, RefNotFoundException,
			InvalidRefNameException, CheckoutConflictException, GitAPIException {
		git.checkout().setName(App.branch).call();
	}
*/
	private static Git cloneRepo() throws InvalidRemoteException, TransportException, GitAPIException {
		Set<String> set = new HashSet<>();
		set.add("refs/heads/"+App.branch);
		CloneCommand clone = Git.cloneRepository();
		return clone.setDirectory(new File(input)).setURI(App.cloneUrl).setBranchesToClone(set).setBranch("refs/heads/"+App.branch)
				.call();
	}

	private static void before() throws IOException {

		try {

			RepositoryService service = new RepositoryService();
			service.getClient().setCredentials("token", App.token);
			RepositoryId toBeForked = new RepositoryId(App.nameUser, App.name);
			
			Repository repo=service.forkRepository(toBeForked);
		
			App.branch=repo.getMasterBranch();
			App.cloneUrl=repo.getCloneUrl();
			System.out.println(App.cloneUrl);
			// Clone the repo of the url.
			Git git = cloneRepo();
			
			// Create a new branch for edit.
			//createBranch(git);
			// Go on the new branch.
			//changeBranch(git);
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
		git.commit().setMessage("test message").call();
	}

	private static void after() throws IOException {

		if(App.cloneUrl==null) return;
		
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
	
			GitpullRequest pull = new GitpullRequest(App.nameBot, App.name, App.branch);
			pull.getData();
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
}
