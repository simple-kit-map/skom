package cx.ctt.skom.gamerules;

import cx.ctt.skom.Gamerule;
import io.github.term4.minestommechanics.MinestomMechanics;
import net.minestom.server.event.instance.InstanceRegisterEvent;
import net.minestom.server.instance.Instance;
import org.jspecify.annotations.Nullable;

public class PvP implements Gamerule {
    boolean ran = false;

    public void startListening(Instance instance, @Nullable String dbKey) {
        var a = MinestomMechanics.getInstance();
        instance.eventNode().addListener(InstanceRegisterEvent.class, event -> {
            if (!ran) {

                ran = true;
            }
        });
    }
}
