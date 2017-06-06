package spoon.main;


import java.io.File;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;

import spoon.Launcher;
import spoon.processing.ProcessInterruption;


public class App 
{
    public static void main( String[] args ) throws Exception {

    	
    	/*Git git = new Git("./input");
    	AddCommand add = git.add();
    	add.addFilepattern("someDirectory").call();
    	*/
    	
    	
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
		//launcher.getEnvironment().setCommentEnabled(true);
	
		final MethodProcessor methodprocessor = new MethodProcessor();
		launcher.addProcessor(methodprocessor);
		final ClassProcessor classprocessor = new ClassProcessor();
		launcher.addProcessor(classprocessor);
		final InterfaceProcessor interfaceProcessor = new InterfaceProcessor();
		launcher.addProcessor(interfaceProcessor);
		try {
			
			launcher.run();
		} catch (ProcessInterruption e) {
			System.out.println("ok");
		}
	//	System.out.println(methodprocessor.getFactory().getEnvironment().getWarningCount());
		
		//System.out.println(classprocessor.getFactory().getEnvironment().getWarningCount());
		
	//	System.out.println(launcher.getFactory().Class().get("Appp"));
		
		
	}
    
}

