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

package me.despical.commandframework.test;

import me.despical.commandframework.annotations.Option;
import me.despical.commandframework.parser.OptionParser;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Despical
 * <p>
 * Created at 12.10.2025
 */
class OptionParserTest {

    private Map<String, List<String>> parseOptions(String methodName, String[] args) throws NoSuchMethodException {
        Method method = OptionParserTestMethods.class.getMethod(methodName);
        OptionParser parser = new OptionParser(args, method);
        return parser.parseOptions();
    }

    @Test
    void test_defaultOption_multipleValues() throws NoSuchMethodException {
        String[] args = {"--players=berke,despical"};
        Map<String, List<String>> parsedOptions = parseOptions("defaultOption", args);

        assertTrue(parsedOptions.containsKey("players"));
        List<String> values = parsedOptions.get("players");
        assertEquals(2, values.size());
        assertTrue(values.contains("berke"));
        assertTrue(values.contains("despical"));
    }

    @Test
    void test_optionNoSeparating_valuesAsSingleString() throws NoSuchMethodException {
        String[] args = {"--players=berke,despical"};
        Map<String, List<String>> parsedOptions = parseOptions("optionNoSeparating", args);

        assertTrue(parsedOptions.containsKey("players"));
        List<String> values = parsedOptions.get("players");
        assertEquals(1, values.size());
        assertEquals("berke,despical", values.getFirst());
    }

    @Test
    void test_customOption_withCustomPrefixAndKeySeparator() throws NoSuchMethodException {
        String[] args = {".teams:alpha,beta"};
        Map<String, List<String>> parsedOptions = parseOptions("customOption", args);

        assertTrue(parsedOptions.containsKey("teams"));
        List<String> values = parsedOptions.get("teams");
        assertEquals(2, values.size());
        assertTrue(values.contains("alpha"));
        assertTrue(values.contains("beta"));
    }

    public static class OptionParserTestMethods {

        @Option("players")
        public void defaultOption() {}

        @Option(value = "players", allowSeparating = false)
        public void optionNoSeparating() {}

        @Option(value = "teams", prefix = ".", keySeparator = ":")
        public void customOption() {}
    }
}
