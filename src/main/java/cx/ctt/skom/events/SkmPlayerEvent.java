package cx.ctt.skom.events;

import cx.ctt.skom.Main;
import cx.ctt.skom.commands.SpawnCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

/**
 * Fancy name for handling when a player logs in and out
 */
public class SkmPlayerEvent {
    public static HashMap<UUID, Pos> lastPositions = new HashMap<>();
    public static HashMap<UUID, ItemStack[]> lastItems = new HashMap<>();

    static void Broadcast(Component message){
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> player.sendMessage(message));
    }

    static void Broadcast(String message){
        MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> player.sendMessage(message));
    }

    public static void register(GlobalEventHandler globalEventHandler) {

        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            Main.JEDIS.hset("player_cache", player.getUuid().toString(), player.getUsername());
            event.setSpawningInstance(Main.INSTANCES.get("spawn"));
            setAnimatiumFeatures(player, Set.of(Feature.ALL, Feature.MISS_PENALTY, Feature.LEFT_CLICK_ITEM_USAGE));

            if (lastPositions.containsKey(player.getUuid())) {
                player.setRespawnPoint(lastPositions.get(player.getUuid()));
            } else {
                player.setRespawnPoint(SpawnCommand.spawnCoords);
            }
            if (lastItems.containsKey(player.getUuid())) {
                player.getInventory().copyContents(lastItems.get(player.getUuid()));
            }
            player.setGameMode(GameMode.CREATIVE);
            player.setPermissionLevel(4);

            event.setHardcore(true);
            Component username = player.getDisplayName() != null ? player.getDisplayName() : Component.text(player.getUsername());
            Component joinMsg = username.color(NamedTextColor.YELLOW).append(Component.text(" joined the game"));
            Broadcast(joinMsg);
//            player.sendMessage(joinMsg);
        });
        globalEventHandler.addListener(PlayerDisconnectEvent.class, event -> {
            Player player = event.getPlayer();
            UUID id = player.getUuid();
            lastPositions.put(id, player.getPosition());
//            jedis.set("toItemStackArray" + id, Arrays.toString(player.getInventory().getItemStacks()));
            lastItems.put(id, player.getInventory().getItemStacks());

            Component username = player.getDisplayName() != null ? player.getDisplayName() : Component.text(player.getUsername());
            Component leaveMsg = username.color(NamedTextColor.YELLOW).append(Component.text(" left the game"));
            Broadcast(leaveMsg);
        });
    }

    private static void setAnimatiumFeatures(Player player, Set<Feature> features) {
        final NetworkBuffer buf = NetworkBuffer.resizableBuffer();

        buf.write(NetworkBuffer.VAR_INT, features.size());
        for (Feature feature : features) {
            buf.write(NetworkBuffer.STRING, feature.name().toLowerCase());
        }

        player.sendPluginMessage("animatium:set_features", buf.read(NetworkBuffer.RAW_BYTES));
    }

    enum Feature {
        ALL,
        MISS_PENALTY,
        LEFT_CLICK_ITEM_USAGE
    }

}
