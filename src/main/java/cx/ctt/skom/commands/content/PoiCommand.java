package cx.ctt.skom.commands.content;

import cx.ctt.skom.Creatable;
import cx.ctt.skom.Main;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

public class PoiCommand extends Command {
    public PoiCommand() {
        super("setpoi");
        var nameArg = ArgumentType.Word("name");
        addSyntax((sender, context) -> {
            var name = context.get(nameArg);
            if (!(sender instanceof Player p)){
                sender.sendMessage("players only");
                return;
            }
            if (Creatable.isNotAlphaNumeric(name)){
                sender.sendMessage("point of interest name must be alphanumeric characters only");
                return;
            }
            Main.JEDIS.hset("skm:poi:"+Main.getInstanceName(p.getInstance()), name, p.getPosition().toString());
        }, nameArg);
    }
}
