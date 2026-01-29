package cx.ctt.skom.commands;

import cx.ctt.skom.Creatable;
import net.minestom.server.command.builder.Command;

public class Kit3Command extends Creatable {
    public Kit3Command() {
        super("kit3", "k3");
        addSubcommand(new KitCreate());
    }
    class KitCreate extends Command {
        public KitCreate(){
            super("create", "new");
        }
    }
}
