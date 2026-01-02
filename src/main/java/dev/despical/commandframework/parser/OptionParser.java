/*
 * Command Framework - Annotation based command framework
 * Copyright (C) 2026  Berke Akçen
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

package dev.despical.commandframework.parser;

import dev.despical.commandframework.annotations.Flag;
import dev.despical.commandframework.annotations.Option;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Despical
 * <p>
 * Created at 20.09.2024
 */
@ApiStatus.Internal
public final class OptionParser {

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

			if (option.allowSeparating() && !argument.contains(option.keySeparator())) {
				continue;
			}

			String[] options = argument.substring(prefix.length()).split(keySeparator);

			if (!option.allowSeparating()) {
				if (options.length < 2) {
					continue;
				}

				String value = options[1];
				this.parsedOptions.put(option.value(), Collections.singletonList(value));

				iterator.remove();
				continue;
			}

			String[] values = options[1].split(valueSeparator);
			this.parsedOptions.put(option.value(), Arrays.asList(values));

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
