/**
 * @author Ben-Noah Engelhaupt (code@bensoft.de) GitHub: bensoftde
 *
 */
package de.bensoft.acis.core.environment;

import de.bensoft.acis.core.Action;

/**
 * Represents the Environment which is passed to the {@link Action}'s run()
 * {@link Action.ActionMethod}.
 * 
 * It consists of a {@link PackageEnvironment} and a {@link SystemEnvironment}.
 *
 */
public interface Environment extends PackageEnvironment, SystemEnvironment {

}
