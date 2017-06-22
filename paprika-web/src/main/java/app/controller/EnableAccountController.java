package app.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import app.application.PaprikaFacade;
import app.application.PaprikaWebMain;
import app.model.Project;
import app.utils.PaprikaKeyWords;
import app.utils.PathIn;
import app.utils.RequestUtil;
import app.utils.ViewUtil;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Controller for enable the account.
 * 
 * @author guillaume
 * 
 */
public class EnableAccountController {


	public static final Route servePage = (Request request, Response response) -> {


		Map<String, Object> model = new HashMap<>();
		PaprikaWebMain.LOGGER.trace("-------EnableAcc:servePage--------");
		return ViewUtil.render(request, model, PathIn.Template.ENACC);
	};


	public static final Route handlePost = (Request request, Response response) -> {
		Map<String, Object> model = new HashMap<>();

		PaprikaWebMain.LOGGER.trace("-------EnableAcc:handlePost--------");
		String activation = request.queryParams("activation");
		if (activation != null) {
			String email=RequestUtil.getQueryUsername(request);
			PaprikaFacade facade= PaprikaFacade.getInstance();
			int flag=facade.activeAccount(email, activation);
			model.put("authENACC", flag);
		}
		model.put(PaprikaKeyWords.PROJECT, null);
		return ViewUtil.render(request, model, PathIn.Template.ENACC);
	};

	private EnableAccountController() {
		throw new IllegalAccessError("Controller class");
	}

}


