package app.exception;

/**
 * Throw when a method have too many exception.
 * 
 * @author guillaume
 *
 */
public class PapWebRunTimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public PapWebRunTimeException() {
		super();
	}

	/**
	 * @param str
	 */
	public PapWebRunTimeException(String str) {
		super(str);
	}

}
