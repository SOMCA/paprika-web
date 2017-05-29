package app.controller;

import spark.*;
import java.util.*;

import org.mindrot.jbcrypt.BCrypt;

import app.application.PaprikaFacade;
import app.model.*;
import app.utils.*;

/**
 * Controller of Login page.
 * 
 * @author guillaume
 * 
 */
public class LoginController {

	private static final String CURRENTUSER = "currentUser";

	/**
	 * Login page per default.
	 */
	public static final Route serveLoginPage = (Request request, Response response) -> {
		Map<String, Object> model = new HashMap<>();
		model.put("loggedOut", RequestUtil.removeSessionAttrLoggedOut(request));
		model.put("loginRedirect", RequestUtil.removeSessionAttrLoginRedirect(request));
		return ViewUtil.render(request, model, PathIn.Template.LOGIN);
	};
	/**
	 * Request when user try to login
	 */
	public static final Route handleLoginPost = (Request request, Response response) -> {
		Map<String, Object> model = new HashMap<>();
		if (!authenticate(RequestUtil.getQueryUsername(request), RequestUtil.getQueryPassword(request))) {
			model.put("authenticationFailed", true);
			return ViewUtil.render(request, model, PathIn.Template.LOGIN);
		}

		model.put("authenticationSucceeded", true);
		request.session().attribute(CURRENTUSER, RequestUtil.getQueryUsername(request));
		request.session().attribute("user", PaprikaFacade.getInstance().user(RequestUtil.getQueryUsername(request)));
		if (RequestUtil.getQueryLoginRedirect(request) != null) {
			response.redirect(RequestUtil.getQueryLoginRedirect(request));
		}
		return ViewUtil.render(request, model, PathIn.Template.INDEX);
	};

	/**
	 * Request when user try to logout.
	 */
	public static final Route handleLogoutPost = (Request request, Response response) -> {
		request.session().removeAttribute(CURRENTUSER);
		request.session().removeAttribute("user");
		request.session().removeAttribute(PaprikaKeyWords.PROJECT);
		request.session().removeAttribute(PaprikaKeyWords.VERSION);
		request.session().attribute("loggedOut", true);
		response.redirect(PathIn.Web.INDEX);
		return null;
	};

	private LoginController() {
		throw new IllegalAccessError("Controller class");
	}

	/**
	 * Ensure than the user is logged.
	 * 
	 * @param request
	 * @param response
	 */
	public static final void ensureUserIsLoggedIn(Request request, Response response) {
		if (request.session().attribute(CURRENTUSER) == null) {
			request.session().attribute("loginRedirect", request.pathInfo());
			response.redirect(PathIn.Web.LOGIN);
		}
	}

	/**
	 * Authenticate the User with the email and password
	 * 
	 * @param email
	 *            the email of user
	 * @param password
	 *            the password of user
	 * @return true if the email and password are good, else false.
	 */
	public static final boolean authenticate(String email, String password) {
		if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
			return false;
		}
		PaprikaFacade facade = PaprikaFacade.getInstance();

		User user = facade.user(email);
		if (user == null) {
			return false;
		}
		String salt = facade.salt();
		if (salt == null) {
			return false;
		}
		String hashedPassword = BCrypt.hashpw(password, salt);
		return hashedPassword.equals(facade.getUserHash(user));
	}

}
