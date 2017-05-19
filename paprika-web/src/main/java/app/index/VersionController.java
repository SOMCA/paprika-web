package app.index;

import spark.*;

import java.io.File;
import java.util.*;

import app.application.PaprikaFacade;
import app.application.PaprikaWebMain;
import app.exception.PapWebRunTimeException;
import app.login.LoginController;
import app.model.Application;
import app.model.User;
import app.model.Version;
import app.utils.PaprikaKeyWords;
import app.utils.PathIn;
import app.utils.RequestUtil;
import app.utils.ViewUtil;

public class VersionController {

	public static final String renderVersion(Request request, Map<String, Object> model, String templatePath) {

		return ViewUtil.render(request, model, templatePath);
	}

	public static final Route serveVersionPage = (Request request, Response response) -> {
		Map<String, Object> model = new HashMap<>();

		/*
		 * Impossible d'accéder à cette page avant d'être connecter.
		 */
		LoginController.ensureUserIsLoggedIn(request, response);

		Application application = RequestUtil.getSessionApplication(request);

		if (application == null)
			return ViewUtil.render(request, model, PathIn.Template.INDEX);

		PaprikaWebMain.LOGGER.trace("-------serveVersionPage--------");

		PaprikaFacade facade = PaprikaFacade.getInstance();
		Version version = RequestUtil.getSessionVersion(request);
		if (version != null && version.isAnalyzed() != 3) {
			facade.reloadVersion(version);
			model.put(PaprikaKeyWords.VERSION, version);
		}

		return VersionController.renderVersion(request, model, PathIn.Template.VERSION);
	};

	public static final Route handleVersionPost = (Request request, Response response) -> {
		/*
		 * Impossible d'accéder à cette page avant d'être connecter.
		 */
		LoginController.ensureUserIsLoggedIn(request, response);

		Map<String, Object> model = new HashMap<>();
		PaprikaFacade facade = PaprikaFacade.getInstance();

		User user = RequestUtil.getSessionUser(request);

		PaprikaWebMain.LOGGER.trace("-------handleVersionPost--------");
		String menu = RequestUtil.getQueryVersion(request);
		if (menu != null) {
			PaprikaWebMain.LOGGER.trace("etape menu: " + menu);
			request.session().attribute(PaprikaKeyWords.APPLICATION, facade.application(Long.parseLong(menu)));
		}
		Application application = RequestUtil.getSessionApplication(request);

		// Formulaire quand on choisit la version dans la page layout.
		String menuVer = RequestUtil.getParamMenuVersion(request);
		if (menuVer != null) {
			PaprikaWebMain.LOGGER.trace("etape menuVer: " + menuVer);
			request.session().attribute(PaprikaKeyWords.VERSION, facade.version(Long.parseLong(menuVer)));
		}
		Version version = RequestUtil.getSessionVersion(request);
		// Formulaire quand on choisit d'ANALYSEr dans la page version.
		String analys = request.queryParams("analyse");
		if (analys != null) {
			PaprikaWebMain.LOGGER.trace("etape ANALYSE");
			String fname = version.getName() + ".apk";
			String pathstr = PaprikaKeyWords.REPERTORY + RequestUtil.getSessionCurrentUser(request) + "/"
					+ application.getName() + "/" + fname;
			boolean flag = false;
			flag = analyseVersion(pathstr);
			if (flag) {
				facade.callAnalyzeThread(version.getID(), fname, application, user, PaprikaWebMain.dockerVersion);
				facade.reloadVersion(version);
				model.put(PaprikaKeyWords.VERSION, version);
			}
		}

		return VersionController.renderVersion(request, model, PathIn.Template.VERSION);
	};

	private VersionController() {
		throw new IllegalAccessError("Controller class");
	}

	private static boolean analyseVersion(String pathstr) {
		try {
			new File(pathstr);
			return true;
		} catch (NullPointerException e) {
			PaprikaWebMain.LOGGER.error("The path for the file is null", e);
			throw new PapWebRunTimeException(e.getMessage());
		}
	}

}