package paprikaana.utils.neo4j;


/**
 * 
 * @author guillaume
 *
 *   Classe qui sert à stocker des static final variables
 */
public class PaprikaKeyWords {

	public static final String LABELAPP = "Code";
	public static final String LABELQUERY = "CodeSmells";

	public static final String REL_CAS_CODE="HAS_CODESMELL";
	public static final String REL_VERSION_CODE = "IS_STRUCTURED";
	public static final String REL_VERSION_CODESMELLS = "EXHIBITS";

	public static final String REL_CODESMELLS_CAS="RESULT";
	
	public static final String APPKEY = "app_key";
	public static final String CODEA = "code_is_analyzed";
	public static final String NAMELABEL = "target";
	public static final String VERSIONLABEL = "Version";
	public static final String ANALYSEINLOAD = "analyseInLoading";
	
	private PaprikaKeyWords(){
		
	}

}
