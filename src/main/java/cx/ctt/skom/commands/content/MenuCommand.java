package cx.ctt.skom.commands.content;

import cx.ctt.skom.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class MenuCommand extends Command {
    Tag<String> MINIGAME_KEY = Tag.String("minigame");
    final public static AtomicLong lastUpdate = new AtomicLong(System.currentTimeMillis());
    ArrayList<Inventory> cachedInventories = new ArrayList<>();

    ArrayList<Inventory> populateInv() {
        ArrayList<Inventory> inventories = new ArrayList<>();
        var minigames = Main.JEDIS.keys("minigame:*");
        var minigameIterator = minigames.iterator();
        int page = 1;
        for (int j = 0; j <= minigames.size(); j += 45) {
            String curMinigame = minigameIterator.next();
            Map<String, String> minigame = Main.JEDIS.hgetAll(curMinigame);
            Inventory inv = new Inventory(InventoryType.CHEST_6_ROW, Component.text("Page " + page));
            List<Component> lore = new ArrayList<>();
            minigame.keySet().stream().sorted().forEach(key -> {
                String value = minigame.get(key);
                if (key.equals("by")){
                    var username = Main.JEDIS.hget("player_cache", value);
                    if (username != null) { value = username; }
                }
                lore.add(Component.text(key + ": " +  value));
            });
            ItemStack a = ItemStack.builder(Material.PAPER)
                    .customName(Component.text(curMinigame.replace("minigame:", ""))
                            .color(NamedTextColor.WHITE))
                    .lore(lore).build().withTag(MINIGAME_KEY, curMinigame);
            inv.setItemStack(j, a);
            inventories.add(inv);
            page++;
        }
        Consumer<InventoryPreClickEvent> eventListener = event -> {
            event.setCancelled(true);
            var item = event.getClickedItem();
            if (item.hasTag(MINIGAME_KEY)){
                PlayCommand.PrepareGame(item.getTag(MINIGAME_KEY), null, event.getPlayer());
            }
        };
        inventories.forEach(inventory -> {
            inventory.eventNode().addListener(InventoryPreClickEvent.class, eventListener);
        });
        return inventories;
    }
    public MenuCommand() {
        super("menu");
        setDefaultExecutor((sender, context) -> {
            if (sender instanceof Player p){
                if (System.currentTimeMillis() - lastUpdate.get() > 1000) {
                    this.cachedInventories = populateInv();
                    lastUpdate.set(System.currentTimeMillis());
                }
                if (cachedInventories == null || cachedInventories.isEmpty()) {
                    sender.sendMessage("There are no cached inventories that are supposed to store the menu items :(");
                    return;
                }
                p.openInventory(cachedInventories.getFirst());
            }
        });
    }
}
