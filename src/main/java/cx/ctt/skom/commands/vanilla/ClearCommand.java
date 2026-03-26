package cx.ctt.skom.commands.vanilla;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class ClearCommand extends Command {
    public ClearCommand() {
        super("clear");
        setDefaultExecutor((sender, _) ->{
                if (sender instanceof Player p) {
                    p.getInventory().clear();
                }
        }
        );
    }
}
