package app.index;

import spark.*;

import java.io.File;
import java.util.*;

import app.application.PaprikaFacade;
import app.login.LoginController;
import app.model.Application;
import app.model.User;
import app.model.Version;
import app.utils.PaprikaKeyWords;
import app.utils.PathIn;
import app.utils.RequestUtil;
import app.utils.ViewUtil;
import app.utils.neo4j.LowNode;

public class VersionController {

	private static final String ANALYSE = "analyse";

	public static final String renderVersion(Request request, Map<String, Object> model, String templatePath) {
		PaprikaFacade facade = PaprikaFacade.getInstance();
		Version version = RequestUtil.getSessionVersion(request);
		if (version != null) {
			LowNode nodeVer = new LowNode(PaprikaKeyWords.VERSIONLABEL);
			nodeVer.setId(version.getID());
			String value = facade.getParameter(nodeVer, PaprikaKeyWords.ANALYSEINLOAD);
			if (value != null && !"100".equals(value)) {
				model.put(PaprikaKeyWords.ANALYSEINLOAD, value);
			}
		}
		return ViewUtil.render(request, model, templatePath);
	}

	public static final Route serveVersionPage = (Request request, Response response) -> {
		Map<String, Object> model = new HashMap<>();

		Application application = RequestUtil.getSessionApplication(request);

		if (application == null)
			return ViewUtil.render(request, model, PathIn.Template.INDEX);

		/*
		 * Impossible d'accéder à cette page avant d'être connecter.
		 */
		LoginController.ensureUserIsLoggedIn(request, response);

		// On ne le garde que dans la page version, sinon on le récupère par
		// Post
		model.put(ANALYSE, request.session().attribute(ANALYSE));

		System.out.println("-------serveVersionPage--------");

		return VersionController.renderVersion(request, model, PathIn.Template.VERSION);
	};

	public static final Route handleVersionPost = (Request request, Response response) -> {
		Map<String, Object> model = new HashMap<>();
		PaprikaFacade facade = PaprikaFacade.getInstance();

		User user = RequestUtil.getSessionUser(request);

		System.out.println("-------handleVersionPost--------");
		String menu = RequestUtil.getQueryVersion(request);
		System.out.println(menu);
		if (menu != null) {
			System.out.println("etape menu: " + menu);
			request.session().attribute(PaprikaKeyWords.APPLICATION, facade.application(user, menu));
		}
		Application application = RequestUtil.getSessionApplication(request);

		// Formulaire quand on choisit la version dans la page layout.
		String menuVer = RequestUtil.getParamMenuVersion(request);
		if (menuVer != null) {
			System.out.println("etape menuVer: " + menuVer);
			request.session().attribute("version", facade.version(application, menuVer));
			request.session().removeAttribute(ANALYSE);
		}
		Version version = RequestUtil.getSessionVersion(request);
		LowNode nodeVer = new LowNode(PaprikaKeyWords.VERSIONLABEL);
		nodeVer.setId(version.getID());
		String str;
		// Formulaire quand on choisit d'ANALYSEr dans la page version.
		String analys = request.queryParams(ANALYSE);
		if (analys != null) {
			System.out.println("etape ANALYSE");
			request.session().attribute(ANALYSE, true);
			String fname = version.getName() + ".apk";
			String pathstr = PaprikaKeyWords.REPERTORY + RequestUtil.getSessionCurrentUser(request) + "/" + application.getName()
					+ "/" + fname;
			boolean flag = false;
			File file = null;
			try {
				file = new File(pathstr);
				flag = true;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			if (flag) {


				 facade.callAnalyzeThread(nodeVer, fname, application, user, file.length());
				model.put(PaprikaKeyWords.ANALYSEINLOAD, "0");

			}

		}
		// Regarde si la version a déjà été analysé ou non.

		else {

			str = facade.getParameter(nodeVer, PaprikaKeyWords.CODEA);
			if (str != null) {
				System.out.println("loading statut");
				request.session().attribute(ANALYSE, true);
				if ("loading".equals(str)) {
					model.put(PaprikaKeyWords.ANALYSEINLOAD, "0");
				}
			} else
				request.session().removeAttribute(ANALYSE);
		}

		// Formulaire quand on choisit de recevoir les données dans la page
		// version sous forme de csv.

		model.put(ANALYSE, request.session().attribute(ANALYSE));

		return VersionController.renderVersion(request, model, PathIn.Template.VERSION);
	};

	private VersionController() {
		throw new IllegalAccessError("Controller class");
	}

}