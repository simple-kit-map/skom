package cx.ctt.skom.gamerules;

import cx.ctt.skom.Listenable;
import cx.ctt.skom.Main;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerDeathEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;

public class Lives implements Listenable {
    HashMap<Player, Integer> lives = new HashMap<>();

    public void startListening(@NotNull Instance instance, @Nullable String dbKey) {
        int defaultLives = Integer.getInteger(Main.JEDIS.get(dbKey));
        instance.eventNode().addListener(PlayerDeathEvent.class, event -> {
            if (!lives.keySet().contains(event.getPlayer())){
                lives.put(event.getPlayer(), defaultLives);
            }
            int curLives = lives.get(event.getPlayer());
            curLives--;
            lives.put(event.getPlayer(), curLives);
        });
    }
}
