<h1 align="center">Command Framework</h1>

<div align="center">

[![](https://github.com/Despical/CommandFramework/actions/workflows/build-commandframework.yml/badge.svg)](https://github.com/Despical/CommandFramework/actions/workflows/build-commandframework.yml)
[![](https://jitpack.io/v/Despical/CommandFramework.svg)](https://jitpack.io/#Despical/CommandFramework)
[![](https://img.shields.io/badge/JavaDocs-latest-lime.svg)](https://javadoc.jitpack.io/com/github/Despical/CommandFramework/latest/javadoc/index.html)
[![](https://img.shields.io/badge/Buy%20Me%20A%20Coffee-Support-lime.svg?logo=BuyMeACoffee)](https://www.buymeacoffee.com/despical)

This framework is very lightweight annotation based command system that works similar to Bukkit's event system. It removes the necessity to
add commands to your plugin.yml but will still allow you to set command usage, description, permission, aliases, sender type, cooldown, minimum
and maximum argument length through the code and adds a bunch of new methods to improve your code.

</div>

## Documentation
- [Wiki](https://github.com/Despical/CommandFramework/wiki)
- [JavaDocs](https://javadoc.jitpack.io/com/github/Despical/CommandFramework/latest/javadoc/index.html)

## Donations
- [Patreon](https://www.patreon.com/despical)
- [Buy Me A Coffee](https://www.buymeacoffee.com/despical)

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
    <version>1.4.7</version>
    <scope>compile</scope>
</dependency>
```

### Gradle dependency
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```
```groovy
dependencies {
    implementation 'com.github.Despical:CommandFramework:1.4.7'
}
```

> [!IMPORTANT]  
> Do not forget to relocate the Command Framework package (**me.despical.commandframework**)

## Example usage

```java
import me.despical.commandframework.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
		cooldown = 10,
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
> **[Maven](https://maven.apache.org/)** must be installed to build Command Framework.
