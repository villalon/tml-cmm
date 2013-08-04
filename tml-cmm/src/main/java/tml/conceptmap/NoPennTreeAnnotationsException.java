/**
 * 
 */
package tml.conceptmap;

/**
 * Exception indicating that no PennTree annotations are available to build
 * a CM from text.
 * 
 * @author Jorge Villalon
 *
 */
public class NoPennTreeAnnotationsException extends Exception {

	/**
	 * For serializarion
	 */
	private static final long serialVersionUID = -4808908208900478369L;

	public NoPennTreeAnnotationsException() {
		super("No PennTree annotations found.");
	}
	
	public NoPennTreeAnnotationsException(Exception e) {
		super("No PennTree annotations found.", e);
	}
}
