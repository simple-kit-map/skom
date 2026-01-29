package cx.ctt.skom.commands;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Player;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;

public class EnchantCommand extends Command {

    public EnchantCommand() {
        super("enchant");
        ArgumentString enchantArg = new ArgumentString("enchant");
        ArgumentInteger levelArg = new ArgumentInteger("level");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(Component.text("Only players can use this command"));
                return;
            }

            Enchantment enchant = MinecraftServer.getEnchantmentRegistry().get(Key.key(context.get(enchantArg)));

            var enchantTheRightTypeYesss = MinecraftServer.getEnchantmentRegistry().getKey(enchant);
            p.setItemInMainHand(
                    p.getItemInMainHand().with(DataComponents.ENCHANTMENTS, new EnchantmentList(
                    enchantTheRightTypeYesss, context.get(levelArg))));
            p.getInventory().update();

        }, enchantArg, levelArg);
    }
}
