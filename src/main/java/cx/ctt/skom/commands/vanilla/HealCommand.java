package cx.ctt.skom.commands.vanilla;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentBoolean;
import net.minestom.server.entity.Player;
import net.minestom.server.potion.PotionEffect;

import java.util.List;

public class HealCommand extends Command {
    static List<PotionEffect> negative_effects = List.of(
            PotionEffect.POISON,
            PotionEffect.BAD_OMEN,
            PotionEffect.BLINDNESS,
            PotionEffect.DARKNESS,
            PotionEffect.HUNGER,
            PotionEffect.INFESTED,
            PotionEffect.UNLUCK,
            PotionEffect.POISON,
            PotionEffect.INSTANT_DAMAGE,
            PotionEffect.MINING_FATIGUE,
            PotionEffect.LEVITATION,
            PotionEffect.NAUSEA,
            PotionEffect.SLOWNESS,
            PotionEffect.WEAKNESS,
            PotionEffect.WEAVING,
            PotionEffect.WITHER
    );
    public HealCommand() {
        super("heal");
        setDefaultExecutor((sender, context) ->{
            heal(sender, false);
        });
        addSyntax((sender, context) ->{
            heal(sender, context.get("all_effects"));
        }, new ArgumentBoolean("all_effects"));
    }
    void heal(CommandSender sender, boolean all_effects){
        if(!(sender instanceof Player player)){
            sender.sendMessage("/su <player> heal");
            return;
        }
        player.heal();
        var effects = player.getActiveEffects();
        if(all_effects){

            player.clearEffects();
        } else {
            effects.forEach(effect -> {
                if (negative_effects.contains(effect.potion().effect()))
                    player.removeEffect(effect.potion().effect());
            });
        }
        player.clearEffects();
        player.setFoodSaturation(20);
        player.setFood(20);
        player.setFireTicks(0);
    }
}
