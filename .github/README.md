<h1 align="center">Command Framework</h1>

<div align="center">

[![Build](https://github.com/Despical/CommandFramework/actions/workflows/build-commandframework.yml/badge.svg)](https://github.com/Despical/CommandFramework/actions/workflows/build-commandframework.yml)
[![](https://jitpack.io/v/Despical/CommandFramework.svg)](https://jitpack.io/#Despical/CommandFramework)
[![](https://img.shields.io/badge/JavaDocs-latest-lime.svg)](https://javadoc.jitpack.io/com/github/Despical/CommandFramework/latest/javadoc/index.html)
[![Support](https://img.shields.io/badge/Patreon-Support-lime.svg?logo=Patreon)](https://www.patreon.com/despical)

This framework is very lightweight annotation based command system that works similar to Bukkit's event system. It removes the necessity to
add commands to your plugin.yml but will still allow you to set command usage, description, permission, aliases, sender type, cooldown, minimum
and maximum argument length through the code and adds a bunch of new methods to improve your code.

</div>

## Documentation
- [Wiki](https://github.com/Despical/CommandFramework/wiki)
- [JavaDocs](https://javadoc.jitpack.io/com/github/Despical/CommandFramework/latest/javadoc/index.html)

## Donations
- [Patreon](https://www.patreon.com/despical)
- [Buy Me A Coffe](https://www.buymeacoffee.com/despical)

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
    <version>1.2.6</version>
    <scope>compile</scope>
</dependency>
```

### Gradle dependency
```
repositories {
    maven { url = uri("https://jitpack.io") }
}
```
```
dependencies {
    compileOnly group: "com.github.Despical", name: "CommandFramework", version: "1.2.6";
}
```

## Example usage
```java
public class ExampleClass extends JavaPlugin {

    // Don't forget to shade framework in to your project
    private CommandFramework commandFramework;

    @Override
    public void onEnable() {
        // Initialize the framework before using
        commandFramework = new CommandFramework(this);

        // Then this will register all the @Command methods as a command
        // so there is no necessity to add command to your plugin.yml
        commandFramework.registerCommands(this);
        commandFramework.setAnyMatch(arguments -> {
            if (arguments.isArgumentsEmpty()) return;

            String label = arguments.getLabel(), arg = arguments.getArgument(0);
            
            // StringMatcher is an external class from Despical's Commons library.
            List<StringMatcher.Match> matches = StringMatcher.match(arg, commandFramework.getCommands().stream().map(cmd -> cmd.name().replace(label + ".", "")).collect(Collectors.toList()));

            if (!matches.isEmpty()) {
                arguments.sendMessage("Did you mean %command%?".replace("%command%", label + " " + matches.get(0).getMatch()));
            }
        });
    }

    // Before creating command the method must only have
    // CommandArguments parameter and also @Command annotation
    @Command(
            name = "example",
            aliases = {"firstAlias", "secondAlias"},
            permission = "example.permission",
            desc = "Sends an example message to sender",
            usage = "/example",
            min = 1,
            max = 5,
            cooldown = 10,
            onlyOp = false, // this option will ignore permission if it is set
            // be careful if you are using non-thread safe operations
            // and if you want to enable option below
            async = false,
            senderType = Command.SenderType.CONSOLE
    )
    public void exampleCommand(CommandArguments arguments) {
        // CommandArguments class contains basic things related Bukkit commands
        // And here it's all done, you've created command with properties above!
        arguments.sendMessage("This is how you can create a example command using framework.");
    }

    // Aliases don't need to be same with the command above
    @Completer(
              name = "example",
              aliases = {"firstAlias", "secondAlias"}
    )
    public List<String> exampleCommandCompletion(CommandArguments arguments) {
        // And you've created a tab completion for the command above
        return Arrays.asList("first", "second", "third");
    }
}
```

## License
This code is under [GPL-3.0 License](http://www.gnu.org/licenses/gpl-3.0.html)

See the [LICENSE](https://github.com/Despical/CommandFramework/blob/main/LICENSE) file for required notices and attributions.

## Contributing

I accept Pull Requests via GitHub. There are some guidelines which will make applying PRs easier for me:
+ Ensure you didn't use spaces! Please use tabs for indentation.
+ Respect the code style.
+ Do not increase the version numbers in any examples files and the README.md to the new version that this Pull Request would represent.
+ Create minimal diffs - disable on save actions like reformat source code or organize imports. If you feel the source code should be reformatted create a separate PR for this change.

You can learn more about contributing via GitHub in [contribution guidelines](../CONTRIBUTING.md).

## Known issues
* ~~Cooldowns are not working command based.~~
* ~~Framework can't detect the sub commands when a sub command registered with the name of main command.~~
* ~~Sub-commands aren't compatible with tab completions.~~

## To do list
* ~~Exception log handler.~~
* ~~Message handler to change system messages.~~

## Building from source
To build this project from source code, run the following from Git Bash:
```
git clone https://www.github.com/Despical/CommandFramework && cd CommandFramework
mvn clean package -Dmaven.javadoc.skip=true
```

> **Note** Don't forget to install Maven before building.
