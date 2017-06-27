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
		return DataSave.nbUser;
	}
	public long getNbProject(){
		return DataSave.nbProject;
	}	
	public long getNbVersion(){
		return DataSave.nbVersion;
	}
	public void setNbUser(long nb){
		DataSave.nbUser=nb;
	}
	public void setNbProject(long nb){
		DataSave.nbProject=nb;
	}	
	public void setNbVersion(long nb){
		DataSave.nbVersion=nb;
	}
	

}
