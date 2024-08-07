/*
 * Command Framework - Annotation based command framework
 * Copyright (C) 2024  Berke Akçen
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

package me.despical.commandframework;

import me.despical.commandframework.annotations.Command;
import me.despical.commandframework.confirmations.ConfirmationManager;
import me.despical.commandframework.cooldown.CooldownManager;
import me.despical.commandframework.options.Option;
import me.despical.commandframework.options.OptionManager;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.*;
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
	private final ParameterHandler parameterHandler;
	private final CommandRegistry registry;

	public CommandFramework(@NotNull Plugin plugin) {
		this.checkRelocation();
		this.checkIsAlreadyInitialized();

		this.plugin = plugin;
		this.logger = plugin.getLogger();
		this.optionManager = new OptionManager();
		this.registry = new CommandRegistry(this);
		this.parameterHandler = new ParameterHandler();
		super.setRegistry(this);
	}

	private void checkRelocation() {
		String suppressRelocation = System.getProperty("commandframework.suppressrelocation");

		if ("true".equals(suppressRelocation)) return;

		String defaultPackage = new String(new byte[] {'m', 'e', '.', 'd', 'e', 's', 'p', 'i', 'c', 'a', 'l', '.',
			'c', 'o', 'm', 'm', 'a', 'n', 'd', 'f', 'r', 'a', 'm', 'e', 'w', 'o', 'r', 'k'});

		String examplePackage = new String(new byte[] {'y', 'o', 'u', 'r', '.', 'p', 'a', 'c', 'k', 'a', 'g', 'e'});
		String packageName = "me.despical.commandframework";

		if (packageName.startsWith(defaultPackage) || packageName.startsWith(examplePackage)) {
			throw new IllegalStateException("Command Framework has not been relocated correctly!");
		}
	}

	private void checkIsAlreadyInitialized() {
		String suppressRelocation = System.getProperty("commandframework.suppress_initialization");

		if (!"true".equals(suppressRelocation) && instance != null) {
			throw new IllegalStateException("Instance already initialized!");
		} else instance = this;
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
	 * @param value    the value to call custom parameter using {@linkplain me.despical.commandframework.annotations.Param @Param}, must not be null,
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
	 * Returns the logger instance of Command Framework. By default, logger is {@code plugin}'s logger.
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
	 * Enables the specified option.
	 *
	 * @param  option the {@link Option} to be enabled. Must not be {@code null}.
	 * @throws IllegalArgumentException if the {@code option} is {@code null}.
	 * @since 1.4.8
	 */
	public final void enableOption(Option option) {
		this.optionManager.enableOption(option);
	}

	/**
	 * Enables the specified options.
	 *
	 * @param option  the {@link Option} to be enabled. Must not be {@code null}.
	 * @param options the array of {@link Option} to be enabled. Must not be {@code null}.
	 * @throws IllegalArgumentException if the {@code option} or {@code options} are {@code null}.
	 * @since 1.4.8
	 */
	public final void enableOptions(Option option, Option... options) {
		this.optionManager.enableOptions(option, options);
	}

	/**
	 * Checks whether the specified {@link Option} is enabled.
	 *
	 * @param option the {@link Option} to check.
	 * @return {@code true} if the option is enabled; {@code false} otherwise.
	 * @throws IllegalArgumentException if {@code option} is {@code null}.
	 * @since 1.4.8
	 */
	public final boolean isOptionEnabled(Option option) {
		return this.optionManager.isEnabled(option);
	}

	@ApiStatus.Internal
	final CooldownManager getCooldownManager() {
		if (this.cooldownManager == null)
			this.cooldownManager = new CooldownManager(this);
		return cooldownManager;
	}

	@ApiStatus.Internal
	final CommandRegistry getRegistry() {
		return registry;
	}

	@ApiStatus.Internal
	final ParameterHandler getParameterHandler() {
		return parameterHandler;
	}

	@ApiStatus.Internal
	final boolean checkConfirmation(CommandSender sender, final Command command, final Method method) {
		if (!isOptionEnabled(Option.CONFIRMATIONS)) return false;

		if (this.confirmationManager == null)
			this.confirmationManager = new ConfirmationManager();
		return confirmationManager.checkConfirmations(sender, command, method);
	}

	protected final void setCommandMap(CommandMap commandMap) {
		this.registry.setCommandMap(commandMap);
	}

	/**
	 * Get a copy of registered commands and sub-commands.
	 *
	 * @return list of the commands and sub-commands.
	 */
	@NotNull
	@Contract(pure = true)
	public final List<Command> getCommands() {
		List<Command> commands = new ArrayList<>(this.registry.getCommands());
		commands.addAll(this.registry.getCommands());

		return commands;
	}

	/**
	 * Get a copy of registered sub-commands-.
	 *
	 * @return list of the sub-commands.
	 */
	@NotNull
	@Contract(pure = true)
	public final List<Command> getSubCommands() {
		return new ArrayList<>(this.registry.getSubCommands());
	}
}