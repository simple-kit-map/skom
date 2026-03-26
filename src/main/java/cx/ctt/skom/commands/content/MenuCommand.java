package cx.ctt.skom.commands.content;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;

public class MenuCommand extends Command {
    public MenuCommand() {
        super("menu");
        setDefaultExecutor((sender, context) -> {
            Inventory inv = new Inventory(InventoryType.CHEST_6_ROW, Component.text(""));
            if (sender instanceof Player p){
                p.openInventory(inv);
            }
        });
    }
}
