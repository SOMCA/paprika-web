package app.form;

import spark.*;

import java.util.*;

import app.application.PaprikaFacade;
import app.login.LoginController;
import app.model.Application;
import app.model.User;
import app.utils.PaprikaKeyWords;
import app.utils.PathIn;
import app.utils.RequestUtil;
import app.utils.ViewUtil;

public class FormController {

	private FormController() {
		throw new IllegalAccessError("Controller class");
	}

	public static final Route serveFormDELPage = (Request request, Response response) -> {
		/*
		 * Impossible d'accéder au formulaire avant d'être connecter.
		 */

		LoginController.ensureUserIsLoggedIn(request, response);
		Map<String, Object> model = new HashMap<>();
		System.out.println("-------handleFormDelPage--------");
		return ViewUtil.render(request, model, PathIn.Template.FORM_DELETE);
	};

	public static final Route handleFormDeletePost = (Request request, Response response) -> {
		Map<String, Object> model = new HashMap<>();
		PaprikaFacade facade = PaprikaFacade.getInstance();
		Application application= RequestUtil.getSessionApplication(request);
		System.out.println("-------handleFormDeletePost--------");
		String delete = request.queryParams("delete");
		if (delete != null) {

			System.out.println("etape delete: " + delete);
			Set<String> setQueryParams = request.queryParams();
			Set<String> setOfIdToDelete = new HashSet<>();
			for (String params : setQueryParams) {
				String idtoDelete = request.queryParams(params);

	

				if (idtoDelete == null || idtoDelete.equals("00-")) {
						continue;
					
				}
				setOfIdToDelete.add(idtoDelete);
				
			}
			facade.deleteOnDataBase(setOfIdToDelete);
			
			
			facade.needReloadApp(application);
			model.put("application", application);
		}

		return ViewUtil.render(request, model, PathIn.Template.FORM_DELETE);
	};

}
