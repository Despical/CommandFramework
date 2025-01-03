package me.despical.commandframework.options;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

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
 * @see FrameworkOption
 */
public final class OptionManager {

	private final Set<FrameworkOption> frameworkOptions;

	public OptionManager() {
		this.frameworkOptions = EnumSet.noneOf(FrameworkOption.class);
	}

	public void enableOption(FrameworkOption frameworkOption) {
		this.frameworkOptions.add(frameworkOption);
	}

	public void enableOptions(FrameworkOption... frameworkOptions) {
		this.frameworkOptions.addAll(Arrays.asList(frameworkOptions));
	}

	public boolean isEnabled(FrameworkOption frameworkOption) {
		return this.frameworkOptions.contains(frameworkOption);
	}
}
