package app.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Node;

import app.application.PaprikaFacade;
import app.functions.VersionFunctions;
import app.utils.PaprikaKeyWords;
import app.utils.neo4j.LowNode;


public class Version  extends Entity{

	private boolean analyzed;
	
	public Version(String name, long id) {
		super(name, id);
		this.checkAnalyzed();
	}
	

	public long getNumberCodeSmells(){
		VersionFunctions verfct=new VersionFunctions();
		long number=verfct.getNumberOfSmells(this.getID());
		
		if(number==0){
		Iterator<CodeSmells> iter=this.getAllCodeSmells();
		while(iter.hasNext()){
			number+=iter.next().getNumberOfSmells();
		}
		verfct.applyNumberOfCodeSmells(this.getID(), number);
		}
		return number;	
	}
	
	public boolean checkAnalyzed(){
		LowNode nodeVer= new LowNode(PaprikaKeyWords.VERSIONLABEL);
		nodeVer.setId(getID());
		PaprikaFacade facade= PaprikaFacade.getInstance();
		this.analyzed=(facade.getParameter(nodeVer, PaprikaKeyWords.CODEA)!=null);
		return this.analyzed;
	}
	
	public boolean isAnalyzed(){
		return this.analyzed;
	}
	
	
	public long getOrder(){
		return new VersionFunctions().getOrder(this.getID());
	}

	public Iterator<CodeSmells> getAllCodeSmells() {

		List<CodeSmells> listNode = new ArrayList<>();
	
		StatementResult result = new VersionFunctions().loadDataCodeSmell(this);
		Record record;
		Node node;
		CodeSmells codesmell;
		String label;
		org.neo4j.driver.v1.Value number;
		while (result.hasNext()) {
			record = result.next();
			node = record.get(PaprikaKeyWords.NAMELABEL).asNode();
			number = node.get("number");

			if (number != null) {
				
				label=node.labels().iterator().next();
				codesmell = new CodeSmells(label,node.id(),number.asLong());
			
				listNode.add(codesmell);
			}

		}

		return listNode.iterator();
	}

}
