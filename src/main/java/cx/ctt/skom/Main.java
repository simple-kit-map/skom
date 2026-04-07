package cx.ctt.skom;

import ca.spottedleaf.oldgenerator.OldChunkGenerator;
import ca.spottedleaf.oldgenerator.listener.LegacyPopulateHack;
import cx.ctt.skom.commands.admin.MotdCommand;
import cx.ctt.skom.commands.admin.UnknownCommand;
import cx.ctt.skom.commands.vanilla.GamemodeCommand;
import cx.ctt.skom.events.*;
import cx.ctt.skom.gamerules.AllowItemDrops;
import fr.ghostrider584.axiom.AxiomMinestom;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.command.builder.Command;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.block.Block;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.world.DimensionType;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.RedisClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class Main {
    public static final HashMap<String, InstanceContainer> INSTANCES = new HashMap<>();

    // used to get the string name of an instance from an instance object https://stackoverflow.com/a/2904266
    public static String getInstanceName(Instance instance) {
        for (HashMap.Entry<String, InstanceContainer> entry : INSTANCES.entrySet()) {
            if (Objects.equals(instance, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static RedisClient JEDIS = RedisClient.builder().hostAndPort("localhost", 6379).build(); // used to be var JEDIS = new Jedis("localhost", 6379);
    public static final Logger LOG = LoggerFactory.getLogger("SKOM");
    public static Path MAP_PATH = Paths.get(System.getProperty("user.dir"), "maps");
    public static final long STARTED_AT = System.currentTimeMillis();

    static Map<String, Auth> AUTH_TYPES = Map.of(
            "online", new Auth.Online(),
            "offline", new Auth.Offline(),
            "bungee", new Auth.Bungee()
    );
    static HashMap<String, String> skomSettings = new HashMap<>(Map.of(
        "dispatcher_threads", "1",
        "address", "127.0.0.1",
        "port", "45565",
        "auth", "online",
        "node_name", "DEV"
    ));
    public static String NODE_NAME;
    void main(){
        for (var env : System.getenv().entrySet()){
            if (!env.getKey().startsWith("SKM_")) continue;
            var key = env.getKey().replace("SKM_", "").toLowerCase();
            if (!skomSettings.containsKey(key)){ Main.LOG.error("Unknown SKM env var {}", env.getKey()); continue;}
            skomSettings.put(key, env.getValue());
        }

        NODE_NAME = skomSettings.get("node_name");
        LOG.info("NODE_NAME: {}", NODE_NAME);
        System.setProperty("minestom.dispatcher-threads", String.valueOf(Runtime.getRuntime().availableProcessors()));

        if (!Files.exists(MAP_PATH) || !Files.isDirectory(MAP_PATH)) {
            LOG.error("'maps' folder NOT FOUND!, did you forget to make one, or are you not in the right directory?");
            System.exit(1);
        }
        ClassPreloader.preloadAsync();
        MinecraftServer minecraftServer = MinecraftServer.init(
                AUTH_TYPES.get(skomSettings.get("auth"))
        );
        registerSkmGlobals();
        minecraftServer.start(skomSettings.get("address"), Integer.parseInt(skomSettings.get("port")));
        LOG.info("started in {}ms, connect at {}:{}", System.currentTimeMillis() - STARTED_AT, skomSettings.get("address"), Integer.parseInt(skomSettings.get("port")));
    }

    void newInstance(InstanceManager instanceManager, String name, RegistryKey<DimensionType> dimensionType, Consumer<InstanceContainer> cons) {
        InstanceContainer instance;
        if (dimensionType == null) {
            instance = instanceManager.createInstanceContainer(
                    MinecraftServer.getDimensionTypeRegistry().register(
                            name, DimensionType.builder().build()
                    ));
        } else {
            instance = instanceManager.createInstanceContainer(dimensionType);
        }
        cons.accept(instance);
        instance.setTimeRate(0);
        instance.setChunkSupplier(LightingChunk::new);
        INSTANCES.put(name, instance);
    }

    void registerSkmGlobals() {
        /* Plugins */
        {
            ClientVersionDetector.getInstance();
            AxiomMinestom.initialize();
        }
        /* Instances */
        {
            InstanceManager instanceManager = MinecraftServer.getInstanceManager();
            newInstance(instanceManager, "spawn", null, i ->
                    i.setChunkLoader(new AnvilLoader(Paths.get(String.valueOf(MAP_PATH), "spawn"))));

            newInstance(instanceManager, "flat", null, i ->
                    i.setGenerator(unit -> unit.modifier().fillHeight(38, 40, Block.BARRIER)));

            newInstance(instanceManager, "nether", DimensionType.THE_NETHER, i -> {
                i.setChunkLoader(OldChunkGenerator.getNetherGenerator(404));
                LegacyPopulateHack.registerEvents(i.eventNode());
            });
            newInstance(instanceManager, "beta", null, i -> {
                i.setChunkLoader(OldChunkGenerator.getOverworldGenerator(404));
                LegacyPopulateHack.registerEvents(i.eventNode());
            });
            newInstance(instanceManager, "aether", null, i -> {
                i.setChunkLoader(OldChunkGenerator.getSkyGenerator(404));
                LegacyPopulateHack.registerEvents(i.eventNode());
            });
        }
        /* events */
        {
            GlobalEventHandler handler = MinecraftServer.getGlobalEventHandler();
            /* skom */
            {
                SkmChatEvent.register(handler);
                SkmPlayerEvent.register(handler);
                OnPlayerPickBlock.register(handler);
                AllowItemDrops.register(handler);
                Knockback.register(handler);
            }
            /* Commands */
            {
                MotdCommand.registerMotdListener();
                SkmCommandEvent.register(handler);
                GamemodeCommand.setUpTheSneakyGamemodeAliases(handler);
                MinecraftServer.getCommandManager().setUnknownCommandCallback(UnknownCommand::register);

                CommandManager commandManager = MinecraftServer.getCommandManager();

                var commands = new Reflections(this.getClass().getPackageName()+".commands").getSubTypesOf(Command.class);
                for (Class<?> cmd : commands) {
                    try {
                        var a = cmd.getDeclaredConstructor().newInstance();
                        commandManager.register((Command)a);
                    } catch (Exception e) {
                        Main.LOG.error(e.getMessage());
                    }
                }
            }
        }
    }
}
