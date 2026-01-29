package cx.ctt.skom.commands;

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

public class SudoCommand extends Command {
    public SudoCommand() {
        super("sudo", "su");
        ArgumentEntity player = ArgumentType.Entity("target").onlyPlayers(true);

//        ArgumentStringArray command = new ArgumentStringArray("command");
        ArgumentCommand command = new ArgumentCommand("command");
//        String.join(" ", context.get(command))

        addSyntax(((sender, context) -> {
            Player targetPlayer = context.get(player).findFirstPlayer(sender);
            if (targetPlayer == null) {
                sender.sendMessage("Player not found.");
                return;
            }
            MinecraftServer.getCommandManager().execute(targetPlayer, context.get(command).getInput());
        }), player, command);
    }

    public boolean process(@NotNull CommandSender sender, @NotNull String command, String @NotNull [] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /su <player> <command>");
        }
        if (args.length == 1) {
            sender.sendMessage("Usage: /su " + args[0] + " <command>");
        }
        Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(args[0]);
        if (player == null) {
            sender.sendMessage(args[0] + " is not online.");
            return true;
        }
        MinecraftServer.getCommandManager().execute(player, String.join(" ", Arrays.stream(args).toList().removeFirst()));
        return false;
    }

    public boolean hasAccess(@NotNull CommandSender sender, @Nullable String commandString) {
        if (sender instanceof Player player)
            return player.getPermissionLevel() == 4;
        return true;
    }
}
