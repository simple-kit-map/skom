package cx.ctt.skom.events;

import cx.ctt.skom.Main;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerChatEvent;
import redis.clients.jedis.JedisPubSub;

public class SkmChatEvent {
    public static void register(GlobalEventHandler globalEventHandler) {
        globalEventHandler.addListener(PlayerChatEvent.class, event -> {
            Main.LOG.info("{}.{}>{}", event.getPlayer().getUuid(), event.getPlayer().getUsername(), event.getRawMessage());
            Main.JEDIS.publish("PlayerChat", Main.NODE_NAME + ':' + event.getPlayer().getUsername() + "> " + event.getRawMessage());
        });
        JedisPubSub jedisPubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                String region = message.split(":")[0];
                if (Main.NODE_NAME.equals(region)) return;
                Main.LOG.info("{}: {}", channel, message);
                MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> player.sendMessage(message));
            }
        };
        new Thread(() -> Main.JEDIS.subscribe(jedisPubSub, "PlayerChat")).start();
    }
}
