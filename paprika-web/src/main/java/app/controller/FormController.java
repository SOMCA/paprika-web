package app.controller;

import spark.*;

import java.io.IOException;
import java.util.*;

import app.application.PaprikaFacade;
import app.application.PaprikaWebMain;
import app.model.Project;
import app.utils.PaprikaKeyWords;
import app.utils.PathIn;
import app.utils.RequestUtil;
import app.utils.ViewUtil;

/**
 * Controller for the settings.
 * 
 * @author guillaume
 * 
 */
public class FormController {

	/**
	 * Standard Page for reset password, configure for send.
	 */
	public static final Route serveFormResetSendPage = (Request request, Response response) -> {

		Map<String, Object> model = new HashMap<>();
		PaprikaWebMain.LOGGER.trace("-------serveFormResetPage--------");

		model.put("send", true);
		return ViewUtil.render(request, model, PathIn.Template.RESET);
	};
	/**
	 * Standard Page for reset password, configure for change.
	 */
	public static final Route serveFormResetReceivePage = (Request request, Response response) -> {

		Map<String, Object> model = new HashMap<>();
		PaprikaWebMain.LOGGER.trace("-------serveFormResetPage--------");
		model.put("change", true);

		return ViewUtil.render(request, model, PathIn.Template.RESET);
	};

	/**
	 * POST page for resetpassword. If do  not contains pwd on the request, so this is a post from of a SEND page, else CHANGE page.
	 * 
	 * Merged for obscur reason.
	 */
	public static final Route handleFormResetPost = (Request request, Response response) -> {
		Map<String, Object> model = new HashMap<>();

		PaprikaWebMain.LOGGER.trace("-------handleFormResetPost--------");
		String email = RequestUtil.getQueryUsername(request);

		String captcha = request.queryParams("g-recaptcha-response");
		boolean stop = false;
		if (PaprikaWebMain.ENABLEALLSECURITY)
			if (captcha == null || captcha.isEmpty() || "false".equals(captcha)) {
				model.put("resetFlagFail", true);
				stop = true;
			}
		if (email != null) {
			PaprikaFacade facade = PaprikaFacade.getInstance();
			String pwd = RequestUtil.getQueryPassword(request);

			if (pwd != null) {

				model.put("change", true);
				if (stop)
					return ViewUtil.render(request, model, PathIn.Template.RESET);

				String activation = request.queryParams("activation");
				boolean flag = facade.resetpwd(email, activation, pwd);
				model.put("resetFlag", flag);

			} else {

				model.put("send", true);
				if (stop)
					return ViewUtil.render(request, model, PathIn.Template.RESET);
				facade.sendnewPwd(email);
				model.put("emailSended", true);
			}
		}
		model.put(PaprikaKeyWords.PROJECT, null);

		return ViewUtil.render(request, model, PathIn.Template.RESET);
	};

	/**
	 * If a user try to go on the page without be logged. Go on Login page.
	 * Else, nothing.
	 */
	public static final Route serveFormDELPage = (Request request, Response response) -> {
		/*
		 * Impossible d'accéder au formulaire avant d'être connecter.
		 */

		LoginController.ensureUserIsLoggedIn(request, response);
		Map<String, Object> model = new HashMap<>();
		PaprikaWebMain.LOGGER.trace("-------serveFormDELPage--------");
		return ViewUtil.render(request, model, PathIn.Template.FORM_DELETE);
	};

	/**
	 * Active when user give a form to the delete page. he launch the delete of
	 * a the value of the form.
	 */
	public static final Route handleFormDeletePost = (Request request, Response response) -> {
		Map<String, Object> model = new HashMap<>();

		// Project project = RequestUtil.getSessionProject(request);
		PaprikaWebMain.LOGGER.trace("-------handleFormDeletePost--------");
		String delete = request.queryParams("delete");
		if (delete != null) {
			PaprikaWebMain.LOGGER.trace("etape delete: " + delete);
			deleteNotNull(request, null);// project);
			// model.put(PaprikaKeyWords.PROJECT, project);
		}
		model.put(PaprikaKeyWords.PROJECT, null);

		return ViewUtil.render(request, model, PathIn.Template.FORM_DELETE);
	};

	private FormController() {
		throw new IllegalAccessError("Controller class");
	}

	private static void deleteNotNull(Request request, Project project) throws IOException {
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
		// facade.needReloadApp(project);
	}

}
