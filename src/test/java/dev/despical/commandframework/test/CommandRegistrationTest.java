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

package dev.despical.commandframework.test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import dev.despical.commandframework.CommandErrorMessage;
import dev.despical.commandframework.CommandArguments;
import dev.despical.commandframework.CommandFramework;
import dev.despical.commandframework.annotations.*;
import dev.despical.commandframework.exceptions.CommandException;
import dev.despical.commandframework.internal.MessageHelper;
import dev.despical.commandframework.options.FrameworkOption;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Command registration and usage test class.
 * <p>
 * Implemented with <a href="https://github.com/MockBukkit/MockBukkit">MockBukkit</a>
 * </p>
 *
 * @author gamerover98
 * @author Despical
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

	@BeforeAll
	static void beforeAll() {
		System.setProperty("commandframework.suppress.relocation", "true");
		System.setProperty("commandframework.suppress.initialization", "true");
	}

	@BeforeEach
	void setUp() {
		server = MockBukkit.mock();
		plugin = MockBukkit.createMockPlugin("CommandFramework");
	}

	/**
	 * Test to see if the {@link ExampleCommand} has been registered.
	 */
	@Test
	void testCommandRegistration() {
		CommandFramework commandFramework = createCommandFramework();
		assertEquals(8, commandFramework.getCommands().size());
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
		player.assertSaid("/example");

		// one param
		player.performCommand("example firstParam");
		player.assertSaid("This is how you can create a example command using framework.");

		// more params than maximum param amount
		player.performCommand("example firstParam secondParam thirdParam fourthParam fifthParam sixthParam");
		player.assertSaid("/example");

		// first alias
		player.performCommand("firstAlias");
		player.assertSaid("/example");

		// second alias
		player.performCommand("secondAlias");
		player.assertSaid("/example");

		// no command arguments
		player.performCommand("nocommandargs");
		server.getConsoleSender().assertSaid("This command is running without any parameters.");

		// custom parameters
		player.performCommand("customargs test");
		player.assertSaid("First parameter is test.");

		// cooldown command
		player.performCommand("cooldown");
		player.assertSaid("Cooldown command message.");

		player.performCommand("cooldown");
		player.assertSaid("§cYou have to wait before using this command again!");

		player.performCommand("flag");
		player.assertSaid("Flag Present: false");

		player.performCommand("flag --test");
		player.assertSaid("Flag Present: true");

		player.performCommand("option --players=mrdespi,Despical");
		player.assertSaid("Parsed Options: mrdespi, Despical");
	}

	@Test
	void testRegisteredCommandAttributesCanBeUpdated() {
		CommandFramework commandFramework = createCommandFramework();
		PlayerMock player = server.addPlayer();
		player.setOp(true);

		assertTrue(commandFramework.updateCommandAttributes("example", attributes -> attributes
			.desc("Updated description")
			.usage("/updated-example")
			.min(0)
			.max(1)
		));

		PluginCommand bukkitCommand = Bukkit.getPluginCommand("example");
		assertEquals("Updated description", bukkitCommand.getDescription());
		assertEquals("/updated-example", bukkitCommand.getUsage());

		player.performCommand("example");
		player.assertSaid("This is how you can create a example command using framework.");

		player.performCommand("example firstParam secondParam");
		player.assertSaid("/updated-example");
	}

	@Test
	void testRegisteredCommandCanBeRenamedWithoutTouchingOtherCommands() {
		CommandFramework commandFramework = createCommandFramework();
		PlayerMock player = server.addPlayer();
		player.setOp(true);

		assertTrue(commandFramework.updateCommandAttributes("example", attributes -> attributes
			.name("renamed")
			.usage("/renamed")
			.min(0)
		));

		assertEquals("renamed", commandFramework.getCommands().stream()
			.filter(command -> command.desc().equals("Sends an example message to sender"))
			.filter(command -> command.aliases().length == 2)
			.findFirst()
			.orElseThrow()
			.name());

		assertFalse(commandFramework.updateCommandAttributes("minecraft:help", attributes -> attributes.usage("/nope")));

		player.performCommand("renamed");
		player.assertSaid("This is how you can create a example command using framework.");
	}

	@Test
	void testCommandAttributeUpdateRejectsInvalidArgumentBounds() {
		CommandFramework commandFramework = createCommandFramework();

		assertThrows(IllegalArgumentException.class, () ->
			commandFramework.updateCommandAttributes("example", attributes -> attributes.min(4).max(2))
		);

		assertThrows(CommandException.class, () ->
			commandFramework.updateCommandAttributes("example", attributes -> attributes.name("cooldown"))
		);
	}

	@Test
	void testDefaultCommandArgumentsCanBeOverridden() {
		CommandFramework commandFramework = new CommandFrameworkMock(plugin);
		commandFramework.setDefaultArguments(ExampleArguments.class);
		commandFramework.registerCommands(new ArgumentsCommand());

		PlayerMock player = server.addPlayer();
		player.performCommand("arguments");
		player.assertSaid("Custom arguments label: arguments");
	}

	@Test
	void testInvalidCommandMetadataIsRejectedDuringRegistration() {
		CommandFramework commandFramework = new CommandFrameworkMock(plugin);

		assertThrows(CommandException.class, () -> commandFramework.registerCommands(new InvalidCommandMetadata()));
		assertThrows(CommandException.class, () -> commandFramework.registerCommands(new InvalidCompleterMetadata()));
	}

	@Test
	void testMissingSubcommandShowsHelpfulMessageByDefault() {
		CommandFramework commandFramework = new CommandFrameworkMock(plugin);
		commandFramework.registerCommands(new NestedCommand());

		PlayerMock player = server.addPlayer();
		assertTrue(player.performCommand("ap debug"));
		assertEquals("§cThis command cannot be used directly.", player.nextMessage());
		player.assertNoMoreSaid();
	}

	@Test
	void testMissingSubcommandAvailableCommandsIgnoreEmptyUsage() {
		CommandFramework commandFramework = new CommandFrameworkMock(plugin);
		commandFramework.registerCommands(new NestedCommandWithUsage());

		PlayerMock player = server.addPlayer();
		assertTrue(player.performCommand("ss debug"));
		assertEquals("§cThis command cannot be used directly. Try /ss debug <component | test>", player.nextMessage());
		player.assertNoMoreSaid();
	}

	@Test
	void testCustomUnknownSubcommandHandlerStillSeesAllChildren() {
		CommandFramework commandFramework = new CommandFrameworkMock(plugin);
		commandFramework.registerCommands(new NestedCommandWithUsage());

		CommandErrorMessage.UNKNOWN_SUBCOMMAND.setHandler((command, arguments) -> {
			arguments.sendMessage(
				String.join(
					" | ",
					MessageHelper.getDirectSubcommands(command).stream()
						.map(MessageHelper::getSubcommandName)
						.toList()
				)
			);
			return true;
		});

		try {
			PlayerMock player = server.addPlayer();
			assertTrue(player.performCommand("ss debug"));
			assertEquals("component | hidden | test", player.nextMessage());
			player.assertNoMoreSaid();
		} finally {
			CommandErrorMessage.UNKNOWN_SUBCOMMAND.resetHandler();
		}
	}

	@Test
	void testMissingSubcommandMessageCanBeCustomized() {
		CommandFramework commandFramework = new CommandFrameworkMock(plugin);
		commandFramework.registerCommands(new NestedCommand());

		CommandErrorMessage.UNKNOWN_SUBCOMMAND.setHandler((command, arguments) -> {
			String path = MessageHelper.getCommandPath(command, arguments);
			String subcommands = String.join(
				" / ",
				MessageHelper.getDirectSubcommands(command).stream()
					.map(MessageHelper::getSubcommandName)
					.toList()
			);

			arguments.sendMessage(
				"Use /{0} <{1}>",
				path,
				subcommands
			);
			return true;
		});

		try {
			PlayerMock player = server.addPlayer();
			assertTrue(player.performCommand("ap debug"));
			assertEquals("Use /ap debug <component / test>", player.nextMessage());
			player.assertNoMoreSaid();
		} finally {
			CommandErrorMessage.UNKNOWN_SUBCOMMAND.resetHandler();
		}
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
		commandFramework.addCustomParameter(String.class, arguments -> arguments.getArgument(0));
		commandFramework.options().enableOption(FrameworkOption.CUSTOM_COOLDOWN_CHECKER);
		commandFramework.registerCommands(new ExampleCommand());
		return commandFramework;
	}

	/**
	 * Example command class like the
	 * <a href="https://github.com/Despical/CommandFramework/wiki/Command-examples#example-usage">wiki one</a>
	 */
	public class ExampleCommand {

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
		public void noCommandArgsTest() {
			server.getConsoleSender().sendMessage("This command is running without any parameters.");
		}

		@Command(
			name = "customargs",
			min = 1
		)
		public void customParamCommand(String firstParameter, CommandArguments arguments) {
			arguments.sendMessage("First parameter is " + firstParameter + ".");
		}

		@Command(
			name = "cooldown"
		)
		@Cooldown(
			value = 5
		)
		public void cooldownTest(CommandArguments arguments) {
			arguments.checkCooldown();
			arguments.sendMessage("Cooldown command message.");
		}

		@Flag(
			value = "test",
			prefix = "--"
		)
		@Command(
			name = "flag"
		)
		public void flagTest(CommandArguments arguments) {
			arguments.sendMessage("Flag Present: " + arguments.isFlagPresent("test"));
		}

		@Option(
			value = "players",
			prefix = "--"
		)
		@Command(
			name = "option"
		)
		public void optionTest(CommandArguments arguments) {
			arguments.sendMessage("Parsed Options: " + String.join(", ", arguments.getOption("players")));
		}

		@Completer(
			name = "example",
			aliases = {"firstAlias", "secondAlias"}
		)
		public List<String> exampleCommandCompletion() {
			return List.of("first", "second", "third");
		}
	}

	public static class ExampleArguments extends CommandArguments {

		public ExampleArguments(CommandArguments arguments) {
			super(arguments);
		}

		public String customLabel() {
			return getLabel();
		}
	}

	public static class ArgumentsCommand {

		@Command(
			name = "arguments"
		)
		public void argumentsCommand(ExampleArguments arguments) {
			arguments.sendMessage("Custom arguments label: " + arguments.customLabel());
		}
	}

	public static class InvalidCommandMetadata {

		@Command(
			name = "arena..create"
		)
		public void invalidCommand(CommandArguments arguments) {
			arguments.sendMessage("invalid");
		}
	}

	public static class InvalidCompleterMetadata {

		@Completer(
			name = ".arena"
		)
		public List<String> invalidCompleter() {
			return List.of("invalid");
		}
	}

	public static class NestedCommand {

		@Command(
			name = "ap.debug.test"
		)
		public void nestedCommand(CommandArguments arguments) {
			arguments.sendMessage("nested");
		}

		@Command(
			name = "ap.debug.component"
		)
		public void nestedComponentCommand(CommandArguments arguments) {
			arguments.sendMessage("component");
		}
	}

	public static class NestedCommandWithUsage {

		@Command(
			name = "ss.debug.test",
			usage = "/ss debug test"
		)
		public void nestedCommand(CommandArguments arguments) {
			arguments.sendMessage("nested");
		}

		@Command(
			name = "ss.debug.component",
			usage = "/ss debug component <name>"
		)
		public void nestedComponentCommand(CommandArguments arguments) {
			arguments.sendMessage("component");
		}

		@Command(
			name = "ss.debug.hidden"
		)
		public void hiddenCommand(CommandArguments arguments) {
			arguments.sendMessage("hidden");
		}
	}
}
