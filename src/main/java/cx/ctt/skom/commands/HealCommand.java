package cx.ctt.skom.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class HealCommand extends Command {
    public HealCommand() {
        super("heal");
        setDefaultExecutor((sender, context) ->{
            if(!(sender instanceof Player player)){
                sender.sendMessage("/su <player> heal");
                return;
            }
            player.heal();
            player.clearEffects();
            player.setFoodSaturation(20);
            player.setFood(20);
            player.setFireTicks(0);
        });
    }
}
