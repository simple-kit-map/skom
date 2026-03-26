package cx.ctt.skom.commands.content;

import cx.ctt.skom.Creatable;
import cx.ctt.skom.Gamerule;
import cx.ctt.skom.Main;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlayCommand extends Command {
    static final Map<String, Class<? extends Gamerule>> GAMERULES;

    static {
        GAMERULES = new HashMap<>();
        String packageName = PlayCommand.class.getPackage().getName().replace(".commands", ".gamerules");
        Reflections reflections = new Reflections(new ConfigurationBuilder().forPackages(packageName)                 // or .forPackage("com.example")
                .addUrls(ClasspathHelper.forJavaClassPath()).setScanners(new SubTypesScanner(false))  // false = don't exclude Object
        );
        Set<Class<? extends Gamerule>> rules = reflections.getSubTypesOf(Gamerule.class);

        for (Class<?> clazz : rules) {
            if (clazz.getSimpleName().equals("Gamerule")) continue;
            try {
                Gamerule listener = (Gamerule) clazz.getConstructor().newInstance();
                GAMERULES.put(clazz.getSimpleName(), listener.getClass());
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                Main.LOG.error("{}", e.getMessage());
            }
            Main.LOG.error("LOADING GAMEMODE: {}", clazz.getSimpleName());
        }
    }

    public PlayCommand() {
        super("play", "pl");
        var minigameNameArg = new ArgumentString("minigameName");
        var playersArg = new ArgumentStringArray("player(s)");
        addSyntax((sender, context) -> {
            PrepareGame(context.get(minigameNameArg), context.get((playersArg)), sender);
        }, minigameNameArg, playersArg);
        addSyntax((sender, context) -> {
            PrepareGame(context.get(minigameNameArg), new String[0], sender); // that second argument is an empty array
        }, minigameNameArg);
    }

    public void PrepareGame(String mgName, String[] players, CommandSender initiator) {

        Set<Player> parsedPlayers = new HashSet<>();
        for (String name : players) {
            Player toAdd = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(name);
            if (toAdd == null) {
                initiator.sendMessage("Player " + name + " is not online");
                continue;
            }
            parsedPlayers.add(toAdd);
        }
        if (parsedPlayers.isEmpty()) {
            initiator.sendMessage("you did not mention anyone to play the game with :( play alone..");
            parsedPlayers.add((Player) initiator);
        }
        if (Creatable.isNotAlphaNumeric(mgName)) {
            initiator.sendMessage("illegal minigame name");
        }
        StartGame(mgName, parsedPlayers, (Player) initiator);
    }

    public void StartGame(String mgName, Set<Player> players, Player initiator) {
        players.add(initiator);
        String mgKey = "minigame:" + mgName;
        var mapName = Main.JEDIS.hget(mgKey, "map");

        Path mapPath = WarpCommand.getMapPath(mapName, initiator);
        if (mapPath == null) {
            initiator.sendMessage("No map found for " + mgKey);
            return;
        }
        InstanceContainer instance = WarpCommand.loadWorld(initiator, mapName, mapPath);
        if (instance == null) {
            initiator.sendMessage("failed loading world" + mapName);
            return;
        }

        Set<String> gameruleKeys = Main.JEDIS.hkeys(mgKey);
        for (String gamerule : gameruleKeys) {
            var clazz = GAMERULES.get(gamerule);
            if (clazz != null) {
                try {
                    clazz.getConstructor().newInstance().startListening(instance, mgKey);
                } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                         IllegalAccessException e) {
                    Main.LOG.error("{}", e.getMessage());
                }
            }
        }

        players.forEach(p -> p.setInstance(instance));
    }
}
