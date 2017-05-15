package app.utils;


/**
 * 
 * @author guillaume
 *
 *         Cette classe doit être refaite, celle ci est un gros code smell
 *
 */
public class PaprikaKeyWords {

	public static final String LABELKEY = "Key";
	public static final String LABELUSER = "User";
	public static final String LABELPROJECT = "Project";
	public static final String LABELAPP = "Code";
	public static final String LABELQUERY = "CodeSmells";
	public static final String REPERTORY="application/";
	
	public static final String ATTRIBUTE_EMAIL = "email";
	public static final String ATTRIBUTE_PWD = "hashpwd";
	public static final String ATTRIBUTE_SALT = "salt";
	public static final String ATTRIBUTE_NB_APP = "nbApp";
	public static final String ATTRIBUTE_NB_VERSION = "nbVersion";
	
	
	public static final String REL_USER_PROJECT = "BELONGS";
	public static final String REL_PROJECT_VERSION = "HAVE_THE";
	public static final String REL_VERSION_CODE = "IS_STRUCTURED";
	public static final String REL_VERSION_CODESMELLS = "EXHIBITS";
	public static final String REL_CAS_CODE="HAS_CODESMELL";
	public static final String REL_CODESMELLS_CAS="RESULT";
	
	public static final String CODEA = "code_is_analyzed";
	public static final String NAMEATTRIBUTE = "name";
	public static final String NAMELABEL = "target";
	public static final String VERSIONLABEL = "Version";
	public static final String APPLICATION="application";

	public static final String ORDER = "order";
	public static final String ANALYSEINLOAD = "analyseInLoading";
	
	private PaprikaKeyWords(){
		
	}

}
