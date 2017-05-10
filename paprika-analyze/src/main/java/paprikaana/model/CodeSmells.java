package paprikaana.model;

public class CodeSmells extends EntityModel {

	private long numberOfSmells;
	private String description;
	private String longName;

	public CodeSmells(String name, long id, long numberOfSmells) {
		super(name, id);
		this.numberOfSmells = numberOfSmells;
		description="";

	}

	public long getNumberOfSmells() {
		return this.numberOfSmells;
	}

	public String getDescription() {
		return this.description;
	}

	public String getLongName() {
		return this.longName;
	}

	
	
}
