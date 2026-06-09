package cx.ctt.skom.gamerules;

import cx.ctt.skom.Gamerule;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.metadata.avatar.PlayerMeta;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.UpdateViewDistancePacket;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class ResetPlayer implements Gamerule {

    @Override
    public void startListening(Instance instance, @Nullable String dbKey) {

        instance.eventNode().addListener(PlayerSpawnEvent.class, event -> {
            var player = event.getPlayer();
            for (final var attributeInstance : player.getAttributes()) {
//                attributeInstance.setBaseValue(attributeInstance.attribute().defaultValue());
                attributeInstance.clearModifiers();
            }
            player.editEntityMeta(PlayerMeta.class, meta -> {
                meta.setArrowCount(0);
                meta.setBeeStingerCount(0);
                meta.setScore(0);
                meta.setFlyingWithElytra(false);
                meta.setInvisible(false);
                meta.setTickFrozen(0);
                meta.setAdditionalHearts(0);
                meta.setBeeStingerCount(0);
                meta.setEffectParticles(List.of());
                meta.setPotionEffectAmbient(false);
            });
            player.clearEffects();
            player.setBoundingBox(0.6, 1.8, 0.6);
            player.setVelocity(new Vec(0, 0, 0));
            player.clearTitle();
            player.sendActionBar(Component.empty());
            player.getInventory().clear();
            player.setHealth(20f);
            player.setFood(20);
            player.setFireTicks(0);
            player.setGlowing(false);
            player.setAllowFlying(false);
            player.setFlying(false);
            player.setArrowCount(0);
            player.setLevel(0);
            player.setExp(0);
            player.stopSpectating();
            player.tagHandler().updateContent(CompoundBinaryTag.empty());
            player.sendPacket(new UpdateViewDistancePacket(player.getSettings().viewDistance()));
//            player.setTabListVisibilityRule((_, _) -> false);
            player.setTeam(null);
            player.setDisplayName(null);
            MinecraftServer.getBossBarManager().getPlayerBossBars(player).forEach(player::hideBossBar);
        });
    }
}
