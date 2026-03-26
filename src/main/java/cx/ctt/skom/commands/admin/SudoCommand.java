package cx.ctt.skom.commands.admin;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentCommand;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SudoCommand extends Command {
    public SudoCommand() {
        super("sudo", "su");
        ArgumentEntity player = ArgumentType.Entity("target").onlyPlayers(true);

        var commandArg = ArgumentType.StringArray("command");

        addSyntax(((sender, context) -> {
            if (OpCommand.isNotOp(sender)) {
                sender.sendMessage("no permission");
                return;
            }
            Player targetPlayer = context.get(player).findFirstPlayer(sender);
            if (targetPlayer == null) {
                sender.sendMessage("Player not found.");
                return;
            }
            var cmd = context.get(commandArg);
            var cmdString = String.join(" ", cmd);
            MinecraftServer.getCommandManager().execute(targetPlayer, cmdString);
            if (sender instanceof Player p ) {
                if (!OpCommand.isNotOp(targetPlayer)) {
                    targetPlayer.sendMessage(p.getUsername() + " sudo'd you to /" + cmdString);
                }
            }
        }), player, commandArg);
    }
}
