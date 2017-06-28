package spoon.main.processor;


import java.util.Set;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.factory.AnnotationFactory;

/**
 * Add annotations for interface
 * 
 * @author guillaume
 *
 */
//We use many rawtypes.
@SuppressWarnings("rawtypes")
public class InterfaceAnnotateProcessor extends AbstractProcessor<CtInterface> {
	private CtInterface element;
	private String qualifiedName;
	private AnnotationFactory factory;
	
	@Override
	public void process(CtInterface element) {
        String qualifiedName = element.getQualifiedName();
        if (element.isAnonymous()) {
            String[] splitName = qualifiedName.split("\\$");
            qualifiedName = splitName[0] + "$" +
                    ((CtNewClass) element.getParent()).getType().getQualifiedName() + splitName[1];
        }
		
		AnnotationFactory factory = new AnnotationFactory(element.getFactory());
		this.element=element;
		this.qualifiedName=qualifiedName;
		this.factory=factory;
		
        
        SakDetection("SAK");
		SakDetection("SAK_NO_FUZZY");
	}
	private boolean isOnSet(String codesmells){
		Set<String> valueSet=AnnotateProcessor.codesmells.get(codesmells);
		return (valueSet!=null && valueSet.contains(qualifiedName));
	}


	private void SakDetection(String codesmell) {
		if(this.isOnSet(codesmell)){
			Class<codesmells.annotations.SAK> annotationType = codesmells.annotations.SAK.class;
			factory.annotate(element, annotationType);
		}
	}

}