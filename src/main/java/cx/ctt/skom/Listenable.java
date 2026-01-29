package cx.ctt.skom;


import net.minestom.server.instance.Instance;

import javax.annotation.Nullable;

// gamerule template to be able to programmatically .register() functions from reflection
// this may be used globally (GlobalEventHandler), per instance or per entity (https://minestom.net/docs/feature/event)
public interface Listenable {
    public abstract void startListening(Instance instance, @Nullable String dbKey);
}
