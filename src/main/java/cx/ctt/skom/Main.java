package cx.ctt.skom;

import com.minestom.mechanics.config.combat.CombatConfig;
import com.minestom.mechanics.config.combat.CombatPresets;
import com.minestom.mechanics.config.gameplay.DamageConfig;
import com.minestom.mechanics.config.gameplay.DamagePresets;
import com.minestom.mechanics.config.gameplay.GameplayConfig;
import com.minestom.mechanics.config.gameplay.GameplayPresets;
import com.minestom.mechanics.config.knockback.KnockbackConfig;
import com.minestom.mechanics.config.projectiles.ProjectileConfig;
import com.minestom.mechanics.config.projectiles.ProjectilePresets;
import com.minestom.mechanics.config.world.WorldInteractionConfig;
import com.minestom.mechanics.manager.MechanicsManager;
import com.minestom.mechanics.manager.ProjectileManager;
import com.minestom.mechanics.systems.blocking.BlockingSystem;
import com.minestom.mechanics.systems.compatibility.ClientVersionDetector;
import com.minestom.mechanics.systems.compatibility.ModernClientOptimizer;
import com.minestom.mechanics.systems.compatibility.animation.ViewerBasedAnimationHandler;
import com.minestom.mechanics.systems.knockback.KnockbackSystem;
import cx.ctt.skom.commands.*;
import cx.ctt.skom.events.*;
import io.github.togar2.pvp.MinestomPvP;
import fr.ghostrider584.axiom.AxiomMinestom;
import io.github.togar2.pvp.feature.CombatFeatureSet;
import io.github.togar2.pvp.feature.CombatFeatures;
import io.github.togar2.pvp.feature.FeatureType;
import io.github.togar2.pvp.feature.provider.DifficultyProvider;
import io.github.togar2.pvp.potion.effect.CombatPotionEffects;
import io.github.togar2.pvp.potion.item.CombatPotionTypes;
import io.github.togar2.pvp.utils.CombatVersion;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.block.Block;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPooled;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static io.github.togar2.pvp.feature.CombatFeatures.*;


public class Main {

    public static final HashMap<String, InstanceContainer> INSTANCES = new HashMap<>();

