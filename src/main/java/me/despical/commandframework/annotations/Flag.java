package me.despical.commandframework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify flags for command arguments.
 * This annotation can be used multiple times on the same method.
 *
 * <p>The {@code value()} method holds the list of flags, and the
 * {@code prefix()} method defines the default prefix for the flags,
 * which is {@code --} by default.</p>
 *
 * <p>The {@link FlagContainer} is a container annotation that allows
 * the {@link Flag} annotation to be repeatable on methods.</p>
 *
 * @author Despical
 * <p>Created at 20.09.2024</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Flag.FlagContainer.class)
public @interface Flag {

	/**
	 * Specifies the list of flag values.
	 *
	 * @return an array of flag values
	 */
	String[] value();

	/**
	 * Specifies the prefix for the flags. Default is "--".
	 *
	 * @return the flag prefix
	 */
	String prefix() default "--";

	/**
	 * Container annotation for holding multiple {@link Flag} annotations
	 * on the same method.
	 *
	 * <p>Used internally by the {@link Repeatable} annotation.</p>
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@interface FlagContainer {

		/**
		 * Holds an array of {@link Flag} annotations.
		 *
		 * @return an array of {@link Flag} annotations
		 */
		Flag[] value();
	}
}