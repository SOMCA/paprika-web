package spoon.main;

import java.io.IOException;

import org.neo4j.cypher.CypherException;

import analyzer.GraphCreator;
import analyzer.MainProcessor;
import entities.PaprikaApp;
import entities.PaprikaLibrary;
import metrics.MetricsCalculator;
import neo4j.*;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import spoon.utils.neo4j.QueryEngineBolt;


public class Analyse {

	public PaprikaApp runAnalysis(String[] args) {
		ArgumentParser parser = ArgumentParsers.newArgumentParser("paprika");
		Subparsers subparsers = parser.addSubparsers().dest("sub_command");

		Subparser analyseParser = subparsers.addParser("analyse").help("Analyse an app");
        analyseParser.addArgument("-i","--input").help("Path of the input folder");
        analyseParser.addArgument("-o","--output").help("Path of the ouput folder");
        analyseParser.addArgument("-a", "--androidJar").required(true).help("Path to android platform jar");
        analyseParser.addArgument("-n", "--name").required(true).help("Name of the application");
        analyseParser.addArgument("-k", "--key").required(true).help("sha256 of the apk used as identifier");
        analyseParser.addArgument("-l", "--libs").help("List of the external libs used by the apps (separated by :)");


		Namespace res=null;
		try {
		 res = parser.parseArgs(args);
		} catch (ArgumentParserException e) {
		}

		return under_runAnalysis(res);

	}
	
	 public  PaprikaApp under_runAnalysis(Namespace arg){
	        System.out.println("Collecting metrics");
	        String input = arg.getString("input");
	        String output = arg.getString("output");
	        String name = arg.getString("name");
	        String version = arg.getString("version");
	        String key = arg.getString("key");
	        String sdkPath = arg.getString("androidJar");
	        String[] libs = arg.getString("libs").split(":");
	        MainProcessor mainProcessor = new MainProcessor(name, version, key, input,output, sdkPath);
	        mainProcessor.process();
	        GraphCreator graphCreator = new GraphCreator(MainProcessor.currentApp);
	        graphCreator.createClassHierarchy();
	        graphCreator.createCallGraph();
	        if(libs !=null)
	        {
	            for(String lib : libs){
	                addLibrary(MainProcessor.currentApp,lib);
	            }
	        }
	        MetricsCalculator.calculateAppMetrics(MainProcessor.currentApp);
	        System.out.println("Saving into database "+arg.getString("database"));
	        System.out.println("Done");
	        
	        return MainProcessor.currentApp;
	    }


	public  void runQueryMode(String[] args)  {
		ArgumentParser parser = ArgumentParsers.newArgumentParser("paprika");
		Subparsers subparsers = parser.addSubparsers().dest("sub_command");

		Subparser queryParser = subparsers.addParser("query").help("Query the database");
		queryParser.addArgument("-k", "--key").required(true)
				.help("The application key for found the file on the database");
		queryParser.addArgument("-r", "--request").help("Request to execute");
		queryParser.addArgument("-dk", "--delKey").help("key to delete");
		queryParser.addArgument("-dp", "--delPackage").help("Package of the applications to delete");
		queryParser.addArgument("-d", "--details").type(Boolean.class).setDefault(false)
				.help("Show the concerned entity in the results");
		try {
			Namespace res = parser.parseArgs(args);
			this.queryMode(res);
		} catch (ArgumentParserException e) {
		} catch (CypherException e){
		}catch (IOException e){
		}
	}


