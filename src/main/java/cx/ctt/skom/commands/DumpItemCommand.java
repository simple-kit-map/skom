package cx.ctt.skom.commands;

import com.google.gson.Gson;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;

public class DumpItemCommand extends Command {
    public DumpItemCommand() {
        super("dumpitem");
        setDefaultExecutor((sender , context)-> {
            if (!(sender instanceof Player p)){
                return;
            }
            ItemStack target = p.getItemInMainHand();
            Gson ret = new Gson();
            sender.sendMessage(ret.toJson(target));
        });
    }
}
