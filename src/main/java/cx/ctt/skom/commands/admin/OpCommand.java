package cx.ctt.skom.commands.admin;

import cx.ctt.skom.Main;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

public class OpCommand extends Command {
    public static final String OP_KEY = "skm:admin:op";

    public OpCommand() {
        super("op");
        var target = ArgumentType.Entity("target").onlyPlayers(true);
        addSyntax((sender, ctx) -> {
            var player = ctx.get(target).findFirstPlayer(sender);
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
