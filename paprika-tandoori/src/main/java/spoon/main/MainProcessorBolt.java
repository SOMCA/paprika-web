package spoon.main;

import tandoori.entities.PaprikaApp;
import spoon.Launcher;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import tandoori.analyzer.ClassProcessor;
import tandoori.analyzer.InterfaceProcessor;
import tandoori.analyzer.MainProcessor;

/**
 * MainProcessor modified for Paprika-web
 */
public class MainProcessorBolt extends MainProcessor {


    private String input;
   // private String output;


    public MainProcessorBolt(String appName, String appVersion, String appKey, String input, String output, String jarsPath ) {
        super( appName,  appVersion,  appKey,  "",  "",  jarsPath);
    	MainProcessorBolt.currentApp = PaprikaApp.createPaprikaApp(appName, appVersion, appKey);
    	MainProcessorBolt.currentClass = null;
    	MainProcessorBolt.currentMethod = null;
        this.appPath = "";
        this.jarsPath = jarsPath;
    	this.input = input;
      //  this.output = output;
        this.sdkPath = "";
    }
    @Override
    public void process() {
        Launcher launcher = new Launcher();
        launcher.addInputResource(input);
        launcher.getEnvironment().setNoClasspath(true);
        File folder = new File(jarsPath);
        try {
        	MainProcessor.paths = this.listFilesForFolder(folder);
            //paths.add(new File(sdkPath).toURI().toURL());
            String[] cl = new String[paths.size()];
            for (int i = 0; i < paths.size(); i++) {
                URL url = paths.get(i);
                cl[i] = url.getPath();
            }
            launcher.getEnvironment().setSourceClasspath(cl);
            launcher.buildModel();
            ClassProcessor classProcessor = new ClassProcessor();
            InterfaceProcessor interfaceProcessor =new InterfaceProcessor();
            launcher.addProcessor(classProcessor);
            launcher.addProcessor(interfaceProcessor);
            launcher.process();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

    }
    @Override
    public ArrayList<URL> listFilesForFolder(final File folder) throws IOException {
        ArrayList<URL> jars = new ArrayList<>();
        if(folder.listFiles()==null){
            return jars;
        }
        for (final File fileEntry : folder.listFiles()) {

            jars.add(fileEntry.toURI().toURL());

        }
        return jars;
    }
}
