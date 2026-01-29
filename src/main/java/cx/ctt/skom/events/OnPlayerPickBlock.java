package cx.ctt.skom.events;

import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerPickBlockEvent;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class OnPlayerPickBlock {
    public static void register(GlobalEventHandler globalEventHandler) {
        // https://github.com/TonimatasDEV/Ethylene/blob/c43cb6ca5d74bb8a01fa914507d33d92c8bd1b5d/src/main/java/net/ethylene/server/listeners/PlayerListeners.java#L37
        globalEventHandler.addListener(PlayerPickBlockEvent.class, event -> {
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE) return;
            Material material = event.getBlock().registry().material();

            if (material == null) {
                return;
            }

            Player player = event.getPlayer();
            ItemStack itemStack = ItemStack.of(material);
            PlayerInventory inventory = event.getPlayer().getInventory();

            for (int slot = 0; slot < 9; slot++) {
                ItemStack item = inventory.getItemStack(slot);
                if (item.material() == material) {
                    player.setHeldItemSlot((byte) slot);
                    return;
                }
            }

            if (event.getPlayer().getItemInMainHand().isAir()) {
                event.getPlayer().setItemInMainHand(itemStack);
                return;
            }

            for (int slot = 0; slot < 9; slot++) {
                ItemStack item = inventory.getItemStack(slot);
                if (item.isAir()) {
                    player.setHeldItemSlot((byte) slot);
                    inventory.setItemStack(slot, itemStack);
                    return;
                }
            }

            // TODO: Full hotbar things
            player.setItemInMainHand(itemStack);
        });
    }
}
