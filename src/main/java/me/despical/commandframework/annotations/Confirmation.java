package me.despical.commandframework.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @author Despical
 * <p>
 * Created at 5.02.2024
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Confirmation {

	/**
	 * The message will be sent to the sender if they
	 * have not confirmed yet.
	 *
	 * @return the confirmation message.
	 */
	String message();

	/**
	 * The permission to bypass confirmations.
	 *
	 * @return the bypass permission.
	 */
	String bypassPerm() default "";

	/**
	 * How many seconds should pass so the confirmation
	 * gets expired.
	 *
	 * @return the time required for the confirmation period
	 * to expire.
	 * @see #timeUnit()
	 */
	int expireAfter();

	/**
	 * The time unit for {@code #expireAfter()}.
	 *
	 * @return the unit of expiration time.
	 */
	TimeUnit timeUnit() default TimeUnit.SECONDS;

	/**
	 * If option is true, console will be affected by
	 * confirmations; otherwise, it will override the
	 * confirmation period to use the command again.
	 *
	 * @return false if console overrides confirmations, otherwise true
	 */
	boolean overrideConsole() default false;
}