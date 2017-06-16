package app.utils;


/**
 * This class is used for know how many we have to user,project or version.
 * @author guillaume
 *
 */
@SuppressWarnings("javadoc")
public class DataSave {
	private static long nbUser=0;
	private static long nbProject=0;
	private static long nbVersion=0;
	
	
	public DataSave(){
		/*if(this.nbUser==-1)
		new DataSaveFunctions().updateStatic();*/
	}
	
	public long getNbUser(){
		return this.nbUser;
	}
	public long getNbProject(){
		return this.nbProject;
	}	
	public long getNbVersion(){
		return this.nbVersion;
	}
	public void setNbUser(long nb){
		 this.nbUser=nb;
	}
	public void setNbProject(long nb){
		 this.nbProject=nb;
	}	
	public void setNbVersion(long nb){
		 this.nbVersion=nb;
	}
	

}
