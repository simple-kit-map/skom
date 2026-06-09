package cx.ctt.skom.commands.admin.debug;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.event.book.EditBookEvent;
import net.minestom.server.event.entity.*;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.event.entity.projectile.ProjectileUncollideEvent;
import net.minestom.server.event.instance.*;
import net.minestom.server.event.inventory.*;
import net.minestom.server.event.item.*;
import net.minestom.server.event.player.*;
import net.minestom.server.event.server.ClientPingServerEvent;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.event.server.ServerTickMonitorEvent;
import net.minestom.server.event.trait.*;

import java.util.HashSet;
import java.util.Set;

public class SubscribeToAllEventsCommand extends Command {

    static Set<Class> alreadySeen = new HashSet<>(Set.of());
    public SubscribeToAllEventsCommand() {
        super("subscribe");

        setDefaultExecutor((sender, context) -> {
            var events = Set.of(
                    EditBookEvent.class,
                    EntityAttackEvent.class,
                    EntityDamageEvent.class,
                    EntityDeathEvent.class,
                    EntityDespawnEvent.class,
                    EntityFireExtinguishEvent.class,
                    EntityItemMergeEvent.class,
                    EntityPotionAddEvent.class,
                    EntityPotionRemoveEvent.class,
                    EntitySetFireEvent.class,
                    EntityShootEvent.class,
                    EntitySpawnEvent.class,
                    EntityTeleportEvent.class,
                    EntityTickEvent.class,
                    EntityVelocityEvent.class,
                    ProjectileCollideWithBlockEvent.class,
                    ProjectileCollideWithEntityEvent.class,
                    ProjectileUncollideEvent.class,
                    AddEntityToInstanceEvent.class,
                    InstanceBlockUpdateEvent.class,
                    InstanceChunkLoadEvent.class,
                    InstanceChunkUnloadEvent.class,
                    InstanceRegisterEvent.class,
                    InstanceSectionInvalidateEvent.class,
                    InstanceTickEvent.class,
                    InstanceUnregisterEvent.class,
                    RemoveEntityFromInstanceEvent.class,
                    CreativeInventoryActionEvent.class,
                    InventoryClickEvent.class,
                    InventoryCloseEvent.class,
                    InventoryItemChangeEvent.class,
                    InventoryOpenEvent.class,
                    InventoryPreClickEvent.class,
                    EntityEquipEvent.class,
                    ItemDropEvent.class,
                    PickupExperienceEvent.class,
                    PickupItemEvent.class,
                    PlayerBeginItemUseEvent.class,
                    PlayerCancelItemUseEvent.class,
                    PlayerFinishItemUseEvent.class,
                    AdvancementTabEvent.class,
                    AsyncPlayerConfigurationEvent.class,
                    AsyncPlayerPreLoginEvent.class,
                    PlayerAnvilInputEvent.class,
                    PlayerBlockBreakEvent.class,
                    PlayerBlockInteractEvent.class,
                    PlayerBlockPlaceEvent.class,
                    PlayerCancelDiggingEvent.class,
                    PlayerChangeHeldSlotEvent.class,
                    PlayerChatEvent.class,
                    PlayerChunkLoadEvent.class,
                    PlayerChunkUnloadEvent.class,
                    PlayerCommandEvent.class,
                    PlayerConfigCustomClickEvent.class,
                    PlayerCustomClickEvent.class,
                    PlayerDeathEvent.class,
                    PlayerDebugSubscriptionsRequestEvent.class,
                    PlayerDisconnectEvent.class,
                    PlayerEditSignEvent.class,
                    PlayerEntityInteractEvent.class,
                    PlayerFinishDiggingEvent.class,
                    PlayerGameModeChangeEvent.class,
                    PlayerGameModeRequestEvent.class,
                    PlayerHandAnimationEvent.class,
                    PlayerLeaveBedEvent.class,
                    PlayerLoadedEvent.class,
                    PlayerMoveEvent.class,
                    PlayerPacketEvent.class,
                    PlayerPickBlockEvent.class,
                    PlayerPickEntityEvent.class,
                    PlayerPluginMessageEvent.class,
                    PlayerResourcePackStatusEvent.class,
                    PlayerRespawnEvent.class,
                    PlayerSettingsChangeEvent.class,
                    PlayerSkinInitEvent.class,
                    PlayerSpawnEvent.class,
//                    PlayerSpectateEvent.class,
                    PlayerStartDiggingEvent.class,
                    PlayerStartFlyingEvent.class,
                    PlayerStartFlyingWithElytraEvent.class,
                    PlayerStartSprintingEvent.class,
                    PlayerStopFlyingEvent.class,
                    PlayerStopFlyingWithElytraEvent.class,
                    PlayerStopSprintingEvent.class,
                    PlayerSwapItemEvent.class,
                    PlayerTickEndEvent.class,
                    PlayerTickEvent.class,
                    PlayerUseItemEvent.class,
                    PlayerUseItemOnBlockEvent.class,
                    ClientPingServerEvent.class,
                    ServerListPingEvent.class,
                    ServerTickMonitorEvent.class,
                    AsyncEvent.class,
                    BlockEvent.class,
                    CancellableEvent.class,
                    EntityEvent.class,
                    EntityInstanceEvent.class,
                    InstanceEvent.class,
                    InventoryEvent.class,
                    ItemEvent.class,
                    PlayerEvent.class,
                    PlayerInstanceEvent.class
            );

            events.forEach(eventClass ->
                    MinecraftServer.getGlobalEventHandler().addListener(eventClass, event -> {
                        if (!alreadySeen.contains(eventClass)) {
                            alreadySeen.add(eventClass);
                        } else {
                            return;
                        }
                        sender.sendMessage(event.toString());
                    })
            );
        });
    }
}
