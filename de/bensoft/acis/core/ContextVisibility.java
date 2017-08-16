/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.core;

/**
 * Represents the visibility stages of {@link Action}s in the {@link ContextStack}.
 * <br>
 * PUBLIC: The whole user input String including any parameters.<br>
 * PUBLIC_NO_PARAMETERS: The user input String without the parameters.<br>
 * PRIVATE: Action does not appear in the ContextStack.<br>
 * PACKAGE: Actions of the same {@link ActionPackage} have access to the information of visibility stage PUBLIC.<br>
 * Every of the stages above (except PRIVATE) includes the {@link ActionResult}.
 * 
 */
public enum ContextVisibility {
	PUBLIC, PUBLIC_NO_PARAMETERS, PRIVATE, PACKAGE
}


