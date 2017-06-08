package spoon.main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;

import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.factory.AnnotationFactory;
import spoon.reflect.visitor.filter.TypeFilter;

public class ClassProcessor extends AbstractProcessor<CtClass> {
	@Override
	public void process(CtClass element) {
		System.out.println("Class:" + element.getSimpleName());
		blobDetection(element);
	}

	private void blobDetection(CtClass element) {
		boolean added = false;
		Class<codesmells.annotations.Blob> annotationType = codesmells.annotations.Blob.class;

		AnnotationFactory factory = new AnnotationFactory(element.getFactory());
		CtAnnotation<?> annotation = factory.annotate(element, annotationType);

		codesmells.annotations.Blob blob = element.getAnnotation(codesmells.annotations.Blob.class);
		Map<String, Object> values = new HashMap<>();
		// System.out.println("Element size methods:" +
		// element.getMethods().size());
		if (element.getMethods().size() > blob.limitMethods()) {

			getFactory().getEnvironment().report(this, Level.WARN, element, "Blob code smell");

			values.put("limitMethods", blob.limitMethods());
			values.put("currentMethods", element.getMethods().size());

			added = true;
		}
		// System.out.println("Element size fields:" +
		// element.getFields().size());
		if (element.getFields().size() > blob.limitAttributes()) {

			getFactory().getEnvironment().report(this, Level.WARN, element, "Blob code smell");

			values.put("limitAttributes", blob.limitAttributes());
			values.put("currentAttributes", element.getFields().size());

			added = true;
		}
		int lcom = computeLCOM(element);
		System.out.println("Method lack cohesion:" + lcom);
		if (lcom > blob.limitLackOfCohesionMethods()) {

			getFactory().getEnvironment().report(this, Level.WARN, element, "Blob code smell");

			values.put("limitLackOfCohesionMethods", blob.limitLackOfCohesionMethods());
			values.put("currentLackOfCohesionMethods", lcom);

			added = true;
		}

		if (!added)
			element.removeAnnotation(annotation);
		else
			annotation.setElementValues(values);
	}

	/**
	 * Blob to move
	 * 
	 * @param element
	 * @return
	 */
	public int computeLCOM(CtClass element) {
		Set<CtMethod> setmethods = element.getAllMethods();
		CtMethod methods[] = new CtMethod[setmethods.size()];
		int index = 0;
		for (CtMethod method : setmethods) {
			methods[index] = method;
			index++;
		}

		int methodCount = methods.length;
		int haveFieldInCommon = 0;
		int noFieldInCommon = 0;
		List<CtVariable> usedVariables;
		for (int i = 0; i < methodCount; i++) {
			for (int j = i + 1; j < methodCount; j++) {
				if (methods[i] == null)
					continue;
				if (methods[i].getBody() == null)
					continue;
				usedVariables = methods[i].getBody().getElements(new TypeFilter(CtVariable.class));
				if (this.haveCommonFields(usedVariables, methods[j])) {
					haveFieldInCommon++;
				} else {
					noFieldInCommon++;
				}
			}
		}
		int LCOM = noFieldInCommon - haveFieldInCommon;
		return LCOM > 0 ? LCOM : 0;
	}

	/**
	 * Blob to move.
	 * 
	 * @param usedVariables
	 * @param element
	 * @return
	 */
	private boolean haveCommonFields(List<CtVariable> usedVariables, CtMethod element) {
		if (element == null)
			return false;
		if (element.getBody() == null)
			return false;

		List<CtVariable> otherVariables = element.getBody().getElements(new TypeFilter(CtVariable.class));
		for (CtVariable paprikaVariable : usedVariables) {
			if (otherVariables.contains(paprikaVariable))
				return true;
		}
		return false;
	}

}