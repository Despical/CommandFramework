/*
 * Command Framework - Annotation based command framework
 * Copyright (C) 2025  Berke Akçen
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

import dev.despical.commandframework.annotations.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

/**
 * Represents a registered command or completer with its execution handle.
 *
 * @param <T> The annotation type (Command or Completer)
 * @param instance The object instance containing the method
 * @param method The original reflection method (used for annotation processing)
 * @param handle The high-performance MethodHandle for execution
 * @param annotation The specific annotation instance (@Command or @Completer)
 *
 * @author Despical
 * <p>
 * Created at 30.12.2025
 */
public record RegisteredMember<T extends Annotation>(
    @Nullable Object instance,
    @Nullable Method method,
    @Nullable MethodHandle handle,
    @NotNull T annotation
) {

    public static RegisteredMember<Command> dummyCommand(Command command) {
        return new RegisteredMember<>(null , null, null, command);
    }
}
