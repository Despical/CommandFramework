package me.despical.commandframework.cooldown;

import me.despical.commandframework.CommandArguments;
import me.despical.commandframework.CommandFramework;
import me.despical.commandframework.Message;
import me.despical.commandframework.annotations.Command;
import me.despical.commandframework.annotations.Completer;
import me.despical.commandframework.annotations.Cooldown;
import me.despical.commandframework.exceptions.CommandException;
import me.despical.commandframework.exceptions.CooldownException;
import me.despical.commandframework.options.Option;
import me.despical.commandframework.utils.Utils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * This class handles the command cooldowns.
 *
 * <p>This is an internal class and should not be instantiated by any
 * external class.
 *
 * @author Despical
 * @since 1.4.8
 * <p>
 * Created at 18.07.2024
 *
 * @see Option#CUSTOM_COOLDOWN_CHECKER
 * @see Cooldown
 */
@ApiStatus.Internal
public final class CooldownManager {

	private final CommandFramework commandFramework;
	private final Map<CommandSender, Map<Command, Long>> cooldowns;

	public CooldownManager(CommandFramework commandFramework) {
		this.commandFramework = commandFramework;
		this.cooldowns = new HashMap<>();
	}

	public boolean hasCooldown(CommandArguments arguments) {
		if (!this.commandFramework.options().isEnabled(Option.CUSTOM_COOLDOWN_CHECKER)) {
			throw new CommandException("Custom cooldown checker option must be enabled to use CommandArguments#hasCooldown method!");
		}

		final Method method = this.getCallingMethod();

		if (method == null) return false;

		if (method.isAnnotationPresent(Completer.class)) {
			throw new CommandException("You can not use CommandArguments#hasCooldown method in a tab completer!");
		}

		final Command command = method.getAnnotation(Command.class);

		return this.hasCooldown(arguments, command, method);
	}

	public boolean hasCooldown(final CommandArguments arguments, final Command command, final Method method) {
		if (method == null) return false;
		if (!method.isAnnotationPresent(Cooldown.class)) return false;

		final Cooldown cooldown = method.getAnnotation(Cooldown.class);

		if (cooldown.value() <= 0) return false;

		final boolean isConsoleSender = arguments.isSenderConsole();
		final CommandSender sender = arguments.getSender();

		if (isConsoleSender && !cooldown.overrideConsole()) return false;
		if (!isConsoleSender && !cooldown.bypassPerm().isEmpty() && sender.hasPermission(cooldown.bypassPerm()))
			return false;

		final Map<Command, Long> cooldownMap = cooldowns.get(sender);

		if (cooldownMap == null) {
			cooldowns.put(sender, Utils.mapOf(command, System.currentTimeMillis()));
			return false;
		} else if (!cooldownMap.containsKey(command)) {
			cooldownMap.put(command, System.currentTimeMillis());

			cooldowns.replace(sender, cooldownMap);
			return false;
		}

		final long remainingSeconds = ((System.currentTimeMillis() - cooldownMap.get(command)) / 1000) % 60;
		final long cooldownInSeconds = cooldown.timeUnit().toSeconds(cooldown.value());
		final int timeBetween = (int) (cooldownInSeconds - remainingSeconds);

		if (timeBetween > 0) {
			arguments.sendMessage(Message.WAIT_BEFORE_USING_AGAIN);
			return this.handleCooldowns();
		} else {
			cooldownMap.put(command, System.currentTimeMillis());

			cooldowns.replace(sender, cooldownMap);
			return false;
		}
	}

	private boolean handleCooldowns() {
		if (!this.commandFramework.options().isEnabled(Option.CUSTOM_COOLDOWN_CHECKER)) {
			return true;
		}

		throw new CooldownException();
	}

	private Method getCallingMethod() {
		try {
			final StackTraceElement callingElement = Thread.currentThread().getStackTrace()[4];
			final String methodName = callingElement.getMethodName();
			final Class<?> callingClass = Class.forName(callingElement.getClassName());
			final Method[] methods = callingClass.getDeclaredMethods();

			for (Method method : methods) {
				if (method.getName().equals(methodName)) {
					return method;
				}
			}
		} catch (Exception exception) {
			Utils.handleExceptions(exception);
		}

		return null;
	}
}