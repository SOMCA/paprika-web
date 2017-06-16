package app.utils;

import org.apache.velocity.app.VelocityEngine;

import spark.*;
import java.util.*;
import spark.template.velocity.*;

/**
 * 
 * This file have be taken of the example of spark-basic-structure:
 * https://github.com/tipsy/spark-basic-structure
 *
 */
public class ViewUtil {

	private ViewUtil() {
		throw new IllegalAccessError("Render class");
	}

	/**
	 * model.put here, for webpath, already used on the template. For User, not
	 * for the moment. msg is not utilized.
	 * 
	 * @param request
	 * @param model
	 * @param templatePath
	 * @return //
	 */
	public static String render(Request request, Map<String, Object> model, String templatePath) {
		model.put("msg", new MessageBundle(RequestUtil.getSessionLocale(request)));
		String user = RequestUtil.getSessionCurrentUser(request);
		model.put("currentUser", user);
		model.put("user", RequestUtil.getSessionUser(request));
		model.put("WebPath", PathIn.Web.class);

		
		if (!model.containsKey(PaprikaKeyWords.PROJECT)) {
			model.put("data", new DataSave());
			
			/*
			if (!templatePath.equals(PathIn.Template.FORM_DELETE)) {

				Project project = RequestUtil.getSessionProject(request);
				model.put(PaprikaKeyWords.PROJECT, project);
			}*/
		}
	//	model.put("data", new DataSave());

		if (!model.containsKey(PaprikaKeyWords.VERSION)) {
			model.put(PaprikaKeyWords.VERSION, RequestUtil.getSessionVersion(request));
		}

		return strictVelocityEngine().render(new ModelAndView(model, templatePath));
	}

	/**
	 * 
	 * @return
	 */
	private static VelocityTemplateEngine strictVelocityEngine() {
		VelocityEngine configuredEngine = new VelocityEngine();

		configuredEngine.setProperty("runtime.references.strict", true);
		configuredEngine.setProperty("resource.loader", "class");
		configuredEngine.setProperty("class.resource.loader.class",
				"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		return new VelocityTemplateEngine(configuredEngine);
	}
}
