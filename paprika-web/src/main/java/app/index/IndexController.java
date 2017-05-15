package app.index;

import spark.*;

import java.io.File;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.*;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import app.application.PaprikaFacade;
import app.application.PaprikaWebMain;
import app.model.Application;
import app.utils.PaprikaKeyWords;
import app.utils.PathIn;
import app.utils.RequestUtil;
import app.utils.ViewUtil;


public class IndexController {

	
	  private IndexController() {
		    throw new IllegalAccessError("Controller class");
		  }

	/**
	 * First index, who have the form.
	 */
	public static final Route serveIndexPage = (Request request, Response response) -> {
		Map<String, Object> model = new HashMap<>();
		return ViewUtil.render(request, model, PathIn.Template.INDEX);
	};

	/**
	 * Cette fonction récupère les informations de deux formulaires, le menu des
	 * applications et l'upload de versions. Le formulaire qui s'occupe des
	 * paramètres des versions ne se trouvent pas dans l'index, le reste des
	 * paramètres sera déduit de la version.
	 * 
	 * le second formulaire, pour l'instant, prend le nom du fichier et vire le
	 * .apk de fin
	 */
	public static final Route handleIndexaddApp = (Request request, Response response) -> {
		Map<String, Object> model = new HashMap<>();
		PaprikaFacade facade= PaprikaFacade.getInstance();
		Application application=RequestUtil.getSessionApplication(request);
		
		
		// Formulaire quand on ajoute un project.
		String project = request.queryParams("project");
		String menu = RequestUtil.getParamMenu(request);
		if (project != null) {
			long idProject=facade.addProject(RequestUtil.getSessionUser(request), project);
			if(idProject!=-1)
			request.session().attribute(PaprikaKeyWords.APPLICATION, facade.application(idProject));
		}
		// Formulaire quand on choisit le menu.
		else if (menu != null) {
			request.session().attribute(PaprikaKeyWords.APPLICATION,facade.application(Long.parseLong(menu)));
		}
		// Formulaire quand on upload un fichier ET analyse.
		else if (menu == null && project == null) {
			
			String location = "../application";
			if(PaprikaWebMain.dockerVersion) location="/application";
			long maxFileSize = 100000000;
			long maxRequestSize = 100000000;
			int fileSizeThreshold = 1024;
			MultipartConfigElement multipartConfigElement = new MultipartConfigElement(location, maxFileSize,
					maxRequestSize, fileSizeThreshold);
			request.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);
			Part uploadedFile = request.raw().getPart("appAndroid");
			if (uploadedFile != null) {
				String fName = request.raw().getPart("appAndroid").getSubmittedFileName();

				String realname = fName.substring(0, fName.lastIndexOf('.'));
				String format = fName.substring(fName.lastIndexOf('.'), fName.length());
				if (!".apk".equals(format)) {
					System.out.println("The file is not a .apk file! We have: "+format);
				}
				else if(realname.length() > 50){
					System.out.println("The name of the file is too long, maximal size: 21");
				}
				else {

					System.out.println(realname);
					
					String pathstr = PaprikaKeyWords.REPERTORY + RequestUtil.getSessionCurrentUser(request) + '/'
							+ application.getName() + '/' + fName;
					Path out = Paths.get(pathstr);
					File file = new File(pathstr);
					file.mkdirs();
					file = null;
					Files.deleteIfExists(out);
					try (final InputStream in = uploadedFile.getInputStream()) {
						Files.copy(in, out);
						uploadedFile.delete();
					}
					multipartConfigElement = null;
					uploadedFile = null;

					facade.addVersion(application.getID() , realname);
					
				}
			}
		}

		return ViewUtil.render(request, model, PathIn.Template.INDEX);
	};
	


}
