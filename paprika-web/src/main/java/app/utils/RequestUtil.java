package app.utils;


import app.model.*;
import spark.*;

/**
 * 
 * This file have be taken of the example of spark-basic-structure:
 * https://github.com/tipsy/spark-basic-structure
 *
 */
public class RequestUtil {

	  private RequestUtil() {
		    throw new IllegalAccessError("Utility class");
		  }
	
    public static String getQueryLocale(Request request) {
        return request.queryParams("locale");
    }

    public static String getParamIsbn(Request request) {
        return request.params("isbn");
    }
    public static String getParamMenuVersion(Request request) {
        return request.queryParams("titleVer");
    }
    public static String getParamMenu(Request request) {
        return request.queryParams("title");
    }

    public static String getQueryApplication(Request request) {
        return request.queryParams("application");
    }

    public static String getQueryVersion(Request request) {
        return request.queryParams("titleofVer");
    }
    
    public static String getQueryUsername(Request request) {
        return request.queryParams("email");
    }

    public static String getQueryPassword(Request request) {
        return request.queryParams("password");
    }

    public static String getQueryLoginRedirect(Request request) {
        return request.queryParams("loginRedirect");
    }

    public static String getSessionLocale(Request request) {
        return request.session().attribute("locale");
    }

    public static String getSessionCurrentUser(Request request) {
    	return request.session().attribute("currentUser");
  
    }
    public static User getSessionUser(Request request) {
    	return (User)request.session().attribute("user");
  
    }
    public static boolean removeSessionAttrLoggedOut(Request request) {
        Object loggedOut = request.session().attribute("loggedOut");
        request.session().removeAttribute("loggedOut");
        return loggedOut != null;
    }

    public static String removeSessionAttrLoginRedirect(Request request) {
        String loginRedirect = request.session().attribute("loginRedirect");
        request.session().removeAttribute("loginRedirect");
        return loginRedirect;
    }
    public static Application getSessionApplication(Request request) {
    	return (Application)request.session().attribute("application");
    }
    public static Version getSessionVersion(Request request) {
    	return (Version)request.session().attribute("version");
    }
    

    public static String getSessionAnalyseInLoading(Request request) {
    	return request.session().attribute("analyseInLoading");
    }


    public static boolean clientAcceptsHtml(Request request) {
        String accept = request.headers("Accept");
        return accept != null && accept.contains("text/html");
    }

    public static boolean clientAcceptsJson(Request request) {
        String accept = request.headers("Accept");
        return accept != null && accept.contains("application/json");
    }

}
