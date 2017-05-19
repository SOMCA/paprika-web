package app.form;

import spark.*;

import java.io.IOException;
import java.util.*;

import app.application.PaprikaFacade;
import app.application.PaprikaWebMain;
import app.login.LoginController;
import app.model.Application;
import app.utils.PaprikaKeyWords;
import app.utils.PathIn;
import app.utils.RequestUtil;
import app.utils.ViewUtil;

public class FormController {

	
	public static final Route serveFormDELPage = (Request request, Response response) -> {
		/*
		 * Impossible d'accéder au formulaire avant d'être connecter.
		 */

		LoginController.ensureUserIsLoggedIn(request, response);
		Map<String, Object> model = new HashMap<>();
		PaprikaWebMain.LOGGER.trace("-------handleFormDelPage--------");
		return ViewUtil.render(request, model, PathIn.Template.FORM_DELETE);
	};

	public static final Route handleFormDeletePost = (Request request, Response response) -> {
		Map<String, Object> model = new HashMap<>();
		
		Application application= RequestUtil.getSessionApplication(request);
		PaprikaWebMain.LOGGER.trace("-------handleFormDeletePost--------");
		String delete = request.queryParams("delete");
		if (delete != null) {
			PaprikaWebMain.LOGGER.trace("etape delete: " + delete);
			deleteNotNull(request,application);
			model.put(PaprikaKeyWords.APPLICATION, application);
		}
		return ViewUtil.render(request, model, PathIn.Template.FORM_DELETE);
	};
	
	private FormController() {
		throw new IllegalAccessError("Controller class");
	}

	
	private static void deleteNotNull(Request request,Application application) throws IOException{
		Set<String> setQueryParams = request.queryParams();
		Set<String> setOfIdToDelete = new HashSet<>();
		for (String params : setQueryParams) {
			String idtoDelete = request.queryParams(params);
			if (idtoDelete == null || "00-".equals(idtoDelete)) {
					continue;
			}
			setOfIdToDelete.add(idtoDelete);	
		}
		PaprikaFacade facade = PaprikaFacade.getInstance();
		facade.deleteOnDataBase(setOfIdToDelete);
		facade.needReloadApp(application);
	}

}
