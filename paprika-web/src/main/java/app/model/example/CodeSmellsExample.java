package app.model.example;

import app.model.CodeSmells;

/**
 * a CodeSmells Example of a version Example.
 * 
 * @author guillaume
 *
 */
public class CodeSmellsExample extends CodeSmells {

	private String lineList;
	
	
	public CodeSmellsExample(String name, long id, long numberOfSmells) {
		super(name, id, numberOfSmells);
		lineList="";
	}
	
	public void setLineList(String line){
		this.lineList=line;
	}

	@Override
	public String getLineList() {
		return lineList;
	}

	
}
