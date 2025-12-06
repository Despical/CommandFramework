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

package dev.despical.commandframework.test;

import dev.despical.commandframework.annotations.Flag;
import dev.despical.commandframework.parser.OptionParser;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Despical
 * <p>
 * Created at 12.10.2025
 */
class FlagParserTest {

    private Set<String> parseFlagsForMethod(String methodName, String[] args) throws NoSuchMethodException {
        Method method = FlagParserTestMethods.class.getMethod(methodName);
        OptionParser parser = new OptionParser(args, method);
        return parser.parseFlags();
    }

    /**
     * Flag(value = "verbose", prefix = "--")
     */
    @Test
    void test_flags_with_default_prefix() throws NoSuchMethodException {
        String[] args = {"--", "--verbose", "-nogui", "is-that-even-a-flag"};
        Set<String> parsedFlags = parseFlagsForMethod("flagMethodWithDefaultPrefix", args);

        assertEquals(1, parsedFlags.size());
        assertTrue(parsedFlags.contains("verbose"));
    }

    /**
     * Flag(value = {"verbose", "nogui"}, prefix = ".")
     */
    @Test
    void test_flags_with_custom_prefix() throws NoSuchMethodException {
        String[] args = {".", ".verbose", ".nogui", ".noflag"};
        Set<String> parsedFlags = parseFlagsForMethod("flagMethodWithCustomPrefix", args);

        assertEquals(2, parsedFlags.size());
        assertFalse(parsedFlags.contains("noflag"));
        assertTrue(parsedFlags.contains("verbose"));
        assertTrue(parsedFlags.contains("nogui"));
    }

    public static class FlagParserTestMethods {

        @Flag(value = "verbose")
        public void flagMethodWithDefaultPrefix() {}

        @Flag(value = {"verbose", "nogui"}, prefix = ".")
        public void flagMethodWithCustomPrefix() {}
    }
}
