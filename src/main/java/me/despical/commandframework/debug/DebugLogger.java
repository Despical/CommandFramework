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

package me.despical.commandframework.debug;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author Despical
 * <p>
 * Created at 9.09.2024
 */
public class DebugLogger extends Logger {

    public DebugLogger(Logger parent) {
        super("CF Debug", null);
        this.setParent(parent);
        this.setLevel(Level.ALL);
    }

    @Override
    public void log(LogRecord record) {
        record.setMessage("[CF Debug] " + record.getMessage());
        super.log(record);
    }
}
