package spoon.main.processor;

import java.util.Set;


import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.AnnotationFactory;


public class ClassAnnotateProcessor extends AbstractProcessor<CtClass> {
	@Override
	public void process(CtClass element) {
		String qualifiedName = element.getQualifiedName();
		if (element.isAnonymous()) {
			String[] splitName = qualifiedName.split("\\$");
			qualifiedName = splitName[0] + "$" + ((CtNewClass) element.getParent()).getType().getQualifiedName()
					+ splitName[1];
		}
		
		blobDetection(element, qualifiedName,"BLOB");
		blobDetection(element, qualifiedName,"BLOB_NO_FUZZY");
	
		
		processMethods(element,qualifiedName);
	}

	private void blobDetection(CtClass element,String qualifiedName,String codesmell) {
		Set<String> valueSet=AnnotateProcessor.codesmells.get(codesmell);
		if (valueSet!=null && valueSet.contains(qualifiedName)) {
		
			Class<codesmells.annotations.Blob> annotationType = codesmells.annotations.Blob.class;
			AnnotationFactory factory = new AnnotationFactory(element.getFactory());
			factory.annotate(element, annotationType);
		}
	
	}
	
    public void processMethods(CtClass ctClass,String currentClass) {
    	MethodAnnotateProcessor methodProcessor = new MethodAnnotateProcessor();
        ConstructorAnnotateProcessor constructorProcessor = new ConstructorAnnotateProcessor();
        for (Object o : ctClass.getMethods()) {
            methodProcessor.process((CtMethod) o,currentClass);
        }
        CtConstructor ctConstructor;
        for (Object o : ctClass.getConstructors()) {
            ctConstructor = (CtConstructor) o;
            constructorProcessor.process(ctConstructor,currentClass);
        }

    }

}