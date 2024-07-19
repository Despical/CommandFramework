package me.despical.commandframework.confirmations;

import me.despical.commandframework.annotations.Command;
import me.despical.commandframework.annotations.Confirmation;
import me.despical.commandframework.utils.SelfExpiringHashMap;
import me.despical.commandframework.utils.SelfExpiringMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Method;

/**
 * This class handles the confirmations for commands and subcommands.
 *
 * <p>This is an internal class and should not be instantiated by any
 * external class.
 *
 * @author Despical
 * @since 1.4.8
 * <p>
 * Created at 18.07.2024
 *
 * @see me.despical.commandframework.options.Option#CONFIRMATIONS
 * @see Confirmation
 */
@ApiStatus.Internal
public final class ConfirmationManager {

	private final SelfExpiringMap<CommandSender, Command> confirmations;

	public ConfirmationManager() {
		this.confirmations = new SelfExpiringHashMap<>();
	}

	public boolean checkConfirmations(final CommandSender sender, final Command command, final Method method) {
		if (method == null) return false;
		if (!method.isAnnotationPresent(Confirmation.class)) return false;

		final Confirmation confirmation = method.getAnnotation(Confirmation.class);

		if (confirmation.expireAfter() <= 0) return false;

		final boolean isConsoleSender = sender instanceof ConsoleCommandSender;

		if (isConsoleSender && !confirmation.overrideConsole()) return false;
		if (!isConsoleSender && !confirmation.bypassPerm().isEmpty() && sender.hasPermission(confirmation.bypassPerm()))
			return false;

		if (confirmations.containsKey(sender)) {
			confirmations.remove(sender);
			return false;
		}

		confirmations.put(sender, command, confirmation.timeUnit().toMillis(confirmation.expireAfter()));

		sender.sendMessage(confirmation.message());
		return true;
	}
}