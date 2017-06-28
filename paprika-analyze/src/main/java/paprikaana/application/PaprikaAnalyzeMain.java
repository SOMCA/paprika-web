package paprikaana.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import net.dongliu.apk.parser.ApkFile;
import paprika.neo4jBolt.Graph;
import paprika.neo4jBolt.LowNode;
import paprikaana.functions.VersionFunctions;
import paprika.neo4jBolt.DriverBolt;

/**
 * @author guillaume
 *
 */
public class PaprikaAnalyzeMain {
	/**
	 * 
	 */
	public static final Logger LOGGER = LogManager.getLogger();

	private PaprikaAnalyzeMain() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int leng = args.length;
		for (int i = 0; i < leng; i++) {
			PaprikaAnalyzeMain.LOGGER.trace(args[i]);
		}
		if (leng != 4) {
			return;
		}

		try {
			InputStream is;
			is = new FileInputStream("./info.json");
			String jsonTxt;
			jsonTxt = IOUtils.toString(is);
			JSONObject json = new JSONObject(jsonTxt);
			DriverBolt.setValue("7687", "neo4j", json.getString("neo4j_pwd"));
			if(DriverBolt.getSession()==null){
				DriverBolt.setHostName("spirals-somca");
				DriverBolt.updateDriver();
				if(DriverBolt.getSession()==null){
					return;
				}
				
			}
			if(DriverBolt.getSession()==null) return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		PaprikaAnalyzeMain.LOGGER.trace("Launch Analyse");
		String fName = args[0];
		String user = args[1];
		String project = args[2];
		LowNode nodeVer = new LowNode(Graph.VERSIONLABEL);
		nodeVer.setId(Long.parseLong(args[3]));
		String pathstr = "application/" + user + "/" + project + "/" + fName;

		ApkFile apkfile = null;
		VersionFunctions verFct = new VersionFunctions();
		try {
			File file = new File(pathstr);
			long size = file.length();

			apkfile = new ApkFile(pathstr);
			String xml = apkfile.getManifestXml();
			apkfile.close();

			AnalyzeProcess anaThread = new AnalyzeProcess(xml, fName, project, user, size, nodeVer);

			verFct.setParameterOnNode(nodeVer.getID(), Graph.CODEA, "inprogress");
			anaThread.run();
		} catch (IOException e) {
			PaprikaAnalyzeMain.LOGGER.error("IOException error: File not found", e);
			verFct.setParameterOnNode(nodeVer.getID(), Graph.CODEA, "error");
			throw new AnalyseException();
		}
	}
}