    // used to get the string name of an instance from an instance object https://stackoverflow.com/a/2904266
    public String getKeyByValue(HashMap<String, InstanceContainer> instances, Instance instance) {
        for (HashMap.Entry<String, InstanceContainer> entry : instances.entrySet()) {
            if (Objects.equals(instance, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static JedisPooled JEDIS = new JedisPooled("localhost", 6379);

    public static final Logger LOG = LoggerFactory.getLogger("SKOM");

    public static final @NotNull String NODE_NAME = System.getenv("NODE_NAME") != null ? System.getenv("NODE_NAME") : "DEV";

    public static Path MAP_PATH = Paths.get(System.getProperty("user.dir"), "maps");

    public static final long STARTED_AT = System.currentTimeMillis();

    void main(String[] args) {
        LOG.info("NODE_NAME: {}", NODE_NAME);


        if (!Files.exists(MAP_PATH) || !Files.isDirectory(MAP_PATH)){
            LOG.error("'maps' folder NOT FOUND, did you forget to make one or are you not in the right directory?");
            System.exit(1);
        }

        Auth auth;
//        if (NODE_NAME.equals("DEV"))
//            auth = new Auth.Offline();
//        else
            auth = new Auth.Bungee();

        MinecraftServer minecraftServer = MinecraftServer.init(auth);
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        GlobalEventHandler handler = MinecraftServer.getGlobalEventHandler();

        /* Commands */ {
            Set.of(
                    new TeleportCommand(),
                    new HealCommand(),
                    new EnchantCommand(),
                    new StopCommand(),
                    new FlyCommand(),
                    new UptimeCommand(),
                    new SudoCommand(),
                    new DashCommand(),
                    new SpawnCommand(),
                    new GamemodeCommand(),
                    new SetUnbreakableCommand(),
                    new SubscribeToAllEventsCommand(),
                    new WarpCommand(),
                    new KitCommand(),
                    new Kit2Command(),
                    new MinigameCommand(),
                    new TransferCommand(),
                    new CookieCommand(),
                    new HorseCommand(),
                    new DumpItemCommand(),
                    new HideCommand(),
                    new ShowCommand()
            ).forEach(command -> MinecraftServer.getCommandManager().register(command));
            SkmCommandEvent.register(handler);
            GamemodeCommand.setUpTheSneakyGamemodeAliases(handler);
            MinecraftServer.getCommandManager().setUnknownCommandCallback(UnknownCommand::register);
        }

        /* skom */ {
            SkmChatEvent.register(handler);
            SkmPlayerEvent.register(handler);
            OnPlayerPickBlock.register(handler);
            ItemDropListener.register(handler);
            Knockback.register(handler);
        }

        /* Plugins */ {
            AxiomMinestom.initialize();
//            VanillaReimplementation vri = VanillaReimplementation.hook(MinecraftServer.process());

            /* MinestomPvP */ if (false){

                MinestomPvP.init(true, true);
                CombatFeatureSet skmSet = getVanilla(CombatVersion.LEGACY, DifficultyProvider.DEFAULT)
                        .remove(VANILLA_SWEEPING.featureType())
                        .remove(VANILLA_BLOCK.featureType())
                        .remove(LEGACY_VANILLA_BLOCK.featureType())
//                    .remove(VANILLA_DAMAGE.featureType())
//                    .remove(FeatureType.ATTACK)
//                    .remove(FeatureType.ATTACK_COOLDOWN)
//                    .remove(FeatureType.CRITICAL)
//                    .remove(VANILLA_ITEM_DAMAGE.featureType())
//                    .remove(VANILLA_KNOCKBACK.featureType())
//                    .remove(FAIR_RISING_KNOCKBACK.featureType())
//                    .remove(FAIR_RISING_FALLING_KNOCKBACK.featureType())
                        .remove(FeatureType.FOOD)
                        .build();

                MinecraftServer.getGlobalEventHandler().addChild(skmSet.createNode());
            }
            /* minestom-mechanics-lib */ if (false){
//                 Set up server-wide world interaction configuration
            WorldInteractionConfig worldConfig = WorldInteractionConfig.builder()
                    .blockReach(6.0, 4.5)  // Creative, Survival
                    .blockRaycastStep(0.2)
                    .build();

            com.minestom.mechanics.config.ServerConfig.setWorldInteraction(worldConfig);

            MinecraftServer.LOGGER.info("[Main] World interaction configuration set");

//                 ===========================
//                 PVP INITIALIZATION
//                 ===========================

            com.minestom.mechanics.systems.misc.MinestomVelocityFix.initialize();
            KnockbackConfig mmc = KnockbackConfig.validated(
                    0.375,  // horizontal
                    0.365,  // vertical
                    0.45,   // verticalLimit
                    0.34,   // sprintBonusHorizontal
                    0.0,    // sprintBonusVertical
                    1.0,    // airMultiplierHorizontal
                    1.0,    // airMultiplierVertical
                    0.5,    // lookWeight
                    false,   // modern
                    false    // knockbackSyncSupported
            );
            KnockbackSystem.initialize(mmc);
            CombatConfig COMBAT_CONFIG = CombatPresets.MINEMEN;
            DamageConfig DAMAGE_CONFIG = DamagePresets.MINEMEN;
            GameplayConfig GAMEPLAY_PRESET = GameplayPresets.LEGACY_1_8;
            ProjectileConfig PROJECTILE_CONFIG = ProjectilePresets.VANILLA18;
            MechanicsManager.getInstance()
                    .configure()
                    .withCombat(COMBAT_CONFIG)
                    .withGameplay(GAMEPLAY_PRESET)
                    .withDamage(DAMAGE_CONFIG)
                    .withHitbox(com.minestom.mechanics.config.combat.HitDetectionConfig.standard())
                    .withArmor(true)
                    .withKnockback(COMBAT_CONFIG.knockbackConfig(), true)  // ← CHANGED
                    .initialize();

//                 Initialize projectiles separately (not handled by MechanicsManager YET)
//                 Using default projectile config
            ProjectileManager.getInstance()
                    .initialize(PROJECTILE_CONFIG);

            BlockingSystem.initialize(COMBAT_CONFIG);
            ClientVersionDetector.getInstance();
            ViewerBasedAnimationHandler.getInstance();
            ModernClientOptimizer.getInstance();

            MinecraftServer.LOGGER.info("[Main] All PvP systems initialized successfully!");
            AxiomMinestom.initialize();
            MinestomPvP.init();
            CombatPotionEffects.registerAll();
            CombatPotionTypes.registerAll();
            CombatFeatureSet legacyVanilla = CombatFeatures.legacyVanilla();
            MinecraftServer.getGlobalEventHandler().addChild(legacyVanilla.createNode());
            }
        }

        /* Default warps */ {
            InstanceContainer spawn = instanceManager.createInstanceContainer(
                    MinecraftServer.getDimensionTypeRegistry().register(
                            "spawn",  DimensionType.builder().build()
                    ),
                    new AnvilLoader(Paths.get(String.valueOf(MAP_PATH), "spawn"))
            );
            spawn.setTimeRate(0);
            spawn.setChunkSupplier(LightingChunk::new);
            INSTANCES.put("spawn", spawn);

            InstanceContainer flat = instanceManager.createInstanceContainer(
                    MinecraftServer.getDimensionTypeRegistry().register(
                            "flat",  DimensionType.builder().build()
                    ));
            flat.setGenerator(unit -> {
                unit.modifier().fillHeight(39, 40, Block.QUARTZ_BLOCK);
                unit.modifier().fillHeight(0, 39, Block.BEDROCK);
            });
            flat.setTimeRate(0);
            flat.setChunkSupplier(LightingChunk::new);
            INSTANCES.put("flat", flat);

//            Listenable foo = new DisallowDrops();
//            handler.addListener(foo.eventType, foo::StartListening);
//            EventNode<Event> a = EventNode.all("flat");
//            foo.StartListening(a);

//            foo.StartListening(flatHandler);
        }

        minecraftServer.start("127.0.0.1", 45565);
        LOG.info("started in {}ms", System.currentTimeMillis() - STARTED_AT);
    }

}