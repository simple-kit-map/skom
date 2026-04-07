package cx.ctt.skom.commands.content;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.TaskSchedule;

import java.util.Set;

enum PING_ACTIONS {
    START,
    STOP
}

public class PingCommand extends Command {
    static Set<Player> pinging_players;
    public PingCommand() {
        super("ping");
        var action = ArgumentType.Enum("action", PING_ACTIONS.class);
        setDefaultExecutor((sender, context) -> {
            if (sender instanceof Player p){
                var entities = p.getInstance().getNearbyEntities(p.getPosition(), 50);
                for (Entity entity : entities) {
                    if (entity instanceof Player other) {
                        if (other == p) continue;
                        sender.sendMessage(other.getUsername() + ": " + String.valueOf(p.getLatency()));
                    }
                }
                sender.sendMessage(String.valueOf(p.getLatency()));
            }
        });
        addSyntax((sender, context) -> {
            PING_ACTIONS act = context.get(action);
            if (act == PING_ACTIONS.START) {
                pinging_players.add((Player) sender);
                MinecraftServer.getSchedulerManager().submitTask(() -> {
                    if (!pinging_players.contains((Player) sender)) {
                        return TaskSchedule.stop();
                    }
                    sender.sendMessage(String.valueOf(((Player)sender).getLatency()));
                    return TaskSchedule.millis(100);
                });
            }
            if (act == PING_ACTIONS.STOP) {
                pinging_players.remove((Player) sender);
            }
        }, action);
    }
}
