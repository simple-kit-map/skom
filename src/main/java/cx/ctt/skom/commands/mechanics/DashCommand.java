package cx.ctt.skom.commands.mechanics;

import net.minestom.server.command.builder.Command;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;

import java.text.DecimalFormat;

public class DashCommand extends Command {
    public DashCommand() {
        super("dash");

        setDefaultExecutor((sender, context) -> {
            if (sender instanceof Player player) {
                Pos prev = player.getPreviousPosition();
                Pos cur = player.getPosition();

                Vec diff;
                if (player.isSneaking()){
                    diff = new Vec(
                            prev.x() - cur.x(),
                            prev.y() - cur.y(),
                            prev.z() - cur.z()
                    );
                } else {
                    diff = new Vec(
                        cur.x() - prev.x(),
                        cur.y() - prev.y(),
                          cur.z() - prev.z()
                    );
                }

                if (diff.equals(Vec.ZERO)){
                    sender.sendMessage("no");
                    return;
                }
                diff = diff.normalize().mul(5);

                player.sendMessage(
                        roundDouble(diff.x()) + ", " +
                        roundDouble(diff.y()) +  ", " +
                        roundDouble(diff.z())
                );
                player.setVelocity(diff.mul(5));
            }
        });
    }
    public static String roundDouble(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.###");
        return twoDForm.format(d);
    }
}
