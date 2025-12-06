/*
 * Command Framework - Annotation based command framework
 * Copyright (C) 2025  Berke Akçen
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
import dev.despical.commandframework.confirmations.ConfirmationManager;
import dev.despical.commandframework.exceptions.CommandException;
import dev.despical.commandframework.cooldown.CooldownManager;
import dev.despical.commandframework.debug.DebugLogger;
import dev.despical.commandframework.options.FrameworkOption;
import dev.despical.commandframework.options.OptionManager;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
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
	private CooldownManager cooldownManager;
	private ConfirmationManager confirmationManager;

	protected final Plugin plugin;
	private final OptionManager optionManager;
	private final CommandRegistry registry;

	public CommandFramework(@NotNull Plugin plugin) {
		this.checkRelocation();
		this.checkIsAlreadyInitialized();

		this.plugin = plugin;
		this.registry = new CommandRegistry();
		this.optionManager = new OptionManager();
		this.initializeLogger();
		super.setRegistry(this);
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
	 * Registers commands in given object's class.
	 *
	 * @param instance the class instance of given object.
	 */
	public final void registerCommands(@NotNull Object instance) {
		this.registry.registerCommands(instance);
	}

	/**
	 * Unregisters command and tab completer if there is with the given name.
	 *
	 * @param commandName name of the command that's going to be removed
	 */
	public final void unregisterCommand(@NotNull String commandName) {
		this.registry.unregisterCommand(commandName);
	}

	/**
	 * Unregisters all of registered commands and tab completers using that instance.
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
	 * Returns the logger instance of Command Framework. By default, logger is {@link #plugin} 's logger.
	 *
	 * @return the current logger instance.
	 * @since 1.4.8
	 */
	@NotNull
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

	@ApiStatus.Internal
	CooldownManager getCooldownManager() {
		if (this.cooldownManager == null)
			this.cooldownManager = new CooldownManager(this);
		return cooldownManager;
	}

	@ApiStatus.Internal
	CommandRegistry getRegistry() {
		return registry;
	}

	@ApiStatus.Internal
	boolean checkConfirmation(CommandSender sender, final Command command, final Method method) {
		if (!this.optionManager.isEnabled(FrameworkOption.CONFIRMATIONS)) {
			return false;
		}

		if (this.confirmationManager == null)
			this.confirmationManager = new ConfirmationManager();
		return confirmationManager.checkConfirmations(sender, command, method);
	}

	protected final void setCommandMap(CommandMap commandMap) {
		this.registry.setCommandMap(commandMap);
	}

	/**
	 * Get a copy of registered commands.
	 *
	 * @return list of the commands.
	 */
	@NotNull
	public final List<Command> getCommands() {
		return new ArrayList<>(this.registry.getCommands());
	}

	/**
	 * Get a copy of registered sub-commands-.
	 *
	 * @return list of the sub-commands.
	 */
	@NotNull
	public final List<Command> getSubCommands() {
		return new ArrayList<>(this.registry.getSubCommands());
	}

	/**
	 * Get a copy of registered commands and sub-commands.
	 *
	 * @return list of the commands and sub-commands.
	 */
	@NotNull
	public final List<Command> getAllCommands() {
		final List<Command> commands = new ArrayList<>(this.registry.getCommands());
		commands.addAll(this.registry.getSubCommands());

		return commands;
	}

	public static CommandFramework getInstance() {
		return instance;
	}
}
