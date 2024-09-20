package me.despical.commandframework.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Despical
 * <p>
 * Created at 29.03.2024
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {

	/**
	 * The unique id of the parameter.
	 * <p>
	 * If a {@link Class} name is used as an id then no need for
	 * this annotation to be used. The parameters can directly be
	 * used from method's parameter list.
	 * </p>
	 *
	 * @return the unique id to call annotated parameter
	 */
	String value();
}