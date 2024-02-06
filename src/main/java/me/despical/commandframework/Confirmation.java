package me.despical.commandframework;

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
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Confirmation {

	String message();

	String bypassPerm() default "";

	int expireAfter();

	TimeUnit timeUnit() default TimeUnit.SECONDS;

	boolean overrideConsole() default false;
}