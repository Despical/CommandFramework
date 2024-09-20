package me.despical.commandframework.parser;

import me.despical.commandframework.annotations.Flag;
import me.despical.commandframework.annotations.Option;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Despical
 * <p>
 * Created at 20.09.2024
 */
public class OptionParser {

	private final Flag[] flags;
	private final Option[] options;
	private final Set<String> arguments;
	private final Set<String> parsedFlags;
	private final Map<String, List<String>> parsedOptions;

	public OptionParser(String[] arguments, Method method) {
		this.flags = method.getAnnotationsByType(Flag.class);
		this.options = method.getAnnotationsByType(Option.class);
		this.arguments = new HashSet<>(Arrays.asList(arguments));
		this.parsedFlags = new HashSet<>();
		this.parsedOptions = new HashMap<>();
	}

	public Map<String, List<String>> parseOptions() {
		for (Option option : options) {
			this.parseOption(option);
		}

		return this.parsedOptions;
	}

	public Set<String> parseFlags() {
		for (Flag flag : flags) {
			this.parseFlag(flag);
		}

		return this.parsedFlags;
	}

	private void parseOption(Option option) {
		String prefix = option.prefix();
		String keySeparator = Pattern.quote(option.keySeparator());
		String valueSeparator = Pattern.quote(option.valueSeparator());
		Iterator<String> iterator = arguments.iterator();

		while (iterator.hasNext()) {
			String argument = iterator.next();

			if (!argument.startsWith(prefix)) {
				continue;
			}

			if (option.allowSeparating() && !argument.contains(keySeparator)) {
				continue;
			}

			String[] options = argument.substring(prefix.length()).split(keySeparator);

			if (!option.allowSeparating()) {
				String value = options.length <= 1 ? "" : options[1];
				this.parsedOptions.put(option.name(), Collections.singletonList(value));

				iterator.remove();
				continue;
			}

			String[] values = options[1].split(valueSeparator);
			this.parsedOptions.put(option.name(), Arrays.asList(values));

			iterator.remove();
		}
	}

	private void parseFlag(Flag flag) {
		String prefix = flag.prefix();

		outer:
		for (String argument : this.arguments) {
			if (!argument.startsWith(prefix)) {
				continue;
			}

			String foundFlag = argument.substring(prefix.length());

			for (String flagName : flag.value()) {
				if (!flagName.equals(foundFlag)) {
					continue;
				}

				this.parsedFlags.add(flagName);
				continue outer;
			}
		}
	}
}