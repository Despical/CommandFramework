package me.despical.commandframework.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Despical
 * <p>
 * Created at 30.03.2024
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Default {

	/**
	 * The default value for the parameter annotated with
	 * {@link Param}, if the value of parameter is null.
	 *
	 * @return the default value for the possible null parameter
	 */
	String value();
}