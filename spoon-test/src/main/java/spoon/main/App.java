package spoon.main;


import java.io.File;
import spoon.Launcher;
import spoon.processing.ProcessInterruption;
import spoon.annotations.*;
/**
 * Hello world!
 *
 */
@Blob()
public class App 
{
    public static void main( String[] args ) throws Exception {

		final Launcher launcher = new Launcher();
		launcher.getEnvironment().setNoClasspath(true);
		
		// Analyze only file on the input directory
		launcher.addInputResource("./input");
		// Put all analyzed file (transformed or not) on the ouput directory
		String out="./output";
		
		// Clean the output directory
		File directory_out = new File(out);
		String[]entries = directory_out.list();
		for(String s: entries){
		    File currentFile = new File(directory_out.getPath(),s);
		    currentFile.delete();
		}
		launcher.setSourceOutputDirectory(out);
		launcher.getEnvironment().setCommentEnabled(true);
		final CatchProcessor processor = new CatchProcessor();
		launcher.addProcessor(processor);

		
		try {
			
			launcher.run();
		} catch (ProcessInterruption e) {
			System.out.println("ok");
		}

		System.out.println(processor.getFactory().getEnvironment().getWarningCount());
		
	//	System.out.println(launcher.getFactory().Class().get("Appp"));
		
		
	}
}

