package cx.ctt.skom.gamerules;

import cx.ctt.skom.Gamerule;
import cx.ctt.skom.Main;
import cx.ctt.skom.commands.content.KitCommand.KitLoad;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.Instance;

public class SpawnWithKit implements Gamerule {
    public void startListening(Instance instance, String dbKey) {
        String kitName = Main.JEDIS.hget(dbKey, "SpawnWithKit");
        instance.eventNode().addListener(
                PlayerSpawnEvent.class, event -> {
                    KitLoad.loadKit(event.getPlayer(), kitName);
                }
        );
    }
}
