package me.despical.commandframework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An utility class in framework to create argument completions
 * for commands.
 *
 * @author  Despical
 * @since   1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Completer {

    /**
     * The name of the command. If command would be a sub command then
     * sub command's name must be separated by dot. For example like the
     * {@code "command.subcommand"}
     *
     * @return name of the command or subcommand
     */
    String name();

    /**
     * An alternative name list of command. Check {@link #name()}
     * to understand how command names work.
     *
     * @return aliases list of the command
     */
    String[] aliases() default {};
}