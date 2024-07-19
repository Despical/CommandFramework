package me.despical.commandframework.options;

import org.jetbrains.annotations.ApiStatus;

import java.util.*;

/**
 * @author Despical
 * <p>
 * Created at 18.07.2024
 */
@ApiStatus.Obsolete
public class OptionManager {

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