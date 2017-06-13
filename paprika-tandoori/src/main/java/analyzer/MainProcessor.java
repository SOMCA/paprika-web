package analyzer;

import entities.PaprikaApp;
import entities.PaprikaClass;
import entities.PaprikaMethod;
import spoon.Launcher;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by sarra on 21/02/17.
 */
public class MainProcessor {

    public static PaprikaApp currentApp;
    public static PaprikaClass currentClass;
    public static PaprikaMethod currentMethod;
    public static ArrayList<URL> paths;
    private String input;
    private String output;
    private String sdkPath;

    public MainProcessor(String appName, String appVersion, String appKey, String input, String output, String sdkPath ) {
        this.currentApp = PaprikaApp.createPaprikaApp(appName, appVersion, appKey);
        this.currentClass = null;
        this.currentMethod = null;
        this.input = input;
        this.output = output;
        this.sdkPath = sdkPath;
    }

    public void process() {
        Launcher launcher = new Launcher();
        launcher.addInputResource(input);
        launcher.getEnvironment().setNoClasspath(true);
        try {
            paths = new ArrayList<URL>();
            paths.add(new File(sdkPath).toURI().toURL());
            String[] cl = new String[paths.size()];
            for (int i = 0; i < paths.size(); i++) {
                URL url = paths.get(i);
                cl[i] = url.getPath();
            }
            launcher.getEnvironment().setSourceClasspath(cl);
    		launcher.setSourceOutputDirectory(output);
    		launcher.getEnvironment().setAutoImports(true);

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
