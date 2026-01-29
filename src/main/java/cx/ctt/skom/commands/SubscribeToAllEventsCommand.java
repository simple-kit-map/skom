package cx.ctt.skom.commands;

import com.google.gson.Gson;
import cx.ctt.skom.Main;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.book.*;
import net.minestom.server.event.entity.*;
import net.minestom.server.event.inventory.*;
import net.minestom.server.event.instance.*;
import net.minestom.server.event.entity.projectile.*;
import net.minestom.server.event.item.*;
import net.minestom.server.event.server.*;
import net.minestom.server.event.player.*;
import net.minestom.server.event.trait.*;

import java.util.Set;

public class SubscribeToAllEventsCommand extends Command {
    public SubscribeToAllEventsCommand() {
        super("subscribe");
        addSyntax((sender, args) -> {
            if (!(sender instanceof Player player)){
                return;
            }
            Set.of(
//                    Event.class,
//                    EditBookEvent.class,
//                    EntityAttackEvent.class,
//                    EntityDamageEvent.class,
//                    EntityDeathEvent.class,
//                    EntityDespawnEvent.class,
//                    EntityFireExtinguishEvent.class,
//                    EntityItemMergeEvent.class,
//                    EntityPotionAddEvent.class,
//                    EntityPotionRemoveEvent.class
//                    EntitySetFireEvent.class,
//                    EntityShootEvent.class,
//                    EntitySpawnEvent.class,
//                    EntityTeleportEvent.class,
////                    EntityTickEvent.class,
//                    EntityVelocityEvent.class,
//                    ProjectileCollideWithBlockEvent.class,
//                    ProjectileCollideWithEntityEvent.class,
//                    ProjectileUncollideEvent.class,
//                    AddEntityToInstanceEvent.class,
//                    InstanceBlockUpdateEvent.class,
////                    InstanceChunkLoadEvent.class,
////                    InstanceChunkUnloadEvent.class,
//                    InstanceRegisterEvent.class,
//                    InstanceSectionInvalidateEvent.class,
////                    InstanceTickEvent.class,
//                    InstanceUnregisterEvent.class,
//                    RemoveEntityFromInstanceEvent.class,
////                    CreativeInventoryActionEvent.class,
//                    InventoryClickEvent.class,
////                    InventoryCloseEvent.class,
                    InventoryItemChangeEvent.class,
////                    InventoryOpenEvent.class,
//                    InventoryPreClickEvent.class,
//                    EntityEquipEvent.class,
//                    ItemDropEvent.class,
//                    PickupExperienceEvent.class,
//                    PickupItemEvent.class,
                    PlayerBeginItemUseEvent.class,
                    PlayerCancelItemUseEvent.class,
                    PlayerFinishItemUseEvent.class
//                    AdvancementTabEvent.class,
//                    AsyncPlayerConfigurationEvent.class,
//                    AsyncPlayerPreLoginEvent.class,
//                    PlayerAnvilInputEvent.class,
//                    PlayerBlockBreakEvent.class,
//                    PlayerBlockInteractEvent.class,
//                    PlayerBlockPlaceEvent.class,
//                    PlayerCancelDiggingEvent.class
//                    PlayerChangeHeldSlotEvent.class,
//                    PlayerChatEvent.class,
//                    PlayerChunkLoadEvent.class,
//                    PlayerChunkUnloadEvent.class,
//                    PlayerCommandEvent.class,
//                    PlayerConfigCustomClickEvent.class,
//                    PlayerCustomClickEvent.class,
//                    PlayerDeathEvent.class,
//                    PlayerDebugSubscriptionsRequestEvent.class,
//                    PlayerDisconnectEvent.class,
//                    PlayerEditSignEvent.class,
//                    PlayerEntityInteractEvent.class,
//                    PlayerFinishDiggingEvent.class,
//                    PlayerGameModeChangeEvent.class,
//                    PlayerGameModeRequestEvent.class,
//                    PlayerHandAnimationEvent.class,
//                    PlayerLeaveBedEvent.class,
//                    PlayerLoadedEvent.class,
//                    PlayerMoveEvent.class,
//                    PlayerPacketEvent.class,
//                    PlayerPacketOutEvent.class,
//                    PlayerPickBlockEvent.class,
//                    PlayerPickEntityEvent.class,
//                    PlayerPluginMessageEvent.class,
//                    PlayerPreEatEvent.class,
//                    PlayerResourcePackStatusEvent.class,
//                    PlayerRespawnEvent.class,
//                    PlayerSettingsChangeEvent.class,
//                    PlayerSkinInitEvent.class,
//                    PlayerSpawnEvent.class,
//                    PlayerSpectateEvent.class,
//                    PlayerStartDiggingEvent.class
//                    PlayerStartFlyingEvent.class,
//                    PlayerStartFlyingWithElytraEvent.class,
//                    PlayerStartSneakingEvent.class,
//                    PlayerStartSprintingEvent.class,
//                    PlayerStopFlyingEvent.class,
//                    PlayerStopFlyingWithElytraEvent.class,
//                    PlayerStopSneakingEvent.class,
//                    PlayerStopSprintingEvent.class,
//                    PlayerSwapItemEvent.class
//                    PlayerTickEndEvent.class,
//                    PlayerTickEvent.class,
//                    PlayerUseItemEvent.class
//                    PlayerUseItemOnBlockEvent.class,
//                    ClientPingServerEvent.class,
//                    ServerListPingEvent.class,
//                    ServerTickMonitorEvent.class,
//                    AsyncEvent.class,
//                    BlockEvent.class,
//                    CancellableEvent.class,
//                    EntityEvent.class,
//                    EntityInstanceEvent.class,
//                    InstanceEvent.class,
//                    InventoryEvent.class,
//                    ItemEvent.class,
//                    PlayerEvent.class,
//                    PlayerInstanceEvent.class
//                    RecursiveEvent.class
            ).forEach( eventClass ->
                    MinecraftServer.getGlobalEventHandler().addListener(eventClass, event -> {
                            player.sendMessage(eventClass.getSimpleName());
//                            player.sendMessage((new Gson()).toJson(event));
////                        if (event instanceof PlayerPacketEvent pEvent) {
////                            player.sendMessage(pEvent.getClass().getSimpleName() +  ": " + pEvent.getPacket().toString());
////                        }
//                    if (event instanceof PlayerCancelItemUseEvent cevent) {
////                            player.sendMessage(event.getClass().getSimpleName() + ": " + event);
//                        }
//                    if (event instanceof PlayerCancelDiggingEvent devent) {
//                            player.sendMessage(event.getClass().getSimpleName() + ": " + event.toString());
//                    }
                    })
            );
        });
    }
}
