package cx.ctt.skom.gamerules;

import cx.ctt.skom.Gamerule;
import cx.ctt.skom.Main;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerDeathEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;

public class Lives implements Gamerule {
    HashMap<Player, Integer> lives = new HashMap<>();

    public void startListening(@NotNull Instance instance, @Nullable String dbKey) {

        int defaultLives = Integer.parseInt(Main.JEDIS.hget(dbKey, "Lives"));
        instance.eventNode().addListener(PlayerDeathEvent.class, event -> {
            if (!lives.keySet().contains(event.getPlayer())){
                lives.put(event.getPlayer(), defaultLives);
            }
            int curLives = lives.get(event.getPlayer());
            if (curLives == 0){
                // do shit when out of lives
            }
            curLives--;
            lives.put(event.getPlayer(), curLives);
        });
    }
}
