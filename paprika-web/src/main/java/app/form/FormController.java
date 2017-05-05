package app.form;

import spark.*;


import java.util.*;



import app.login.LoginController;
import app.utils.PathIn;
import app.utils.ViewUtil;


public class FormController {
	
	  private FormController() {
		    throw new IllegalAccessError("Controller class");
		  }
	

	public static final  Route serveFormDELPage = (Request request, Response response) -> {
		/*
		 * Impossible d'accéder au formulaire avant d'être connecter.
		 */
		LoginController.ensureUserIsLoggedIn(request, response);
		Map<String, Object> model = new HashMap<>();

		return ViewUtil.render(request, model, PathIn.Template.FORM_DELETE);
	};
	
	public  static final Route handleFormDeletePost = (Request request, Response response) -> {
		Map<String, Object> model = new HashMap<>();
		return ViewUtil.render(request, model, PathIn.Template.FORM_DELETE);
	};



}
