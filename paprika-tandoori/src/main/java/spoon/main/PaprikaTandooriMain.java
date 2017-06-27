package spoon.main;

import java.io.IOException;

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
		
		DriverBolt.setHostName("spirals-somca");

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
