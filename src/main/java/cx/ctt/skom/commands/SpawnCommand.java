package cx.ctt.skom.commands;

import cx.ctt.skom.Main;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.SimpleCommand;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpawnCommand extends SimpleCommand {

    public static Pos spawnCoords = new Pos(0, 71, 0);

    public SpawnCommand() {
        super("spawn", "s");
    }

    @Override
    public boolean process(@NotNull CommandSender sender, @NotNull String command, String[] args) {
        Player p = (Player) sender;
        if (args.length == 0) {
            p.teleport(spawnCoords);
            Instance spawnInstance = Main.INSTANCES.get("spawn");
            if (p.getInstance() != spawnInstance){
                p.setInstance(Main.INSTANCES.get("spawn"));
            }
        } else {
//            Player targetPlayer = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(args[0]);
            Player targetPlayer = MinecraftServer.getConnectionManager().findOnlinePlayer(args[0]);
            if (targetPlayer == null) {
                sender.sendMessage("Player " + args[0] +  "  not found");
                return true;
            }
            if (targetPlayer.getInstance() != p.getInstance())
                targetPlayer.setInstance(p.getInstance());
            targetPlayer.teleport(p.getPosition());
        }
        return false;
    }

    @Override
    public boolean hasAccess(@NotNull CommandSender sender, @Nullable String commandString) {
        return true;
    }
}
