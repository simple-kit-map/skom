package cx.ctt.skom.events;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.utils.time.TimeUnit;


public class ItemDropListener {
    public static void register(GlobalEventHandler handler) {
        register(handler, 80);
    }
    public static void register(GlobalEventHandler handler, int droppedItemLifetime) {
        // https://github.com/TonimatasDEV/Ethylene/blob/c43cb6ca5d74bb8a01fa914507d33d92c8bd1b5d/src/main/java/net/ethylene/server/listeners/ItemListener.java#L14

        handler.addListener(ItemDropEvent.class, event -> {

//            event.getPlayer().scheduleNextTick(() -> event.getPlayer().getInventory().update());
            MinecraftServer.getSchedulerManager().scheduleNextTick(() -> event.getPlayer().getInventory().update());
            if (droppedItemLifetime <= 0) return;

            ItemEntity itemEntity = new ItemEntity(event.getItemStack());
            Pos playePos = event.getPlayer().getPosition();
            double eyeHeight = event.getPlayer().getEyeHeight() - (double) 0.3F;
            Vec velocity = event.getPlayer().getPosition().direction().mul(6);

            itemEntity.setPickupDelay(40, TimeUnit.SERVER_TICK);
            itemEntity.setMergeable(true);
            itemEntity.setMergeRange(1);
            itemEntity.setVelocity(velocity);
            itemEntity.setInstance(event.getPlayer().getInstance(), new Pos(playePos.x(), eyeHeight + playePos.y(), playePos.z()));
            itemEntity.scheduler().scheduleTask(itemEntity::remove, TaskSchedule.tick(droppedItemLifetime), TaskSchedule.stop());
        });
        // https://github.com/Minestom/Minestom/blob/9992e51042bce8fd13d9ca184a917ceebf33b491/demo/src/main/java/net/minestom/demo/PlayerInit.java#L88
        handler.addListener(PickupItemEvent.class, event -> {
            final Entity entity = event.getLivingEntity();
            if (entity instanceof Player) {
                // Cancel event if player does not have enough inventory space
                final ItemStack itemStack = event.getItemEntity().getItemStack();
                event.setCancelled(!((Player) entity).getInventory().addItemStack(itemStack));
            }
            if (event.getEntity() instanceof Player p) {
                MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
                    p.getInventory().update();
                });
            }
        });
    }
}
