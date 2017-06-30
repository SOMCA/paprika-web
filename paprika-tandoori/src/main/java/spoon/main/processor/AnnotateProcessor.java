package spoon.main.processor;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;


import spoon.Launcher;

/**
 * Class who launch all annotate Processors
 * 
 * @author guillaume
 *
 */
@SuppressWarnings("javadoc")
public class AnnotateProcessor {

	private String input;
	private String output;

	public static final Map<String,Set<String>> codesmells= new HashMap<>();


	public AnnotateProcessor(String input, String output) {
		this.input = input;
		this.output = output;
	}

	public void process() {
		Launcher launcher = new Launcher();
		launcher.addInputResource(input);
		launcher.getEnvironment().setNoClasspath(true);

		launcher.setSourceOutputDirectory(output);
		launcher.getEnvironment().setAutoImports(true);


		ClassAnnotateProcessor classProcessor = new ClassAnnotateProcessor();
		InterfaceAnnotateProcessor interfaceProcessor = new InterfaceAnnotateProcessor();
		launcher.addProcessor(classProcessor);
		launcher.addProcessor(interfaceProcessor);
		launcher.run();

	}

}
