package paprikaana.model;

/**
 * @author guillaume
 *
 */
public abstract class EntityModel {
	private String name;
	private long id;
	
	
	/**
	 * @param name
	 * @param id
	 */
	public EntityModel(String name,long id){
		this.name=name;
		this.id=id;
	}
	
	/**
	 * @return return the name of the entity
	 */
	public String getName(){
		return this.name;
	}
	/**
	 * @return  return the id of the entity
	 */
	public long getID(){
		
		return this.id;
	}
}
