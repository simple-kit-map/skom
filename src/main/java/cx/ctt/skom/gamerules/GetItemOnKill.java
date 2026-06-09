package cx.ctt.skom.gamerules;

import cx.ctt.skom.Gamerule;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerDeathEvent;
import net.minestom.server.instance.Instance;
import org.jspecify.annotations.Nullable;

public class GetItemOnKill implements Gamerule {
    @Override
    public void startListening(Instance instance, @Nullable String dbKey) {
        instance.eventNode().addListener(PlayerDeathEvent.class, event -> {
            if (event.getPlayer().getLastDamageSource().getSource() instanceof Player) {

            }
        });
    }
}
