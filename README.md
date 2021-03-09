# Command Framework
[![](https://jitpack.io/v/Despical/CommandFramework.svg)](https://jitpack.io/#Despical/CommandFramework)
[![](https://img.shields.io/badge/javadocs-latest-lime.svg)](https://javadoc.jitpack.io/com/github/Despical/CommandFramework/latest/javadoc/index.html)
[![discord](https://img.shields.io/discord/719922452259668000.svg?color=lime&label=discord)](https://discord.gg/Vhyy4HA)

This framework is very lightweight annotation based command system that works similar to Bukkit's event system. It removes the necessity to
add command to your plugin.yml but will still allow you to set command usage, description, permission, aliases, sender type, cooldown, minimum
and maximum argument length through the code.

## Documentation
More information can be found on the [wiki page](https://github.com/Despical/CommandFramework/wiki). The [Javadoc](https://javadoc.jitpack.io/com/github/Despical/CommandFramework/latest/javadoc/index.html) can be browsed. Questions
related to the usage of Command Framework should be posted on my [Discord server](https://discord.com/invite/Vhyy4HA).

## Using Command Framework
The project isn't in the Central Repository yet, so specifying a repository is needed.<br>
To add this project as a dependency to your project, add the following to your pom.xml:

### Maven dependency

```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```
```xml
<dependency>
    <groupId>com.github.Despical</groupId>
    <artifactId>CommandFramework</artifactId>
    <version>1.0.1</version>
    <scope>compile</scope>
</dependency>
```

### Gradle dependency
```
repositories {
    maven { url 'https://jitpack.io' }
}
```
```
dependencies {
    compileOnly group: "com.github.Despical", name: "CommandFramework", version: "1.0.1";
}
```

## Example usage
```java
public class ExampleClass extends JavaPlugin {

    // Don't forget to shade framework in to your project
    CommandFramework commandFramework;

    @Override
    public void onEnable() {
        // Initialize the framework before using
        commandFramework = new CommandFramework(this);

        // Then this will register all the @Command methods as a command
        // so there is no necessity to add command to your plugin.yml
        commandFramework.registerCommands(this);
    }

    // Before creating command the method must only have
    // CommandArguments parameter and also @Command annotation
    @Command(name = "example",
            aliases = {"firstAlias", "secondAlias"},
            permission = "example.permission",
            description = "Sends an example message to sender",
            usage = "/example",
            min = 1,
            max = 5,
            cooldown = 10,
            senderType = Command.SenderType.CONSOLE)
    public void exampleCommandMethod(CommandArguments arguments) {
        // CommandArguments class contains basic things related Bukkit commands
        CommandSender sender = arguments.getSender();
        // And here it's all done, you've created command with properties above!
        sender.sendMessage("This is how you can create a example command using framework.");
    }

    // Aliases don't need to be same with the command above
    @Completer(name = "example", aliases = {"firstAlias", "secondAlias"})
    public List<String> exampleCommandCompletion(CommandArguments arguments) {
        // And you've created a tab completion for the command above
        return Arrays.asList("first", "second", "third");
    }
}
```

## License
This code is under [GPL-3.0 License](http://www.gnu.org/licenses/gpl-3.0.html)

See the [LICENSE](https://github.com/Despical/CommandFramework/blob/main/LICENSE) file for required notices and attributions.

## Donations
You like the Command Framework? Then [donate](https://www.patreon.com/despical) back me to support the development.

## Contributing

I accept Pull Requests via GitHub. There are some guidelines which will make applying PRs easier for me:
+ Ensure you didn't use tabs! Please use spaces for indentation.
+ Respect the code style.
+ Do not increase the version numbers in any examples files and the README.md to the new version that this Pull Request would represent.
+ Create minimal diffs - disable on save actions like reformat source code or organize imports. If you feel the source code should be reformatted create a separate PR for this change.

You can learn more about contributing via GitHub in [contribution guidelines](CONTRIBUTING.md).

## Known issues
* ~~Sub-commands aren't compatible with tab completions.~~

## To do list
* Message handler to change system messages.
* Exception handler to make exceptions more clear.

## Building from source
If you want to build this project from source code, run the following from Git Bash:
```
git clone https://www.github.com/Despical/CommandFramework.git && cd CommandFramework
mvn clean package
```
Also don't forget to install Maven before building.
