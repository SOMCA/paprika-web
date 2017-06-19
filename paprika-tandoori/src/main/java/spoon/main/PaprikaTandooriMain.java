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

	

	public static void main(String[] args) {
		// 0 : fname
		// 1 : version node id
		// 2 : github link

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

		// VersionFunctions verFct = new VersionFunctions();
		// Paprika_analyze analyze = null;
		//
		// try {
		// analyze = new Paprika_analyze(args[0], Long.parseLong(args[1]));
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// if (analyze != null) {
		// verFct.setParameterOnNode(Long.parseLong(args[1]),
		// Graph.CODEA, "inprogress");
		// if (analyze.run()) {
		// verFct.setParameterOnNode(Long.parseLong(args[1]),
		// Graph.CODEA, "error");
		// }
		// } else {
		// verFct.setParameterOnNode(Long.parseLong(args[1]),
		// Graph.CODEA, "error");
		//
		// }

	}

}
