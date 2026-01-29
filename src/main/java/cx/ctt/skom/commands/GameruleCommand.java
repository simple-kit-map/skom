package cx.ctt.skom.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentString;

public class GameruleCommand extends Command {
    public GameruleCommand() {
        super("gamerule", "gr");
        addSyntax((sender, args) -> {
        }, new ArgumentString("gamerule"));
    }
}