    public static void addLibrary(PaprikaApp paprikaApp, String libraryString){
        PaprikaLibrary.createPaprikaLibrary(libraryString,paprikaApp);
    }
	private void launchStats(QueryEngineBolt queryEngine) throws IOException{
		QuartileCalculator quartileCalculator = new QuartileCalculator(queryEngine);
		quartileCalculator.calculateClassComplexityQuartile();
		quartileCalculator.calculateLackofCohesionInMethodsQuartile();
		quartileCalculator.calculateNumberOfAttributesQuartile();
		quartileCalculator.calculateNumberOfImplementedInterfacesQuartile();
		quartileCalculator.calculateNumberOfMethodsQuartile();
		quartileCalculator.calculateNumberofInstructionsQuartile();
		quartileCalculator.calculateCyclomaticComplexityQuartile();
		quartileCalculator.calculateNumberOfMethodsForInterfacesQuartile();
	}
	private void launchALLINFO(QueryEngineBolt queryEngine)throws IOException{
		queryEngine.getAllLCOM();
		queryEngine.getAllCyclomaticComplexity();
		queryEngine.getAllClassComplexity();
		queryEngine.getAllNumberOfMethods();
		queryEngine.countVariables();
		queryEngine.countInnerClasses();
		queryEngine.countAsyncClasses();
		queryEngine.countViews();
	}
	private void launchALLAP(QueryEngineBolt queryEngine,	Boolean details)throws IOException {
		ARGB8888Query.createARGB8888Query(queryEngine).execute(details);
		CCQuery.createCCQuery(queryEngine).executeFuzzy(details);
		LMQuery.createLMQuery(queryEngine).executeFuzzy(details);
		SAKQuery.createSAKQuery(queryEngine).executeFuzzy(details);
		BLOBQuery.createBLOBQuery(queryEngine).executeFuzzy(details);
		MIMQuery.createMIMQuery(queryEngine).execute(details);
		IGSQuery.createIGSQuery(queryEngine).execute(details);
		LICQuery.createLICQuery(queryEngine).execute(details);
		NLMRQuery.createNLMRQuery(queryEngine).execute(details);
		OverdrawQuery.createOverdrawQuery(queryEngine).execute(details);
		HeavyServiceStartQuery.createHeavyServiceStartQuery(queryEngine).executeFuzzy(details);
		HeavyBroadcastReceiverQuery.createHeavyBroadcastReceiverQuery(queryEngine).executeFuzzy(details);
		HeavyAsyncTaskStepsQuery.createHeavyAsyncTaskStepsQuery(queryEngine).executeFuzzy(details);
		UnsuitedLRUCacheSizeQuery.createUnsuitedLRUCacheSizeQuery(queryEngine).execute(details);
		InitOnDrawQuery.createInitOnDrawQuery(queryEngine).execute(details);
		UnsupportedHardwareAccelerationQuery.createUnsupportedHardwareAccelerationQuery(queryEngine)
				.execute(details);
		HashMapUsageQuery.createHashMapUsageQuery(queryEngine).execute(details);
		InvalidateWithoutRectQuery.createInvalidateWithoutRectQuery(queryEngine).execute(details);
		TrackingHardwareIdQuery.createTrackingHardwareIdQuery(queryEngine).execute(details);
	}
	
	private void launchFORCENOFUZZY(QueryEngineBolt queryEngine,Boolean details)throws IOException {
		CCQuery.createCCQuery(queryEngine).execute(details);
		LMQuery.createLMQuery(queryEngine).execute(details);
		SAKQuery.createSAKQuery(queryEngine).execute(details);
		BLOBQuery.createBLOBQuery(queryEngine).execute(details);
		HeavyServiceStartQuery.createHeavyServiceStartQuery(queryEngine).execute(details);
		HeavyBroadcastReceiverQuery.createHeavyBroadcastReceiverQuery(queryEngine).execute(details);
		HeavyAsyncTaskStepsQuery.createHeavyAsyncTaskStepsQuery(queryEngine).execute(details);
	}
	
	public void queryMode(Namespace arg) throws IOException {
		QueryEngineBolt queryEngine = new QueryEngineBolt(Long.parseLong(arg.getString("key")));
		String request = arg.get("request");
		Boolean details = arg.get("details");
		switch (request) {
		case "STATS":
			launchStats(queryEngine);
			break;
		case "ALLINFO":
			launchALLINFO(queryEngine);
			break;
		case "ALLAP":
			launchALLAP(queryEngine,details);
			break;
		case "FORCENOFUZZY":
			launchFORCENOFUZZY(queryEngine,details);
			break;
		default:
			queryEngine.executeRequest(request);
		}
	}
}
