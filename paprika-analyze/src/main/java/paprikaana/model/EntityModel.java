package paprikaana.model;

public abstract class EntityModel {
	private String name;
	private long id;
	
	
	public EntityModel(String name,long id){
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
