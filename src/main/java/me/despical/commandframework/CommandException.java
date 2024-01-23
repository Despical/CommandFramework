package me.despical.commandframework;

/**
 * @author Despical
 * <p>
 * Created at 23.01.2024
 */
public class CommandException extends RuntimeException {

	public CommandException(final String message) {
		super(message);
	}

	public CommandException(final String message, final Object... params) {
		super(String.format(message, params));
	}
}