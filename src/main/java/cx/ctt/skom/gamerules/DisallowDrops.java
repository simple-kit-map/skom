package cx.ctt.skom.gamerules;

import cx.ctt.skom.Listenable;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class DisallowDrops implements Listenable {

    @Override
    public void startListening(@NotNull Instance instance, @Nullable String dbKey) {
        instance.eventNode().addListener(ItemDropEvent.class, event -> event.setCancelled(true));
    }
}
