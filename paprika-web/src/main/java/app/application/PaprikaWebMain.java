package app.application;

import static spark.Spark.*;
import static spark.debug.DebugScreen.*;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;

import app.form.FormController;
import app.functions.DescriptionFunctions;
import app.index.IndexController;
import app.index.VersionController;
import app.login.LoginController;
import app.register.SignUpController;
import app.utils.PathIn;

import spark.Spark;
import java.net.InetAddress;

public class PaprikaWebMain {

	// "bolt://localhost:7687" quand on n'utilise pas docker.
	public static  Driver driver = GraphDatabase.driver("bolt://" + getHostName() + ":7687",
			AuthTokens.basic("neo4j", "paprika"));

	/**
	 * Prend le nom du container neo4j-praprika et renvoie son adresse.
	 * 
	 * @return
	 */
	private static String getHostName() {
		try {
			String str= InetAddress.getByName("neo4j-paprika").getHostAddress();
			System.out.println(str);
			return str;
		} catch (final Exception e) {
			return "localhost";
		}
	}
	public static final boolean dockerVersion=true;

	private PaprikaWebMain() {

	}

	public static void main(String[] args) {

		new DescriptionFunctions().addAllClassicDescription();

		// Open the port 4567 for create a localhost server.
		port(4567);

		// Enable the debugscreen for know why we have this error, need be
		// optional on the futur
		enableDebugScreen();

		// Request of Spark for know where are the css or img in the ressources
		// of the project
		Spark.staticFileLocation("/public");

		// La page d'index.
		get("", IndexController.serveIndexPage);
		get("/", IndexController.serveIndexPage);
		get(PathIn.Web.INDEX, IndexController.serveIndexPage);
		// La page de login, quand tu veux te connecter.
		get(PathIn.Web.LOGIN, LoginController.serveLoginPage);
		// La page d'index avec la demande de logout.
		get(PathIn.Web.LOGOUT, LoginController.handleLogoutPost);
		// La page d'inscription, quand tu veux t'inscrire.
		get(PathIn.Web.SIGNUP, SignUpController.serveSignUpPage);
		// Partie SETTINGs
		get(PathIn.Web.FORMDEL, FormController.serveFormDELPage);

		// Mis sur indexController car il est basé sur l'index
		get(PathIn.Web.VERSION, VersionController.serveVersionPage);

		/*
		 * Reçoit les données du formulaire de login et renvoie à l'index.
		 * Demande à la page login que le formulaire envoie sur /index/,
		 * handleloginpost attrape alors la requête en passant.
		 * 
		 */

		post(PathIn.Web.INDEX, IndexController.handleIndexaddApp);
		post(PathIn.Web.VERSION, VersionController.handleVersionPost);

		post(PathIn.Web.LOGIN, LoginController.handleLoginPost);

		// Reçoit les données du formulaire de signup et renvoie à l'index.
		post(PathIn.Web.SIGNUP, SignUpController.handleSignUpPost);

		post(PathIn.Web.FORMDEL, FormController.handleFormDeletePost);

	}
}