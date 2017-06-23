package app.controller;

import spark.*;

import java.io.IOException;

import java.util.*;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;

import app.application.PaprikaFacade;
import app.application.PaprikaWebMain;
import app.model.Project;
import app.utils.PaprikaKeyWords;
import app.utils.PathIn;
import app.utils.RequestUtil;
import app.utils.ViewUtil;

/**
 * The controller is used per the index page.
 * 
 * @author guillaume
 * 
 */
public class IndexController {

	/**
	 * Index where you go per default.
	 */
	public static final Route serveIndexPage = (Request request, Response response) -> {
		Map<String, Object> model = new HashMap<>();
		return ViewUtil.render(request, model, PathIn.Template.INDEX);
	};
	/**
	 * reset the project
	 */
	public static final Route resetProjectIndexPage = (Request request, Response response) -> {
		Map<String, Object> model = new HashMap<>();
		model.put(PaprikaKeyWords.PROJECT, null);

		return ViewUtil.render(request, model, PathIn.Template.INDEX);
	};

	/**
	 * Index page who take multiple form: -Add a project -Add a version -Change
	 * the focus of Project.
	 */
	public static final Route handleIndexaddApp = (Request request, Response response) -> {
		Map<String, Object> model = new HashMap<>();
		PaprikaFacade facade = PaprikaFacade.getInstance();
		Project application = RequestUtil.getSessionProject(request);
		// Formulaire quand on ajoute un project.
		String project = request.queryParams("project");
		String menu = RequestUtil.getParamMenu(request);
		if (project != null) {
			long idProject = facade.addProject(RequestUtil.getSessionUser(request), project);
			if (idProject != -1)
				request.session().attribute(PaprikaKeyWords.PROJECT, facade.project(idProject));
		}
		// Formulaire quand on choisit le menu.
		else if (menu != null) {
			request.session().attribute(PaprikaKeyWords.PROJECT, facade.project(Long.parseLong(menu)));
		}
		// Formulaire quand on upload un fichier.
		else if (menu == null && project == null) {
			boolean fileadded = addFile(request, application, facade);
			if (fileadded) {
				model.put(PaprikaKeyWords.PROJECT, application);
			}
		}
		return ViewUtil.render(request, model, PathIn.Template.INDEX);
	};

	private IndexController() {
		throw new IllegalAccessError("Controller class");
	}

	/**
	 * Add a file
	 * 
	 * @param request
	 * @param project
	 * @param facade
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	private static boolean addFile(Request request, Project project, PaprikaFacade facade)
			throws IOException, ServletException {

		String location = "/application";

		String github = request.queryParams("github");
		if (github != null) {
			// https://github.com/Snrasha/Tandoori     Format< Ignore the user, probably.
			String[] cutGithub= github.split("/");
			boolean badLink= (cutGithub.length != 5)||(!github.startsWith("https://github.com/"))||(!cutGithub[4].endsWith(".git") || cutGithub[4].length()<=4);
			if (badLink) {
				System.out.println("Error: The format is https://github.com/User/project.git");
				System.out.flush();
				return false;
			}

			facade.addGithub(project, github);
			System.out.println(github);
			return true;
		} else {
			long maxFileSize = 100000000;
			long maxRequestSize = 100000000;
			int fileSizeThreshold = 1024;
			MultipartConfigElement multipartConfigElement = new MultipartConfigElement(location, maxFileSize,
					maxRequestSize, fileSizeThreshold);
			request.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);
			Part uploadedFile = request.raw().getPart("appAndroid");
			if (uploadedFile != null) {
				String fName = request.raw().getPart("appAndroid").getSubmittedFileName();
				int lastindex = fName.lastIndexOf('.');
				if (lastindex == -1) {
					PaprikaWebMain.LOGGER.error("The file have not format!");
					return false;
				}

				String realname = fName.substring(0, lastindex);
				String format = fName.substring(lastindex, fName.length());
				if (".apk".equals(format)) {
					PaprikaWebMain.LOGGER.trace(realname);
					facade.addFile(RequestUtil.getSessionCurrentUser(request), project, fName, uploadedFile, realname);

					return true;
				}
			}
		}

		return false;
	}
}
