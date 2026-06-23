/*
 * Command Framework - Annotation based command framework
 * Copyright (C) 2026  Berke Akcen
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

package dev.despical.commandframework;

import dev.despical.commandframework.annotations.Command;
import dev.despical.commandframework.utils.CommandNameValidator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable command metadata used to replace the attributes of a command that
 * was already registered by {@link CommandFramework}.
 *
 * @author Despical
 * @since 1.6.4
 */
public final class CommandAttributes {

    private final String name;
    private final String fallbackPrefix;
    private final String permission;
    private final String[] aliases;
    private final String desc;
    private final String usage;
    private final int min;
    private final int max;
    private final boolean onlyOp;
    private final boolean async;
    private final Command.SenderType senderType;

    private CommandAttributes(Builder builder) {
        this.name = normalizeCommandName(builder.name, "name");
        this.fallbackPrefix = Objects.requireNonNull(builder.fallbackPrefix, "fallbackPrefix");
        this.permission = Objects.requireNonNull(builder.permission, "permission");
        this.aliases = normalizeAliases(builder.aliases, this.name);
        this.desc = Objects.requireNonNull(builder.desc, "desc");
        this.usage = Objects.requireNonNull(builder.usage, "usage");
        this.min = builder.min;
        this.max = builder.max;
        this.onlyOp = builder.onlyOp;
        this.async = builder.async;
        this.senderType = Objects.requireNonNull(builder.senderType, "senderType");

        if (this.min < 0) {
            throw new IllegalArgumentException("Minimum argument count cannot be negative.");
        }

        if (this.max < -1) {
            throw new IllegalArgumentException("Maximum argument count must be -1 or greater.");
        }

        if (this.max != -1 && this.max < this.min) {
            throw new IllegalArgumentException("Maximum argument count cannot be lower than minimum argument count.");
        }
    }

    @NotNull
    @Contract("_ -> new")
    public static Builder builder(@NotNull Command command) {
        return new Builder(command);
    }

    @NotNull
    @Contract(pure = true)
    public String name() {
        return name;
    }

    @NotNull
    @Contract(pure = true)
    public String fallbackPrefix() {
        return fallbackPrefix;
    }

    @NotNull
    @Contract(pure = true)
    public String permission() {
        return permission;
    }

    @NotNull
    @Contract(pure = true)
    public String[] aliases() {
        return aliases.clone();
    }

    @NotNull
    @Contract(pure = true)
    public String desc() {
        return desc;
    }

    @NotNull
    @Contract(pure = true)
    public String usage() {
        return usage;
    }

    @Contract(pure = true)
    public int min() {
        return min;
    }

    @Contract(pure = true)
    public int max() {
        return max;
    }

    @Contract(pure = true)
    public boolean onlyOp() {
        return onlyOp;
    }

    @Contract(pure = true)
    public boolean async() {
        return async;
    }

    @NotNull
    @Contract(pure = true)
    public Command.SenderType senderType() {
        return senderType;
    }

    @NotNull
    @Contract(pure = true)
    public Command toCommand() {
        return new Command() {
            public String name() { return name; }
            public String fallbackPrefix() { return fallbackPrefix; }
            public String permission() { return permission; }
            public String[] aliases() { return aliases.clone(); }
            public String desc() { return desc; }
            public String usage() { return usage; }
            public int min() { return min; }
            public int max() { return max; }
            public boolean onlyOp() { return onlyOp; }
            public boolean async() { return async; }
            public SenderType senderType() { return senderType; }
            public Class<? extends Annotation> annotationType() { return Command.class; }
        };
    }

    private static String normalizeCommandName(String value, String fieldName) {
        return CommandNameValidator.normalizeName(value, fieldName);
    }

    private static String[] normalizeAliases(String[] aliases, String commandName) {
        return CommandNameValidator.normalizeAliases(aliases, commandName);
    }

    /**
     * Mutable builder used by {@link CommandFramework#updateCommandAttributes(String, java.util.function.Consumer)}.
     */
    public static final class Builder {

        private String name;
        private String fallbackPrefix;
        private String permission;
        private String[] aliases;
        private String desc;
        private String usage;
        private int min;
        private int max;
        private boolean onlyOp;
        private boolean async;
        private Command.SenderType senderType;

        private Builder(Command command) {
            this.name = command.name();
            this.fallbackPrefix = command.fallbackPrefix();
            this.permission = command.permission();
            this.aliases = command.aliases();
            this.desc = command.desc();
            this.usage = command.usage();
            this.min = command.min();
            this.max = command.max();
            this.onlyOp = command.onlyOp();
            this.async = command.async();
            this.senderType = command.senderType();
        }

        @NotNull
        @Contract("_ -> this")
        public Builder name(@NotNull String name) {
            this.name = name;
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public Builder fallbackPrefix(@NotNull String fallbackPrefix) {
            this.fallbackPrefix = fallbackPrefix;
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public Builder permission(@NotNull String permission) {
            this.permission = permission;
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public Builder aliases(@NotNull String... aliases) {
            this.aliases = Arrays.copyOf(aliases, aliases.length);
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public Builder desc(@NotNull String desc) {
            this.desc = desc;
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public Builder usage(@NotNull String usage) {
            this.usage = usage;
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public Builder min(int min) {
            this.min = min;
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public Builder max(int max) {
            this.max = max;
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public Builder onlyOp(boolean onlyOp) {
            this.onlyOp = onlyOp;
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public Builder async(boolean async) {
            this.async = async;
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public Builder senderType(@NotNull Command.SenderType senderType) {
            this.senderType = senderType;
            return this;
        }

        @NotNull
        @Contract(" -> new")
        public CommandAttributes build() {
            return new CommandAttributes(this);
        }
    }
}
