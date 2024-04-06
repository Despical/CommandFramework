package me.despical.commandframework;

import java.lang.annotation.*;

/**
 * @author Despical
 * <p>
 * Created at 29.03.2024
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {

	String value();
}