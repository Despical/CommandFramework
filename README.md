# CommandFramework
This framework is very lightweight annotation based command system that works similar to Bukkit's event system. It removes the necessity to
add command to your plugin.yml but will still allow you to set command usage, description, permission, aliases, sender type through the code.

## Using CommandFramework
The project isn't in the Central Repository yet, so specifying a repository is needed.
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
    <version>1.0.0</version>
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
    compileOnly group: "com.github.Despical", name: "CommandFramework", version: "1.0.0";
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

See the [LICENSE.txt](https://github.com/Despical/CommandFramework/blob/main/LICENSE) file for required notices and attributions.

## Donations
You like the CommandFramework? Then [donate]((https://www.patreon.com/despical)) back me to support the development.

## Building from source
If you want to build this project from source code, run the following from Git Bash:
```
git clone https://www.github.com/Despical/CommandFramework.git && cd CommandFramework
mvn clean package
```
Also don't forget to install Maven before building.