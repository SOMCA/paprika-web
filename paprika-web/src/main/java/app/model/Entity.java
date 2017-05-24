package app.model;

/**
 * Entity is a abstract clas who contains a name and a id.
 * @author guillaume
 *
 */
public abstract class Entity {
	private String name;
	private long id;
	
	
	/**
	 * @param name name of the entity.
	 * @param id id of the entity.
	 */
	public Entity(String name,long id){
		this.name=name;
		this.id=id;
	}
	
	/**
	 * @return the name of the entity.
	 */
	public String getName(){
		return this.name;
	}
	/**
	 * @return the id of the entity.
	 */
	public long getID(){
		
		return this.id;
	}
	
	
	
}
