package me.despical.commandframework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Despical
 * <p>
 * Created at 20.09.2024
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Option.OptionContainer.class)
public @interface Option {

	String name();

	String prefix() default "--";

	String valueSeparator() default ",";

	String keySeparator() default "=";

	boolean allowSeparating() default true;

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@interface OptionContainer {

		Option[] value();
	}
}