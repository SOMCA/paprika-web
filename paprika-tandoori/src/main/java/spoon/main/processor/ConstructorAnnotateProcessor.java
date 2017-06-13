package spoon.main.processor;


import spoon.reflect.declaration.CtConstructor;



public class ConstructorAnnotateProcessor{

	public void process(CtConstructor element,String currentClass) {
		String qualifiedName = element.getSimpleName()+"#"+currentClass;

	}
    
}
