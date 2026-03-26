package cx.ctt.skom.commands.vanilla;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;

public class SpecCommand extends Command {
    public SpecCommand() {
        super("spec");
        addSyntax((sender, context) -> {
            if (sender instanceof Player p){
                Player target = context.get("player");
                if (p.getGameMode() != GameMode.SPECTATOR)
                    p.setGameMode(GameMode.SPECTATOR);
                if (target.getInstance() != p.getInstance())
                    p.setInstance(p.getInstance());
                if (target.getPosition() != p.getPosition())
                    p.teleport(target.getPosition());
                p.spectate(target);
            }
        }, ArgumentType.Entity("player").onlyPlayers(true));
    }
}
