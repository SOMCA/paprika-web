package app.model;

public abstract class Entity {
	private String name;
	private long id;
	
	
	public Entity(String name,long id){
		this.name=name;
		this.id=id;
	}
	
	public String getName(){
		return this.name;
	}
	public long getID(){
		
		return this.id;
	}
	
	
	
}
