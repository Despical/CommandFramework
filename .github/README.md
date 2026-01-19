<h1 align="center">Command Framework</h1>

<div align="center">

[![](https://github.com/Despical/CommandFramework/actions/workflows/build.yml/badge.svg)](https://github.com/Despical/CommandFramework/actions/workflows/build.yml)
[![](https://img.shields.io/maven-central/v/dev.despical/command-framework.svg?label=Maven%20Central)](https://repo1.maven.org/maven2/dev/despical/command-framework)
[![](https://img.shields.io/badge/License-GPLv3-blue.svg)](../LICENSE)
[![](https://img.shields.io/badge/Javadoc-latest-blue.svg)](https://despical.github.io/CommandFramework)

A lightweight, annotation-based command framework inspired by Bukkit’s event system. It eliminates the need to register commands in plugin.yml
while still allowing you to define usage, description, permissions, aliases, sender types, cooldowns, and argument limits directly in code.
Includes extra helper methods to streamline command handling and improve code readability.

</div>

## Documentation
- [Wiki](https://github.com/Despical/CommandFramework/wiki)
- [Documentation](https://docs.despical.dev/command-framework/)
- [Javadocs](https://despical.github.io/CommandFramework)
- [Maven Central](https://repo1.maven.org/maven2/dev/despical/command-framework)
- [Sonatype Central](https://central.sonatype.com/artifact/dev.despical/command-framework)

## Using Command Framework
To add this project as a dependency to your project, add the following to your pom.xml:

### Maven dependency
```xml
<dependency>
    <groupId>dev.despical</groupId>
    <artifactId>command-framework</artifactId>
    <version>1.6.1</version>
</dependency>
```

### Gradle dependency
```groovy
dependencies {
    implementation 'dev.despical:command-framework:1.6.1'
}
```

> [!IMPORTANT]  
> Remember to relocate the Command Framework package to avoid conflicts (**dev.despical.commandframework**).

## Example usage

```java
public class ExampleClass extends JavaPlugin {

    @Override
    public void onEnable() {
        // Initialize the framework before using
        // Don't forget to shade framework in to your project
        CommandFramework commandFramework = new CommandFramework(this);
        // Adding custom parameters without @Param and @Default annotations.
        // Now all String type objects will return first argument.
        commandFramework.addCustomParameter("String", arguments -> arguments.getArgument(0));
        // Adding custom parameters to use with @Param and optionally with @Default annotations.
        commandFramework.addCustomParameter("secondAsInt", arguments -> arguments.getLength() > 1 ? arguments.getArgumentAsInt(1) : null);
        // Then this will register all the @Command methods as a command
        // so there is no necessity to add command to your plugin.yml
        commandFramework.registerCommands(this);
    }

    // Before creating command the method must only have
    // CommandArguments parameter and also @Command annotation
    @Command(
        name = "example",
        fallbackPrefix = "prefix",
        aliases = {"firstAlias", "secondAlias"},
        permission = "example.permission",
        desc = "Sends an example message to sender",
        usage = "/example",
        min = 1,
        max = 5,
        onlyOp = false, // this option will ignore permission if it is set
        // be careful if you are using non-thread safe operations
        // and if you want to enable option below
        async = false,
        senderType = Command.SenderType.CONSOLE
    )
    @Cooldown(
        value = 10,
        timeUnit = TimeUnit.SECONDS,
        bypassPerm = "command.cooldownBypass",
        overrideConsole = true // console will now be affected by cooldown
    )
    public void exampleCommand(CommandArguments arguments) {
        // CommandArguments class contains basic things related Bukkit commands
        // And here it's all done, you've created command with properties above!
        arguments.sendMessage("This is how you can create a example command using framework.");
    }

    @Command(
        name = "noParams"
    )
    public void commandWithoutParameters() {
        Bukkit.getConsoleSender().sendMessage("This command is running without any parameters.");
    }

    @Command(
        name = "customParamWithoutAnnotations",
        min = 1
    )
    // See CommandFramework#addCustomParameter method above.
    public void customParamCommand(String firstParameter, CommandArguments arguments) {
        // CommandArguments parameter can be added to anywhere in method as a parameter.
        arguments.sendMessage("First parameter is " + firstParameter);
    }

    @Command(
        name = "customParams",
        min = 1
    )
    // If command is executed with only one argument then the default value will be accepted.
    // Otherwise, the given argument will be converted to specified type, in this case an int.
    // If parameter is not annotated by @Default then command will throw an exception on execution.
    // See the wiki page for creating custom parameters using @Param and @Default annotations.
    public void customParamsCommand(CommandArguments arguments,
                                    @Param("secondAsInt")
                                    @Default("50")
                                    int secondArg) {
        arguments.sendMessage("Second argument as integer is " + secondArg);
    }

    @Command(
        name = "confirmationTest"
    )
    @Confirmation(
        message = "Are you sure, if so, please execute command again to confirm.",
        expireAfter = 10,
        bypassPerm = "confirmation.bypass",
        timeUnit = TimeUnit.SECONDS,
        overrideConsole = true
    )
    public void confirmationCommand(CommandArguments arguments) {
        arguments.sendMessage("Confirmation successful.");
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

    // Aliases don't need to be same with the command above
    @Completer(
        name = "example",
        aliases = {"firstAlias", "secondAlias"}
    )
    public List<String> exampleCommandCompletion(/*CommandArguments arguments*/ /*no need to use in this case which is also supported*/) {
        return Arrays.asList("first", "second", "third");
    }
}
```

## Contributing

I accept Pull Requests via GitHub. There are some guidelines which will make applying PRs easier for me:
+ Ensure you didn't use tabs! Please use spaces for indentation.
+ Respect the code style.
+ Do not increase the version numbers in any examples files and the README.md to the new version that this Pull Request would represent.
+ Create minimal diffs - disable on save actions like reformat source code or organize imports. If you feel the source code should be reformatted create a separate PR for this change.

You can learn more about contributing via GitHub in [contribution guidelines](../CONTRIBUTING.md).

## License
This code is under the [GPL-3.0 License](http://www.gnu.org/licenses/gpl-3.0.html).

See the [LICENSE](../LICENSE) file for required notices and attributions.

## Known issues
* ~~Cooldowns are not working command based.~~
* ~~Framework can't detect the sub commands when a sub command registered with the name of main command.~~
* ~~Sub-commands aren't compatible with tab completions.~~

## To do list
* ~~Custom parameters with the same type.~~
* ~~Exception log handler.~~
* ~~Message handler to change system messages.~~

## Building from source
To build this project from source code, run the following from Git Bash:
```
git clone https://www.github.com/Despical/CommandFramework && cd CommandFramework
mvn clean package -DskipTests -Dmaven.javadoc.skip=true
```

> [!IMPORTANT]  
> **[Maven](https://maven.apache.org/)** must be installed to build this project.
