package cx.ctt.skom.commands.admin;

import cx.ctt.skom.Main;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

// first player needs to be OP'd from redis-cli
//! echo "hset skm:admin:op c8bcf862-e997-4a54-9c59-681bd22096e5 Couleur" | redis-cli
public class OpCommand extends Command {
    public static final String OP_KEY = "skm:admin:op";

    public OpCommand() {
        super("op");
        var target = ArgumentType.Entity("target").onlyPlayers(true);
        addSyntax((sender, context) -> {
            var player = context.get(target).findFirstPlayer(sender);
            if (player == null) {
                sender.sendMessage("Player not found.");
                return;
            }
            if (isNotOp(sender)) {
                sender.sendMessage("no permission");
                return;
            }
            Main.JEDIS.hset(OP_KEY, player.getUuid().toString(), player.getUsername());
        }, target);
    }

    public static boolean isNotOp(CommandSender sender) {
        if (sender instanceof Player p) {
            return !Main.JEDIS.hexists(OP_KEY, p.getUuid().toString());
        }
        return false; // else it's a console
    }
}
