package paprikaana.model;

/**
 * @author guillaume
 *
 */
public class CodeSmells extends EntityModel {

	private long numberOfSmells;
	private String description;
	private String longName;

	/**
	 * @param name
	 * @param id
	 * @param numberOfSmells
	 */
	public CodeSmells(String name, long id, long numberOfSmells) {
		super(name, id);
		this.numberOfSmells = numberOfSmells;
		description="";

	}

	/**
	 * @return return the number of code smells founded.
	 */
	public long getNumberOfSmells() {
		return this.numberOfSmells;
	}

	/**
	 * @return return the description of the code smells
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * @return return the long name of the code smells.
	 */
	public String getLongName() {
		return this.longName;
	}

	
	
}
