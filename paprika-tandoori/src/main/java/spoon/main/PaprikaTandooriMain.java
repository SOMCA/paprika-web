package spoon.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import tandoori.neo4jBolt.DriverBolt;
import tandoori.neo4jBolt.Graph;
import tandoori.neo4jBolt.LowNode;
import spoon.functions.VersionFunctions;

/**
 * 
 * 
 * @author guillaume
 *
 */
public class PaprikaTandooriMain {

	private PaprikaTandooriMain() {
	}

	

	/**
	 * Resume of what gone the Main:
	 * -He launch the Driver-Bolt with the hostname spirals-somca.
	 * -He launch the analyzeProcess.
	 * -He modify the version node for see the advance.
	 * 
	 * 
	 * @param args contains fname, id of the version node and the github link on this order.
	 */
	public static void main(String[] args) {

		if (args.length != 3)
			return;
		
		
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
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		String fName = args[0];
		LowNode nodeVer = new LowNode(Graph.VERSIONLABEL);
		nodeVer.setId(Long.parseLong(args[1]));

		String github = args[2];

		VersionFunctions verFct = new VersionFunctions();

		AnalyzeProcess anaThread;
		try {
			anaThread = new AnalyzeProcess(fName, nodeVer, github);
			verFct.setParameterOnNode(nodeVer.getID(), Graph.CODEA, "inprogress");
			anaThread.run();
		} catch (IOException e) {
			verFct.setParameterOnNode(nodeVer.getID(), Graph.CODEA, "error");
		}

	}

}
