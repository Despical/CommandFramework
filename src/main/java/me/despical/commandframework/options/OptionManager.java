package me.despical.commandframework.options;

import org.jetbrains.annotations.ApiStatus;

import java.util.*;

/**
 * This class handles the options related Command Framework.
 *
 * <p>This is an internal class and should not be instantiated by any
 * external class.
 *
 * @author Despical
 * @since 1.4.8
 * <p>
 * Created at 18.07.2024
 *
 * @see Option
 */
@ApiStatus.Internal
public final class OptionManager {

	private final Set<Option> options;

	public OptionManager() {
		this.options = EnumSet.noneOf(Option.class);
	}

	public void enableOption(Option option) {
		this.options.add(option);
	}

	public void enableOptions(Option option, Option... options) {
		this.options.add(option);

		Collections.addAll(this.options, options);
	}

	public boolean isEnabled(Option option) {
		return this.options.contains(option);
	}
}