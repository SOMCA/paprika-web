package spoon.main;

import org.apache.log4j.Level;

import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.AnnotationFactory;

/**
 * Reports warnings when long method have be found.
 */
public class MethodProcessor extends AbstractProcessor<CtMethod> {
	@Override
	public void process(CtMethod element) {
		Integer limit = 26;

		if (element.getBody().getStatements().size() > limit) {
			getFactory().getEnvironment().report(this, Level.WARN, element, "Long method code smell");
			AnnotationFactory factory = new AnnotationFactory(element.getFactory());	
			Class<codesmells.annotations.Lm> annotationType = codesmells.annotations.Lm.class;
			CtAnnotation<?> annotation = factory.annotate(element, annotationType);
			annotation.addValue("limitInstructions", limit);
			annotation.addValue("currentInstructions", element.getBody().getStatements().size());

		}
	}
}