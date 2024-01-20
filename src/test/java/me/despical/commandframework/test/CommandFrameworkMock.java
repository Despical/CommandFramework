package me.despical.commandframework.test;

import me.despical.commandframework.CommandFramework;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Mocked class of {@link CommandFramework}.
 * @author gamerover98
 */
public class CommandFrameworkMock extends CommandFramework {

	public CommandFrameworkMock(@NotNull Plugin plugin) {
		super(plugin);

		/*
		 * There is no SimplePluginManager class in MockBukkit,
		 * so the commandMap can be easily obtained from the ServerMock instance.
		 */
		this.commandMap = plugin.getServer().getCommandMap();
	}
}