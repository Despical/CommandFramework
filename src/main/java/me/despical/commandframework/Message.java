package me.despical.commandframework;

import me.despical.commandframework.annotations.Command;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author Despical
 * <p>
 * Created at 18.07.2024
 */
public enum Message {

	SHORT_ARG_SIZE("&cRequired argument length is less than needed!", true),
	LONG_ARG_SIZE("&cRequired argument length greater than needed!", true),
	ONLY_BY_PLAYERS("&cThis command is only executable by players!"),
	ONLY_BY_CONSOLE("&cThis command is only executable by console!"),
	NO_PERMISSION("&cYou don't have enough permission to execute this command!"),
	MUST_HAVE_OP("&cYou must have OP to execute this command!"),
	WAIT_BEFORE_USING_AGAIN("&cYou have to wait before using this command again!");

	private BiFunction<Command, CommandArguments, Boolean> message;

	Message(String message) {
		this(message, true);
	}

	Message(String message, boolean sendUsage) {
		this.message = (command, arguments) -> {
			if (sendUsage && !MessageHelper.SEND_USAGE.apply(command, arguments)) return true;

			arguments.sendMessage(message);
			return true;
		};
	}

	/**
	 * Function to apply messages that will be sent using CommandArguments#sendMessage method.
	 */
	@NotNull
	private static Function<String, String> colorFormatter = (string) -> ChatColor.translateAlternateColorCodes('&', string);

	/**
	 * For instance, can be used to translate Minecraft color and Hex color codes.
	 *
	 * @param colorFormatter the function that will be applied to the strings to colorize
	 */
	public static void setColorFormatter(@NotNull Function<String, String> colorFormatter) {
		Message.colorFormatter = colorFormatter;
	}

	/**
	 * Set a custom error message.
	 *
	 * @param message the custom error message.
	 */
	public void setMessage(final BiFunction<Command, CommandArguments, Boolean> message) {
		this.message = message;
	}

	@ApiStatus.Internal
	static String applyColorFormatter(final @NotNull String string) {
		return colorFormatter.apply(string);
	}

	@ApiStatus.Internal
	boolean sendMessage(final Command command, final CommandArguments arguments) {
		return this.message.apply(command, arguments);
	}

	private static class MessageHelper {

		private static final BiFunction<Command, CommandArguments, Boolean> SEND_USAGE = (command, arguments) -> {
			final String usage = command.usage();

			if (!usage.isEmpty()) {
				arguments.sendMessage(usage);
				return false;
			}

			return true;
		};
	}
}