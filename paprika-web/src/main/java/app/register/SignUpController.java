package app.register;

import spark.*;
import java.util.*;
import app.application.PaprikaFacade;
import app.utils.*;

public class SignUpController {


	
	public static final Route serveSignUpPage = (Request request, Response response) -> {
		Map<String, Object> model = new HashMap<>();
		return ViewUtil.render(request, model, PathIn.Template.SIGNUP);
	};

	public static final Route handleSignUpPost = (Request request, Response response) -> {
		
		Map<String, Object> model = new HashMap<>();
		PaprikaFacade facade= PaprikaFacade.getInstance();
		
		
		if (!facade.signUp(RequestUtil.getQueryUsername(request), RequestUtil.getQueryPassword(request))) {
			model.put("signUpFailed", true);
			return ViewUtil.render(request, model, PathIn.Template.SIGNUP);
		}
		model.put("signUpSucceeded", true);

		request.session().attribute("currentUser", RequestUtil.getQueryUsername(request));
		request.session().attribute("user",
				facade.user(RequestUtil.getQueryUsername(request)));
		if (RequestUtil.getQueryLoginRedirect(request) != null) {
			response.redirect(RequestUtil.getQueryLoginRedirect(request));
		}

		return ViewUtil.render(request, model, PathIn.Template.INDEX);
	};


	  private SignUpController() {
		    throw new IllegalAccessError("Controller class");
		  }

}
