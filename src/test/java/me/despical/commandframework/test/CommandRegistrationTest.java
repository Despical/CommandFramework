package me.despical.commandframework.test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import me.despical.commandframework.Command;
import me.despical.commandframework.CommandArguments;
import me.despical.commandframework.CommandFramework;
import me.despical.commandframework.Completer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

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
		assertEquals(1, commandFramework.getCommands().size());
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
			cooldown = 0, // no cooldown
			senderType = Command.SenderType.BOTH
		)
		public void exampleCommandMethod(CommandArguments arguments) {
			CommandSender sender = arguments.getSender();
			sender.sendMessage("This is how you can create a example command using framework.");
		}

		@Completer(name = "example", aliases = {"firstAlias", "secondAlias"})
		public List<String> exampleCommandCompletion(CommandArguments arguments) {
			return Arrays.asList("first", "second", "third");
		}
	}
}
