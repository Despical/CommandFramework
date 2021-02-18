package me.despical.commandframework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Main class of the framework to create commands in easy way.
 *
 * @author  Despical
 * @since   1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

    /**
     * The name of the command. If command would be a sub command then
     * sub command's name must be separated by dot. For example like the
     * {@code "command.subcommand"}
     *
     * @return name of the command or subcommand
     */
    String name();

    /**
     * The permission that sender has to have to execute command.
     *
     * @return name of the permission
     */
    String permission() default "";

    /**
     * An alternative name list of command. Check {@link #name()}
     * to understand how command names work.
     *
     * @return aliases list of the command
     */
    String[] aliases() default {};

    /**
     * The description of the command that will be showed when sender executes
     * Bukkit's help command.
     *
     * @return description of the command
     */
    String desc() default "";

    /**
     * The usage of the command that will be showed when sender executes
     * command without required or missing arguments.
     *
     * @return usage of the command.
     */
    String usage() default "";

    /**
     * Minimum value of arguments.
     *
     * @return minimum value of arguments.
     */
    int min() default 0;

    /**
     * Maximum value of arguments. -1 for infinite.
     *
     * @return maximum value of arguments.
     */
    int max() default -1;

    /**
     * The time between using command again. Use egative
     * integers for infinite.
     *
     * @return value of time between using command again.
     */
    int cooldown() default -1;

    /**
     * Enum value of command sender type to define who will
     * use the command.
     *
     * @return enum value of {@link SenderType}
     */
    SenderType senderType() default SenderType.BOTH;

    enum SenderType {
        BOTH, CONSOLE, PLAYER
    }
}