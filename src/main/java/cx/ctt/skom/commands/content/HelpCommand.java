package cx.ctt.skom.commands.content;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HelpCommand extends Command {
    static Component helpResult;
    public HelpCommand() {

        super("help", "?");
        setDefaultExecutor((sender, context) -> {
            // needs to be cached lazily because if it checks commands whilst they're still in the middle of being loaded,
            // the commands that would be loaded after /help would be missing
            if (helpResult == null) {
                var builder = Component.empty();

                // Get all commands, sorted alphabetically
                List<Command> sortedCommands = MinecraftServer.getCommandManager().getCommands()
                        .stream()
                        .sorted(Comparator.comparing(command -> command.getNames()[0]))
                        .toList();

                for (int i = 0; i < sortedCommands.size(); i++) {
                    Command command = sortedCommands.get(i);
                    var names = command.getNames();
                    String mainName = "/" + names[0];

                    Component toAdd;
                    if (names.length > 1) {
                        // Create aliases string: (/alias1, /alias2, ...)
                        String aliases = Arrays.stream(names)
                                .skip(1)
                                .map(name -> "/" + name)
                                .collect(Collectors.joining(", "));
                        toAdd = Component.text(mainName + " (" + aliases + ")");
                    } else {
                        toAdd = Component.text(mainName);
                    }
                    builder = builder.append(toAdd.clickEvent(ClickEvent.suggestCommand(mainName + " ")));

                    // Add comma and space between commands (except after the last one)
                    if (i < sortedCommands.size() - 1) {
                        builder = builder.append(Component.text(", "));
                    }
                }
                helpResult = builder;
            }
            sender.sendMessage(helpResult);
        });
    }
}
