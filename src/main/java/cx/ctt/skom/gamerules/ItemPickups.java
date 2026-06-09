package cx.ctt.skom.gamerules;

import cx.ctt.skom.Gamerule;
import cx.ctt.skom.Main;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class ItemPickups implements Gamerule {

    @Override
    public void startListening(Instance instance, @Nullable String dbKey) {
        var key = Main.JEDIS.hget(dbKey, "ItemPickups");
        if (key.equals("allow")){
            instance.eventNode().addListener(PickupItemEvent.class, event -> {
                final Entity entity = event.getLivingEntity();
                // taken from https://github.com/Minestom/Minestom/blob/9992e51042bce8fd13d9ca184a917ceebf33b491/demo/src/main/java/net/minestom/demo/PlayerInit.java#L88
                if (entity instanceof Player) { // Cancel event if player does not have enough inventory space
                    final ItemStack itemStack = event.getItemEntity().getItemStack();
                    event.setCancelled(!((Player) entity).getInventory().addItemStack(itemStack));
                }
                if (event.getEntity() instanceof Player p) {
                    MinecraftServer.getSchedulerManager().scheduleNextTick(() -> p.getInventory().update());
                }
            });
        }
    }
}
