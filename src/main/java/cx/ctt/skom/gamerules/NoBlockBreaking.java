package cx.ctt.skom.gamerules;

import cx.ctt.skom.Gamerule;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import org.jspecify.annotations.Nullable;

public class NoBlockBreaking implements Gamerule {

    public void startListening(Instance instance, @Nullable String dbKey) {
        instance.eventNode().addListener(PlayerBlockBreakEvent.class, event -> {
                event.setCancelled(true);
        });
    }
}
