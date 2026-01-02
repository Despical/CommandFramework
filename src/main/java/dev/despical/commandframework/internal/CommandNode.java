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

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 30.12.2025
 */
public final class CommandNode<T extends Annotation> {

    private RegisteredMember<T> member;

    private final Map<String, CommandNode<T>> children = new HashMap<>();

    public RegisteredMember<T> getMember() {
        return member;
    }

    public void setMember(RegisteredMember<T> member) {
        this.member = member;
    }

    public Map<String, CommandNode<T>> getChildren() {
        return children;
    }
}
