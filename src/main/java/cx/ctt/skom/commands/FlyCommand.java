package cx.ctt.skom.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class FlyCommand extends Command {
        public FlyCommand() {
            super("fly");

            setDefaultExecutor((sender, context) -> {
                if (sender instanceof Player player) {
                    player.setAllowFlying(!player.isAllowFlying());
                }
            });
        }
}
