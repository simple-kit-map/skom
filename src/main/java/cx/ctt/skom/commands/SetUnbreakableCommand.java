package cx.ctt.skom.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.SimpleCommand;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SetUnbreakableCommand extends SimpleCommand {
    public SetUnbreakableCommand() {
        super("setunbreakable");
    }

    @Override
    public boolean process(@NotNull CommandSender sender, @NotNull String command, String @NotNull [] args) {
        Player p = (Player) sender;
        p.setItemInHand(PlayerHand.MAIN, p.getItemInHand(PlayerHand.MAIN).with(DataComponents.UNBREAKABLE));
        return false;
    }

    @Override
    public boolean hasAccess(@NotNull CommandSender sender, @Nullable String commandString) {
        return true;
    }
}
