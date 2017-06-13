package spoon.main.processor;


import java.util.Set;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.factory.AnnotationFactory;


public class InterfaceAnnotateProcessor extends AbstractProcessor<CtInterface> {
	@Override
	public void process(CtInterface element) {
        String qualifiedName = element.getQualifiedName();
        if (element.isAnonymous()) {
            String[] splitName = qualifiedName.split("\\$");
            qualifiedName = splitName[0] + "$" +
                    ((CtNewClass) element.getParent()).getType().getQualifiedName() + splitName[1];
        }
        
        
        SakDetection(element,qualifiedName,"SAK");
		SakDetection(element,qualifiedName,"SAK_NO_FUZZY");
	}

	private void SakDetection(CtInterface element,String qualifiedName,String codesmell) {

		Set<String> valueSet=AnnotateProcessor.codesmells.get(codesmell);
		if (valueSet!=null && valueSet.contains(qualifiedName)) {
			Class<codesmells.annotations.Sak> annotationType = codesmells.annotations.Sak.class;
			AnnotationFactory factory = new AnnotationFactory(element.getFactory());
			factory.annotate(element, annotationType);
		}
	}

}