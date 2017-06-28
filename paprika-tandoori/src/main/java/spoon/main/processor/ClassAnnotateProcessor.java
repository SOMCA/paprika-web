package spoon.main.processor;

import java.util.Set;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.AnnotationFactory;

/**
 * Add annotation to class and launch method and constructor processor
 * 
 * @author guillaume
 *
 */
// We use many rawtypes.
@SuppressWarnings("rawtypes")
public class ClassAnnotateProcessor extends AbstractProcessor<CtClass> {

	private CtClass element;
	private String qualifiedName;
	private AnnotationFactory factory;

	@Override
	public void process(CtClass element) {
		String qualifiedName = element.getQualifiedName();
		if (element.isAnonymous()) {
			String[] splitName = qualifiedName.split("\\$");
			qualifiedName = splitName[0] + "$" + ((CtNewClass) element.getParent()).getType().getQualifiedName()
					+ splitName[1];
		}
		AnnotationFactory factory = new AnnotationFactory(element.getFactory());
		this.element = element;
		this.qualifiedName = qualifiedName;
		this.factory = factory;

		/*
		 * try { Class<Annotation> anno=(Class<Annotation>)
		 * Class.forName("codesmells.annotations.IGS"); } catch
		 * (ClassNotFoundException e) { e.printStackTrace(); }
		 */

		// For add after, differents values for each case. fuzzy and no fuzzy
		// have less difference.
		blobDetection("BLOB");
		blobDetection("BLOB_NO_FUZZY");
		ccDetection("CC");
		ccDetection("CC_NO_FUZZY");
		licDetection("LIC");
		nlmrDetection("NLMR");

		// Launch all constructor and method of the class.
		processMethods(element, qualifiedName);
	}

	private boolean isOnSet(String codesmells) {
		Set<String> valueSet = AnnotateProcessor.codesmells.get(codesmells);
		return (valueSet != null && valueSet.contains(qualifiedName));
	}

	private void blobDetection(String codesmell) {
		if (this.isOnSet(codesmell)) {
			Class<codesmells.annotations.BLOB> annotationType = codesmells.annotations.BLOB.class;
			factory.annotate(element, annotationType);
		}

	}

	private void ccDetection(String codesmell) {
		if (this.isOnSet(codesmell)) {
			Class<codesmells.annotations.CC> annotationType = codesmells.annotations.CC.class;
			factory.annotate(element, annotationType);
		}
	}

	private void licDetection(String codesmell) {
		if (this.isOnSet(codesmell)) {
			Class<codesmells.annotations.LIC> annotationType = codesmells.annotations.LIC.class;
			factory.annotate(element, annotationType);
		}
	}

	private void nlmrDetection(String codesmell) {
		if (this.isOnSet(codesmell)) {
			Class<codesmells.annotations.NLMR> annotationType = codesmells.annotations.NLMR.class;
			factory.annotate(element, annotationType);
		}
	}

	private void processMethods(CtClass ctClass, String currentClass) {
		MethodAnnotateProcessor methodProcessor = new MethodAnnotateProcessor();
		ConstructorAnnotateProcessor constructorProcessor = new ConstructorAnnotateProcessor();
		for (Object o : ctClass.getMethods()) {
			methodProcessor.process((CtMethod) o, currentClass);
		}
		CtConstructor ctConstructor;
		for (Object o : ctClass.getConstructors()) {
			ctConstructor = (CtConstructor) o;
			constructorProcessor.process(ctConstructor, currentClass);
		}

	}

}