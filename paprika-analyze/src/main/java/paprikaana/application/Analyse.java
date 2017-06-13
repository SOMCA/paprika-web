package paprikaana.application;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.neo4j.cypher.CypherException;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;
import paprikaana.entities.PaprikaApp;
import paprikaana.utils.neo4j.query.ARGB8888Query;
import paprikaana.utils.neo4j.query.BLOBQuery;
import paprikaana.utils.neo4j.query.CCQuery;
import paprikaana.utils.neo4j.query.HashMapUsageQuery;
import paprikaana.utils.neo4j.query.HeavyAsyncTaskStepsQuery;
import paprikaana.utils.neo4j.query.HeavyBroadcastReceiverQuery;
import paprikaana.utils.neo4j.query.HeavyServiceStartQuery;
import paprikaana.utils.neo4j.query.IGSQuery;
import paprikaana.utils.neo4j.query.InitOnDrawQuery;
import paprikaana.utils.neo4j.query.InvalidateWithoutRectQuery;
import paprikaana.utils.neo4j.query.LICQuery;
import paprikaana.utils.neo4j.query.LMQuery;
import paprikaana.utils.neo4j.query.MIMQuery;
import paprikaana.utils.neo4j.query.NLMRQuery;
import paprikaana.utils.neo4j.query.OverdrawQuery;
import paprikaana.utils.neo4j.query.QuartileCalculator;
import paprikaana.utils.neo4j.query.QueryEngineBolt;
import paprikaana.utils.neo4j.query.SAKQuery;
import paprikaana.utils.neo4j.query.TrackingHardwareIdQuery;
import paprikaana.utils.neo4j.query.UnsuitedLRUCacheSizeQuery;
import paprikaana.utils.neo4j.query.UnsupportedHardwareAccelerationQuery;
import paprikaana.analyzer.Analyzer;
import paprikaana.analyzer.SootAnalyzer;

public class Analyse {

	public PaprikaApp runAnalysis(String[] args) {
		PaprikaAnalyzeMain.LOGGER.trace("Start runAnalysis");
		ArgumentParser parser = ArgumentParsers.newArgumentParser("paprika");
		Subparsers subparsers = parser.addSubparsers().dest("sub_command");

		Subparser analyseParser = subparsers.addParser("analyse").help("Analyse an app");
		analyseParser.addArgument("apk").help("Path of the APK to analyze");
		analyseParser.addArgument("-a", "--androidJars").required(true).help("Path to android platforms jars");
		analyseParser.addArgument("-db", "--database").required(false).help("Path to neo4J Database folder");
		analyseParser.addArgument("-n", "--name").required(true).help("Name of the application");
		analyseParser.addArgument("-p", "--package").required(true).help("Application main package");
		analyseParser.addArgument("-k", "--key").required(true).help("sha256 of the apk used as identifier");
		analyseParser.addArgument("-dev", "--developer").required(true).help("Application developer");
		analyseParser.addArgument("-cat", "--category").required(true).help("Application category");
		analyseParser.addArgument("-nd", "--nbDownload").required(true).help("Numbers of downloads for the app");
		analyseParser.addArgument("-d", "--date").required(true).help("Date of download");
		analyseParser.addArgument("-r", "--rating").type(Double.class).required(true).help("application rating");
		analyseParser.addArgument("-pr", "--price").setDefault("Free").help("Price of the application");
		analyseParser.addArgument("-s", "--size").type(Integer.class).required(true).help("Size of the application");
		analyseParser.addArgument("-u", "--unsafe").help("Unsafe mode (no args checking)");
		analyseParser.addArgument("-vc", "--versionCode").setDefault("")
				.help("Version Code of the application (extract from manifest)");
		analyseParser.addArgument("-vn", "--versionName").setDefault("")
				.help("Version Name of the application (extract from manifest)");
		analyseParser.addArgument("-tsdk", "--targetSdkVersion").setDefault("")
				.help("Target SDK Version (extract from manifest)");
		analyseParser.addArgument("-sdk", "--sdkVersion").setDefault("").help("sdk version (extract from manifest)");
		analyseParser.addArgument("-omp", "--onlyMainPackage").type(Boolean.class).setDefault(false)
				.help("Analyze only the main package of the application");

		Namespace res=null;
		try {
		 res = parser.parseArgs(args);
		} catch (ArgumentParserException e) {
			PaprikaAnalyzeMain.LOGGER.error("runAnalysis: ArgumentParserException",e);
			throw new AnalyseException();
		}
		PaprikaAnalyzeMain.LOGGER.trace("Collecting metrics");

		if (res.get("unsafe") == null) {
			try {
				checkArgs(res);
			} catch (NoSuchAlgorithmException e) {
				PaprikaAnalyzeMain.LOGGER.error("runAnalysis: NoSuchAlgorithmException",e);
				throw new AnalyseException();
			} catch (IOException e) {
				PaprikaAnalyzeMain.LOGGER.error("runAnalysis: IOException",e);
				throw new AnalyseException();
			}
		}
		Analyzer analyzer = new SootAnalyzer(res.getString("apk"), res.getString("androidJars"), res.getString("name"),
				res.getString("key"), res.getString("package"), res.getString("date"), res.getInt("size"),
				res.getString("developer"), res.getString("category"), res.getString("price"), res.getDouble("rating"),
				res.getString("nbDownload"), res.getString("versionCode"), res.getString("versionName"),
				res.getString("sdkVersion"), res.getString("targetSdkVersion"), res.getBoolean("onlyMainPackage"));
		PaprikaAnalyzeMain.LOGGER.trace("start init");

		analyzer.init();
		PaprikaAnalyzeMain.LOGGER.trace("End init, begin analyze");

		analyzer.runAnalysis();
		PaprikaAnalyzeMain.LOGGER.trace("End collecting metrics");

		PaprikaAnalyzeMain.LOGGER.trace("End runAnalysis");
		return analyzer.getPaprikaApp();

	}

