package cx.ctt.skom.commands.mechanics;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

import javax.swing.*;

public class FlyCommand extends Command {
        public FlyCommand() {
            super("fly");

            setDefaultExecutor((sender, context) -> {
                if (sender instanceof Player player) {
                    var allowed = player.isAllowFlying();
                    player.setAllowFlying(!player.isAllowFlying());
                    sender.sendMessage(allowed ? "fly off" : "fly on");
                }
            });
        }
}
