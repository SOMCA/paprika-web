package app.controller;

import spark.*;
import java.util.*;

import org.mindrot.jbcrypt.BCrypt;

import app.application.PaprikaFacade;
import app.model.*;
import app.utils.*;

public class LoginController {
	

	private static final String CURRENTUSER="currentUser";

	public static final Route serveLoginPage = (Request request, Response response) -> {
		Map<String, Object> model = new HashMap<>();
		model.put("loggedOut", RequestUtil.removeSessionAttrLoggedOut(request));
		model.put("loginRedirect", RequestUtil.removeSessionAttrLoginRedirect(request));
		return ViewUtil.render(request, model, PathIn.Template.LOGIN);
	};

	public static final Route handleLoginPost = (Request request, Response response) -> {
		Map<String, Object> model = new HashMap<>();
		if (!authenticate(RequestUtil.getQueryUsername(request), RequestUtil.getQueryPassword(request))) {
			model.put("authenticationFailed", true);
			return ViewUtil.render(request, model, PathIn.Template.LOGIN);
		}
		
		model.put("authenticationSucceeded", true);
		request.session().attribute(CURRENTUSER, RequestUtil.getQueryUsername(request));
		request.session().attribute("user", 
				PaprikaFacade.getInstance().user(RequestUtil.getQueryUsername(request)));
		if (RequestUtil.getQueryLoginRedirect(request) != null) {
			response.redirect(RequestUtil.getQueryLoginRedirect(request));
		}
		return ViewUtil.render(request, model, PathIn.Template.INDEX);
	};

	public static final Route handleLogoutPost = (Request request, Response response) -> {
		request.session().removeAttribute(CURRENTUSER);
		request.session().removeAttribute("user");
		request.session().removeAttribute(PaprikaKeyWords.APPLICATION);
		request.session().removeAttribute(PaprikaKeyWords.VERSION);
		request.session().attribute("loggedOut", true);
		response.redirect(PathIn.Web.INDEX);
		return null;
	};

	  private LoginController() {
		    throw new IllegalAccessError("Controller class");
		  }
	
	// The origin of the request (request.pathInfo()) is saved in the session so
	// the user can be redirected back after login
	public static final void ensureUserIsLoggedIn(Request request, Response response) {
		if (request.session().attribute(CURRENTUSER) == null) {
			request.session().attribute("loginRedirect", request.pathInfo());
			response.redirect(PathIn.Web.LOGIN);
		}
	}

	// Authenticate the user by hashing the inputted password using the stored
	// salt,
	// then comparing the generated hashed password to the stored hashed
	// password
	public static final boolean authenticate(String email, String password) {
		if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
			return false;
		}
		PaprikaFacade facade=PaprikaFacade.getInstance();
		
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
