package cx.ctt.skom.commands.content;

import cx.ctt.skom.Creatable;
import cx.ctt.skom.Main;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

/** @see cx.ctt.skom.gamerules.SpawnAtPoi */
public class SetPoiCommand extends Command {
    public SetPoiCommand() {
        super("setpoi");

        var nameArg = ArgumentType.Word("name");
        addSyntax((sender, context) -> {
            var name = context.get(nameArg);
            if (!(sender instanceof Player p)){
                sender.sendMessage("players only");
                return;
            }
            if (Creatable.isNotAlphaNumeric(name, ".")){
                sender.sendMessage("point of interest name must be alphanumeric characters only");
                return;
            }
            var posi = p.getPosition();
            var instanceName = Main.getInstanceName(p.getInstance());
            Main.JEDIS.hset("skm:poi:"+instanceName, name+":x", String.valueOf(posi.x()));
            Main.JEDIS.hset("skm:poi:"+instanceName, name+":y", String.valueOf(posi.y()));
            Main.JEDIS.hset("skm:poi:"+instanceName, name+":z", String.valueOf(posi.z()));
            Main.JEDIS.hset("skm:poi:"+instanceName, name+":pitch", String.valueOf(posi.pitch()));
            Main.JEDIS.hset("skm:poi:"+instanceName, name+":yaw", String.valueOf(posi.yaw()));
        }, nameArg);
    }
}
