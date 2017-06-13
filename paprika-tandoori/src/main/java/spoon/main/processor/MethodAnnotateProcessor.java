package spoon.main.processor;


import java.util.Set;

import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.AnnotationFactory;


public class MethodAnnotateProcessor {
	
	public void process(CtMethod element, String currentClass) {
		String qualifiedName = element.getSimpleName()+"#"+currentClass;
		
		
		longMethodDetection(element,qualifiedName,"LM_NO_FUZZY");
		longMethodDetection(element,qualifiedName,"LM");
		getterSetterDetection(element,qualifiedName,"IGS");

		
		
	}

	private void getterSetterDetection(CtMethod element,String qualifiedName,String codesmells) {
		Set<String> valueSet=AnnotateProcessor.codesmells.get(codesmells);
		if (valueSet!=null && valueSet.contains(qualifiedName)) {
			Class<codesmells.annotations.Blob> annotationType = codesmells.annotations.Blob.class;
			AnnotationFactory factory = new AnnotationFactory(element.getFactory());
			factory.annotate(element, annotationType);
		}

	}

	private void longMethodDetection(CtMethod element,String qualifiedName,String codesmells) {
		Set<String> valueSet=AnnotateProcessor.codesmells.get(codesmells);
		if (valueSet!=null && valueSet.contains(qualifiedName)) {
			Class<codesmells.annotations.Blob> annotationType = codesmells.annotations.Blob.class;
			AnnotationFactory factory = new AnnotationFactory(element.getFactory());
			factory.annotate(element, annotationType);
		}
	}

}