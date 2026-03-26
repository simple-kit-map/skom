package cx.ctt.skom.commands.admin;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import org.jetbrains.annotations.NotNull;

public class StopCommand extends Command {
    public StopCommand() {
        super("stop");
        Argument<@NotNull String> reason = ArgumentType.String("reason");
        addSyntax((sender, context) -> {
            if (OpCommand.isNotOp(sender)) {
                sender.sendMessage("no permission");
                return;
            }
            sender.sendMessage("");
            sender.sendMessage("Restarting/Stopping: " + reason);
            sender.sendMessage("");
            MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> player.kick(context.get(reason)));
            MinecraftServer.getSchedulerManager().scheduleNextTick(() -> Thread.startVirtualThread(MinecraftServer::stopCleanly));
        }, reason);

    }
}
