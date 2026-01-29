package cx.ctt.skom.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Player;

public class HideCommand extends Command {
    public HideCommand() {
        super("hide");
        ArgumentEntity player = ArgumentType.Entity("target").onlyPlayers(true);
        addSyntax((sender, context) -> {
            Player targetPlayer = context.get(player).findFirstPlayer(sender);
            if (targetPlayer == null) {
                sender.sendMessage("Player not found.");
                return;
            }
            if (sender instanceof Player) {
                targetPlayer.updateViewableRule(_ -> false);
            }

        }, player);
    }
}
