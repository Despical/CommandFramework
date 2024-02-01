/*
 * Command Framework - Annotation based command framework
 * Copyright (C) 2024  Berke Akçen
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

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import me.despical.commandframework.*;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Command registration and usage test class.
 * <p>
 *     Implemented with <a href="https://github.com/MockBukkit/MockBukkit">MockBukkit</a>
 * </p>
 * @author gamerover98
 */
class CommandRegistrationTest {

	/**
	 * The {@link org.bukkit.Server} mocked instance.
	 */
	private ServerMock server;

	/**
	 * The {@link org.bukkit.plugin.java.JavaPlugin} mocked instance.
	 */
	private MockPlugin plugin;

	@BeforeEach
	public void setUp() {
		server = MockBukkit.mock();
		plugin = MockBukkit.createMockPlugin();
	}

	/**
	 * Test to see if the {@link ExampleCommand} has been registered.
	 */
	@Test
	void testCommandRegistration() {
		CommandFramework commandFramework = createCommandFramework();
		assertEquals(3, commandFramework.getCommands().size());
	}

	/**
	 * Test to see if the {@link ExampleCommand} can be executed correctly by a {@link org.bukkit.entity.Player}.
	 */
	@Test
	void testCommandExecutionByPlayer() {
		createCommandFramework();
		PlayerMock player = server.addPlayer();

		// add as operator to prevent permission issues.
		player.setOp(true);

		// no params
		player.performCommand("example");
		player.assertSaid("§cRequired argument length is less or greater than needed!");

		// one param
		player.performCommand("example firstParam");
		player.assertSaid("This is how you can create a example command using framework.");

		// more params than maximum param amount
		player.performCommand("example firstParam secondParam thirdParam fourthParam fifthParam sixthParam");
		player.assertSaid("/example");

		// first alias
		player.performCommand("firstAlias");
		player.assertSaid("§cRequired argument length is less or greater than needed!");

		// second alias
		player.performCommand("secondAlias");
		player.assertSaid("§cRequired argument length is less or greater than needed!");

		// no command arguments
		player.performCommand("nocommandargs");

		// custom parameters
		player.performCommand("customargs test");
		player.assertSaid("First parameter is test");
	}

	@AfterEach
	public void tearDown() {
		MockBukkit.unmock();
	}

	/**
	 * @return a {@link CommandFramework} instance with an example command.
	 */
	@NotNull
	private CommandFramework createCommandFramework() {
		CommandFramework commandFramework = new CommandFrameworkMock(plugin);
		commandFramework.registerCommands(new ExampleCommand());
		commandFramework.addCustomParameter(String.class, arguments -> arguments.getArgument(0));
		return commandFramework;
	}

	/**
	 * Example command class like the
	 * <a href="https://github.com/Despical/CommandFramework/wiki/Command-examples#example-usage">wiki one</a>
	 */
	public static class ExampleCommand {

		@Command(
			name = "example",
			aliases = {"firstAlias", "secondAlias"},
			permission = "example.permission",
			desc = "Sends an example message to sender",
			usage = "/example",
			min = 1,
			max = 5,
			senderType = Command.SenderType.BOTH
		)
		public void exampleCommandMethod(CommandArguments arguments) {
			CommandSender sender = arguments.getSender();
			sender.sendMessage("This is how you can create a example command using framework.");
		}

		@Command(
			name = "nocommandargs"
		)
		@NoCommandArguments
		public void noCommandArgsTest() {
			Logger.getLogger(this.getClass().getSimpleName()).info("This command is annotated with @NoCommandArguments to run without required parameters.");
		}

		@Command(
			name = "customargs",
			min = 1
		)
		// Do not forget to annotate with @CustomParameters; otherwise, the method won't be registered.
		@CustomParameters
		public void customParamCommand(String firstParameter, CommandArguments arguments) {
			CommandSender sender = arguments.getSender();
			// Check if arguments are empty; otherwise, firstParameter will return null.
			// CommandArguments parameter can be added to anywhere in method as a parameter.
			sender.sendMessage("First parameter is " + firstParameter);
		}

		@Completer(
			name = "example",
			aliases = {"firstAlias", "secondAlias"}
		)
		public List<String> exampleCommandCompletion(CommandArguments arguments) {
			return Arrays.asList("first", "second", "third");
		}
	}
}