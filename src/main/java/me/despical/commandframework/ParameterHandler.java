package me.despical.commandframework;

import com.google.common.primitives.Primitives;
import me.despical.commandframework.annotations.Default;
import me.despical.commandframework.annotations.Param;
import me.despical.commandframework.exceptions.CommandException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Despical
 * <p>
 * Created at 18.07.2024
 */
@ApiStatus.Internal
public final class ParameterHandler {

	@NotNull
	private final Map<String, Function<CommandArguments, ?>> customParametersMap;

	public ParameterHandler() {
		this.customParametersMap = new HashMap<>();
	}

	public <A, B extends A> void addCustomParameter(@NotNull String value, @NotNull Function<CommandArguments, B> function) {
		if (this.customParametersMap.containsKey(value))
			throw new CommandException("Custom parameter function called ''{0}'' is already registered!", value);
		this.customParametersMap.put(value, function);
	}

	@NotNull
	public Object[] getParameterArray(Method method, CommandArguments commandArguments) throws Exception {
		final Parameter[] parameters = method.getParameters();
		final Object[] methodParameters = new Object[parameters.length];

		outer_loop:
		for (int i = 0; i < parameters.length; i++) {
			final String simpleName = parameters[i].getType().getSimpleName();

			if ("CommandArguments".equals(simpleName)) {
				methodParameters[i] = commandArguments;
				continue;
			}

			for (Annotation annotation : parameters[i].getAnnotations()) {
				if (annotation instanceof Param) {
					String value = ((Param) annotation).value();

					if (!customParametersMap.containsKey(value)) {
						throw new CommandException("Custom parameter (type: {0}, value: {1}) is requested but return function is not found!", simpleName, value);
					}

					methodParameters[i] = customParametersMap.get(value).apply(commandArguments);

					if (methodParameters[i] == null) {
						if (!parameters[i].isAnnotationPresent(Default.class)) {
							continue outer_loop;
						}

						String defaultValue = parameters[i].getAnnotation(Default.class).value();

						if (!parameters[i].getType().isInstance(String.class)) {
							Class<?> clazz = parameters[i].getType();

							if (!Primitives.isWrapperType(clazz)) {
								try {
									methodParameters[i] = clazz.getMethod("valueOf", String.class).invoke(null, defaultValue);
								} catch (Exception exception) {
									throw new CommandException("Static method {0}#valueOf(String) does not exist!", clazz.getSimpleName());
								}

								continue outer_loop;
							}

							methodParameters[i] = Primitives.wrap(clazz).getMethod("valueOf", String.class).invoke(null, defaultValue);
							continue outer_loop;
						}

						methodParameters[i] = defaultValue;
					}

					continue outer_loop;
				}
			}

			if (!customParametersMap.containsKey(simpleName)) {
				throw new CommandException("Custom parameter (type: {0}) is requested but return function is not found!", simpleName);
			}

			methodParameters[i] = customParametersMap.get(simpleName).apply(commandArguments);
		}

		return methodParameters;
	}
}