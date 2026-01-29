package cx.ctt.skom.commands;

import cx.ctt.skom.Listenable;
import cx.ctt.skom.Main;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import org.reflections.Reflections;
import redis.clients.jedis.json.Path2;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MinigameCommand extends Command {
    public MinigameCommand() {
        super("minigame", "mi");
        addSyntax((sender, context) -> {
            String[] names = context.get("player(s)");
            HashSet<Player> players = new HashSet<>();
            for (String name : names) {
                Player toAdd = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(name);
                if (toAdd == null){
                    sender.sendMessage("Player " + name + " is not online");
                    continue;
                }
                players.add(toAdd);
            }
            if (players.isEmpty()) {
                return;
            }
            StartGame(context.get("map"), players, (Player) sender);

        }, new ArgumentString("map"), new ArgumentString("preset"), new ArgumentStringArray("player(s)"));
    }

    public void StartGame(String minigameName, Set<Player> players, Player initiator){
        players.add(initiator);
        String miniGameKey = "minigame:" + minigameName;
        var mapName = Main.JEDIS.hget(miniGameKey, "map");

        Path mapPath = WarpCommand.getMapPath(mapName, initiator);
        InstanceContainer instance = WarpCommand.loadWorld(initiator, mapName, mapPath);
        if (instance == null) {
            initiator.sendMessage("failed loading world" + mapName);
            return;
        }

        List<String> enabledGamerules = Main.JEDIS.lrange("map:"+mapName+":gamerules:*", 0, -1);

        String packageName = getClass().getPackage().getName().replace(".commands", ".gamerules");
        for (Class<?> clazz  : new Reflections(packageName).getSubTypesOf(Listenable.class)){
            if (clazz.getSimpleName().equals("Listenable")) continue;
            if (enabledGamerules.contains(clazz.getSimpleName())){
                try {
                    Listenable listener = (Listenable) clazz.getConstructor().newInstance();
                    listener.startListening(instance, mapName);
                } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                    Main.LOG.error("{}", e.getMessage());
                }
            }
            Main.LOG.error("{}", clazz.getSimpleName());
        }
        players.forEach(p->p.setInstance(instance));
    }

}
