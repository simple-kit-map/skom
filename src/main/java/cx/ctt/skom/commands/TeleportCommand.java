package cx.ctt.skom.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.location.RelativeVec;

public class TeleportCommand extends Command {

    public TeleportCommand() {
        super("tp");

        setDefaultExecutor((source, context) -> source.sendMessage(Component.text("Usage: /tp x y z")));

        var posArg = ArgumentType.RelativeVec3("pos");
        var playerArg = ArgumentType.Entity("player").onlyPlayers(true);

        addSyntax((((sender, context) -> {
            final Player target = context.get(playerArg).findFirstPlayer(sender);
            assert target != null;
            if (sender instanceof Player player) {
                player.teleport(target.getPosition());
                if (player.getInstance() != target.getInstance())
                    player.setInstance(target.getInstance());
                sender.sendMessage(Component.text("Teleported to player " + target.getUsername()));
            }
        })), playerArg);
        addSyntax(((sender, context) -> {
            final Player player = (Player) sender;
            final RelativeVec relativeVec = context.get(posArg);
            final Pos position = player.getPosition().withCoord(relativeVec.from(player));
            player.teleport(position);
            player.sendMessage(Component.text("You have been teleported to " + position));
        }), posArg);
        setDefaultExecutor(((sender, context) -> {
            sender.sendMessage("you didnt put the rights args");
        }));
    }
}
