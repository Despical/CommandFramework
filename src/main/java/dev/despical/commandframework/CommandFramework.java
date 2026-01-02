/*
 * Command Framework - Annotation based command framework
 * Copyright (C) 2026  Berke Akçen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.despical.commandframework;

import dev.despical.commandframework.annotations.Command;
import dev.despical.commandframework.annotations.Param;
import dev.despical.commandframework.exceptions.CommandException;
import dev.despical.commandframework.debug.DebugLogger;
import dev.despical.commandframework.options.FrameworkOption;
import dev.despical.commandframework.options.OptionManager;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Main class of the framework to register commands, add tab
 * completions and implement a function to run if there is no
 * matched commands related this framework.
 *
 * @author Despical
 * @since 1.0.0
 */
@ApiStatus.NonExtendable
public class CommandFramework extends CommandHandler {

	protected static CommandFramework instance;

	private Logger logger;
	private final Plugin plugin;
    private final OptionManager optionManager;

	public CommandFramework(@NotNull Plugin plugin) {
		this.checkRelocation();
		this.checkIsAlreadyInitialized();

		this.plugin = plugin;
        this.optionManager = new OptionManager();
		this.initializeLogger();
	}

	private void checkRelocation() {
		if (Boolean.getBoolean("commandframework.suppress.relocation")) return;

		String defaultPackage = new String(new byte[] {'d', 'e', 'v', '.', 'd', 'e', 's', 'p', 'i', 'c', 'a', 'l', '.',
			'c', 'o', 'm', 'm', 'a', 'n', 'd', 'f', 'r', 'a', 'm', 'e', 'w', 'o', 'r', 'k'});

		String examplePackage = new String(new byte[] {'y', 'o', 'u', 'r', '.', 'p', 'a', 'c', 'k', 'a', 'g', 'e'});
		String packageName = "dev.despical.commandframework";

		if (packageName.startsWith(defaultPackage) || packageName.startsWith(examplePackage)) {
			throw new IllegalStateException("Command Framework has not been relocated correctly!");
		}
	}

	private void checkIsAlreadyInitialized() {
		if (!Boolean.getBoolean("commandframework.suppress.initialization") && instance != null) {
			throw new IllegalStateException("Instance already initialized!");
		} else instance = this;
	}

	private void initializeLogger() {
		if (this.optionManager.isEnabled(FrameworkOption.DEBUG)) {
			this.logger = new DebugLogger(plugin.getLogger());
			return;
		}

		this.logger = plugin.getLogger();
	}

    /**
     * Registers commands from the specified instance's class.
     * <p>
     * This method scans the class of the provided instance and registers all commands
     * defined within that class. The class should contain methods annotated to be recognized
     * as commands.
     * </p>
     *
     * @param instance the instance of the class from which commands will be registered. Must not be {@code null}.
     */
	public final void registerCommands(@NotNull Object instance) {
		this.registry.registerCommands(instance);
	}

    /**
     * Scans all classes within the specified package, creates instances of them,
     * and registers them as commands.
     * <p>
     * Note: Scanned classes must have a public no-args constructor.
     * Interfaces and abstract classes are automatically skipped.
     * </p>
     *
     * @param packageName The full path of the package to scan (e.g., "com.example.project.commands")
     * @see #registerCommands(Object)
     */
    public final void registerAllInPackage(@NotNull String packageName) {
        this.registry.registerAllInPackage(packageName);
    }

    /**
     * Unregisters a command and its associated tab completer if they are registered with the specified name.
     *
     * @param commandName the name of the command to be unregistered. Must not be {@code null} or empty.
     * @throws IllegalArgumentException if {@code commandName} is {@code null} or an empty string.
     */
	public final void unregisterCommand(@NotNull String commandName) {
        this.registry.unregisterCommand(commandName);
	}

    /**
     * Unregisters all commands and tab completers that were registered using the instance of this object.
     */
	public final void unregisterCommands() {
        this.registry.unregisterCommands();
	}

	/**
	 * Adds a custom parameter to the parameter handler.
	 *
	 * <p>This method allows the addition of a custom parameter to the parameter handler by specifying
	 * a value and a function that converts {@link CommandArguments} to an instance of a type that
	 * extends {@link A}.
	 *
	 * @param <A>      the type of the parent class that the custom parameter's type extends
	 * @param <B>      the type of the custom parameter, which extends {@link A}
	 * @param value    the value to call custom parameter using {@linkplain Param @Param}, must not be null,
	 *                 can be a class name
	 * @param function a function that takes {@link CommandArguments} and returns an instance of {@link B},
	 *                 must not be null
	 *
	 * @throws NullPointerException if {@code value} is already added as a custom parameter
	 */
	public final <A, B extends A> void addCustomParameter(@NotNull String value, @NotNull Function<CommandArguments, B> function) {
		this.parameterHandler.addCustomParameter(value, function);
	}

    /**
     * Adds a custom parameter to the parameter handler using a class type.
     *
     * <p>This method allows the addition of a custom parameter to the parameter handler by specifying
     * a class and a function that converts {@link CommandArguments} to an instance of {@link T}.
     * <p>
     * The parameter will be registered using the {@link Class#getSimpleName()} as the key.
     *
     * @param <T>      the type of the custom parameter
     * @param clazz    the class of the custom parameter, used to derive the key using {@link Class#getSimpleName()},
     * must not be null
     * @param function a function that takes {@link CommandArguments} and returns an instance of {@link T},
     * must not be null
     *
     * @throws CommandException if a custom parameter with the same class name is already registered
     */
    public final <T> void addCustomParameter(@NotNull Class<T> clazz, @NotNull Function<CommandArguments, T> function) {
        this.parameterHandler.addCustomParameter(clazz, function);
    }

	/**
	 * Returns the logger instance of Command Framework. By default, logger is {@link #plugin}'s logger.
	 *
	 * @return the current logger instance.
	 * @since 1.4.8
	 */
	@NotNull
    @Contract(pure = true)
	public final Logger getLogger() {
		return logger;
	}

	/**
	 * Changes default logger
	 *
	 * @param logger the non-null new logger instance
	 * @since 1.4.8
	 */
	public final void setLogger(@NotNull Logger logger) {
		this.logger = logger;
	}

	/**
	 * Returns the option manager.
	 *
	 * @return the option manager.
	 * @since 1.4.8
	 */
	public final OptionManager options() {
		return this.optionManager;
	}

	/**
	 * Get an unmodifiable copy of registered commands.
	 *
	 * @return list of the commands.
	 */
	@NotNull
    @Contract(pure = true)
	public final List<Command> getCommands() {
		return List.copyOf(registry.getCommands());
	}

	/**
	 * Get an unmodifiable copy of registered sub-commands-.
	 *
	 * @return list of the sub-commands.
	 */
	@NotNull
    @Contract(pure = true)
	public final List<Command> getSubCommands() {
		return List.copyOf(registry.getSubCommands());
	}

	/**
	 * Get an unmodifiable copy of registered commands and sub-commands.
	 *
	 * @return list of the commands and sub-commands.
	 */
	@NotNull
    @Contract(pure = true)
	public final List<Command> getAllCommands() {
		final List<Command> commands = new ArrayList<>(registry.getCommands());
		commands.addAll(registry.getSubCommands());

		return List.copyOf(commands);
	}

    @NotNull
    @Contract(pure = true)
    public final Plugin getPlugin() {
        return plugin;
    }

    protected final void setCommandMap(CommandMap commandMap) {
        this.registry.setCommandMap(commandMap);
    }

    public static CommandFramework getInstance() {
		return instance;
	}
}
