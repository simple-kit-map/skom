package cx.ctt.skom.gamerules;

import cx.ctt.skom.Gamerule;
import cx.ctt.skom.Main;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.function.Consumer;


public class ItemDrops implements Gamerule {
    public void startListening(@NotNull Instance instance, @Nullable String dbKey) {
        String param = Main.JEDIS.hget(dbKey, "ItemDrops");
        if (param.startsWith("allow")){
            instance.eventNode().addListener(ItemDropEvent.class, event -> {

                Player player = event.getPlayer();
                player.getInventory().update();
                ItemStack droppedItem = event.getItemStack();

                ItemEntity itemEntity = new ItemEntity(droppedItem);
                itemEntity.setPickupDelay(500, TimeUnit.MILLISECOND);
                itemEntity.setInstance(player.getInstance(), player.getPosition().add(0, 1.5f, 0));

                Vec velocity = player.getPosition().direction().mul(5);
                itemEntity.setVelocity(velocity);

                // lifetime of 5 minutes
                itemEntity.scheduler().scheduleTask(itemEntity::remove,
                        TaskSchedule.duration(Duration.ofMinutes(5)), TaskSchedule.stop());
            });
        }
        if  (param.equals("disallow")){
            instance.eventNode().addListener(ItemDropEvent.class, e -> e.setCancelled(true));
        }
    }
}
