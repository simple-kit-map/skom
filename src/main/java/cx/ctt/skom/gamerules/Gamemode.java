package cx.ctt.skom.gamerules;

import cx.ctt.skom.Gamerule;
import cx.ctt.skom.Main;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.Instance;
import org.jspecify.annotations.Nullable;

public class Gamemode implements Gamerule {

    public void startListening(Instance instance, @Nullable String dbKey) {
        instance.eventNode().addListener(PlayerSpawnEvent.class, event -> {
            String gamemode = Main.JEDIS.hget(dbKey, "Gamemode");
            event.getPlayer().setGameMode(GameMode.valueOf(gamemode.toUpperCase()));
        });
    }
}
