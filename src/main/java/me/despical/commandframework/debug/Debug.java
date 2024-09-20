package me.despical.commandframework.debug;

import java.lang.annotation.*;

/**
 * @author Despical
 * <p>
 * Created at 8.09.2024
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Debug {
}