	public void checkArgs(Namespace arg) throws NoSuchAlgorithmException, IOException {
		String sha256 = computeSha256(arg.getString("apk"));
		if (!sha256.equals(arg.getString("key"))) {
			PaprikaAnalyzeMain.LOGGER.error("The given key is different from sha256 of the apk");
			throw new AnalyseException("The given key is different from sha256 of the apk");
		}
		if (!arg.getString("date").matches(
				"^([0-9]{4})-([0-1][0-9])-([0-3][0-9])\\s([0-1][0-9]|[2][0-3]):([0-5][0-9]):([0-5][0-9]).([0-9]*)$")) {
			PaprikaAnalyzeMain.LOGGER.error("Date should be formatted : yyyy-mm-dd hh:mm:ss.S");
			throw new AnalyseException("Date should be formatted : yyyy-mm-dd hh:mm:ss.S");
		}
	}

	private String computeSha256(String path) throws IOException, NoSuchAlgorithmException {
		byte[] buffer = new byte[2048];
		MessageDigest digest = MessageDigest.getInstance("SHA-256");

		try (InputStream is = new FileInputStream(path)) {
			while (true) {
				int readBytes = is.read(buffer);
				if (readBytes > 0)
					digest.update(buffer, 0, readBytes);
				else
					break;
			}
		}
		byte[] hashValue = digest.digest();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < hashValue.length; i++) {
			sb.append(Integer.toString((hashValue[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}

	public  void runQueryMode(String[] args)  {
		PaprikaAnalyzeMain.LOGGER.trace("Start runQueryMode");
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
			PaprikaAnalyzeMain.LOGGER.error("runQueryMode: ArgumentParserException",e);
			throw new AnalyseException();
		} catch (CypherException e){
			PaprikaAnalyzeMain.LOGGER.error("runQueryMode: CypherException",e);
			throw new AnalyseException();
		}catch (IOException e){
			PaprikaAnalyzeMain.LOGGER.error("runQueryMode: IOException",e);
			throw new AnalyseException();
		}
		PaprikaAnalyzeMain.LOGGER.trace("End runQueryMode");
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
		PaprikaAnalyzeMain.LOGGER.trace("Executing Queries");
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
			PaprikaAnalyzeMain.LOGGER.trace("Executing custom request");
			queryEngine.executeRequest(request);
		}
		PaprikaAnalyzeMain.LOGGER.trace("Done");
	}
}
