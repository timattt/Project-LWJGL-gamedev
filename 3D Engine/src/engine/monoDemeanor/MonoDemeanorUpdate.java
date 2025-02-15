/**
 * 
 */
package engine.monoDemeanor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author timat
 *
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = { ElementType.METHOD })
public @interface MonoDemeanorUpdate {
	MonoDemeanorPriority priority() default MonoDemeanorPriority.MEDIUM;
}
