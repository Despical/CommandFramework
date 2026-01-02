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

package dev.despical.commandframework.internal;

import com.google.common.primitives.Primitives;
import dev.despical.commandframework.CommandArguments;
import dev.despical.commandframework.CompleterHelper;
import dev.despical.commandframework.annotations.Default;
import dev.despical.commandframework.annotations.Param;
import dev.despical.commandframework.exceptions.CommandException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

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
        this.customParametersMap.put(CompleterHelper.class.getSimpleName(), CompleterHelper::new);
    }

    public <A, B extends A> void addCustomParameter(@NotNull String key, @NotNull Function<CommandArguments, B> function) {
        if (this.customParametersMap.containsKey(key)) {
            throw new CommandException("Cannot register custom parameter provider for ''{0}'' because it is already registered!", key);
        }

        this.customParametersMap.put(key, function);
    }

    public <T> void addCustomParameter(@NotNull Class<T> clazz, @NotNull Function<CommandArguments, T> function) {
        this.addCustomParameter(clazz.getSimpleName(), function);
    }

    @NotNull
    public Object[] getParameterArray(Method method, CommandArguments commandArguments) throws Exception {
        final Parameter[] parameters = method.getParameters();
        final Object[] methodParameters = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            Class<?> paramType = param.getType();

            if (CommandArguments.class.isAssignableFrom(paramType)) {
                methodParameters[i] = commandArguments;
                continue;
            }

            Param paramAnnotation = param.getAnnotation(Param.class);

            String simpleName = paramType.getSimpleName();
            String key = getKey(method, paramAnnotation, simpleName);

            Object value = customParametersMap.get(key).apply(commandArguments);

            if (value == null && param.isAnnotationPresent(Default.class)) {
                String defaultValue = param.getAnnotation(Default.class).value();
                value = parseDefaultValue(paramType, defaultValue);
            }

            if (value == null && paramType.isPrimitive()) {
                throw new CommandException(
                    "Primitive parameter ''{0}'' (type: {1}) in method ''{2}'' cannot be null! usage: Use a wrapper class (e.g. Integer) or ensure the provider returns a value.",
                    key, simpleName, method.getName()
                );
            }

            methodParameters[i] = value;
        }

        return methodParameters;
    }

    private String getKey(Method method, Param paramAnnotation, String simpleName) {
        String key = (paramAnnotation != null) ? paramAnnotation.value() : simpleName;

        if (!customParametersMap.containsKey(key)) {
            String methodName = "%s#%s".formatted(method.getDeclaringClass().getSimpleName(), method.getName());

            if (paramAnnotation != null) {
                throw new CommandException(
                    "No parameter provider found for @Param(''{0}'') in method ''{1}''. Requested Type: {2}. Did you forget to register it?",
                    key, methodName, simpleName
                );
            }

            throw new CommandException(
                "No parameter provider found for type ''{0}'' in method ''{1}''. Did you forget to register it using CommandFramework#addCustomParameter?",
                simpleName, methodName
            );
        }
        return key;
    }

    private Object parseDefaultValue(Class<?> type, String value) throws Exception {
        if (type == String.class) {
            return value;
        }

        Class<?> actualType = Primitives.isWrapperType(type) ? type : Primitives.wrap(type);

        try {
            return actualType.getMethod("valueOf", String.class).invoke(null, value);
        } catch (NoSuchMethodException exception) {
            throw new CommandException("Type ''{0}'' does not support default values (missing static valueOf(String) method).", type.getSimpleName());
        }
    }
}
