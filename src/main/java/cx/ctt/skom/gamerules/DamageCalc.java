package cx.ctt.skom.gamerules;

import cx.ctt.skom.Gamerule;
import cx.ctt.skom.Main;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.*;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.projectile.AbstractArrowMeta;
import net.minestom.server.entity.metadata.projectile.ProjectileMeta;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.entity.EntityTickEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.TimedPotion;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.tag.Tag;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

class DamageCalcValues {

    // ========== MELEE ATTACK ==========
    float fistDamage = 1.0f;
    float criticalHitMultiplier = 1.5f;
    float criticalHitMinCooldown = 0.848f;

    // ========== STRENGTH / WEAKNESS ==========
    float strengthModernPerLevel = 3.0f;
    float strengthLegacyMultiplierPerLevel = 1.3f;
    float weaknessModernPerLevel = -4.0f;
    float weaknessLegacyPerLevel = -0.5f;

    // ========== ENCHANTMENT DAMAGE BONUS ==========
    float sharpnessLegacyPerLevel = 1.25f;
    float sharpnessModernBase = 1.0f;
    float sharpnessModernPerLevel = 0.5f;
    float smitePerLevel = 2.5f;
    float banePerLevel = 2.5f;

    // ========== ARMOR ==========
    float armorDivisor = 25.0f;
    float maxEffectiveArmor = 20.0f;
    float toughnessDivisor = 4.0f;
    float armorBaseDivisor = 2.0f;

    // ========== PROTECTION ENCHANTMENTS (EPF per level) ==========
    int protectionEPF = 1;
    int fireProtectionEPF = 2;
    int featherFallingEPF = 3;
    int blastProtectionEPF = 2;
    int projectileProtectionEPF = 2;
    int maxEPF = 20;

    // ========== RESISTANCE POTION ==========
    float resistancePerLevel = 0.2f;

    // ========== INVULNERABILITY ==========
    int invulnerabilityTicks = 10;

    // ========== FISHING BOBBER ==========
    float fishingBobberDamage = 0.0f;

    // ========== FIRE ASPECT ==========
    int fireAspectTicksPerLevel = 80;

    // ========== THORNS ==========
    float thornsChancePerLevel = 0.15f;
    float thornsMinDamage = 1.0f;
    float thornsMaxDamage = 4.0f;
    int thornsHighLevelThreshold = 10;

    // ========== PROJECTILE DAMAGE ==========
    float arrowBaseDamage = 2.0f;
    int arrowCriticalBonusBase = 2;
    float powerPerLevel = 0.5f;
    float powerBase = 0.5f;
    int flameFireTicks = 100;
    float thrownTridentDamage = 8.0f;

    // ========== WEAPON BASE DAMAGE ==========
    Map<Material, Float> weaponDamage = new HashMap<>();

    {
        weaponDamage.put(Material.WOODEN_SWORD, 4.0f);
        weaponDamage.put(Material.STONE_SWORD, 5.0f);
        weaponDamage.put(Material.IRON_SWORD, 6.0f);
        weaponDamage.put(Material.GOLDEN_SWORD, 4.0f);
        weaponDamage.put(Material.DIAMOND_SWORD, 7.0f);
        weaponDamage.put(Material.NETHERITE_SWORD, 8.0f);

        weaponDamage.put(Material.WOODEN_AXE, 7.0f);
        weaponDamage.put(Material.STONE_AXE, 9.0f);
        weaponDamage.put(Material.IRON_AXE, 9.0f);
        weaponDamage.put(Material.GOLDEN_AXE, 7.0f);
        weaponDamage.put(Material.DIAMOND_AXE, 9.0f);
        weaponDamage.put(Material.NETHERITE_AXE, 10.0f);

        weaponDamage.put(Material.WOODEN_PICKAXE, 2.0f);
        weaponDamage.put(Material.STONE_PICKAXE, 3.0f);
        weaponDamage.put(Material.IRON_PICKAXE, 4.0f);
        weaponDamage.put(Material.GOLDEN_PICKAXE, 2.0f);
        weaponDamage.put(Material.DIAMOND_PICKAXE, 5.0f);
        weaponDamage.put(Material.NETHERITE_PICKAXE, 6.0f);

        weaponDamage.put(Material.WOODEN_SHOVEL, 2.5f);
        weaponDamage.put(Material.STONE_SHOVEL, 3.5f);
        weaponDamage.put(Material.IRON_SHOVEL, 4.5f);
        weaponDamage.put(Material.GOLDEN_SHOVEL, 2.5f);
        weaponDamage.put(Material.DIAMOND_SHOVEL, 5.5f);
        weaponDamage.put(Material.NETHERITE_SHOVEL, 6.5f);

        weaponDamage.put(Material.TRIDENT, 9.0f);

        weaponDamage.put(Material.WOODEN_HOE, 1.0f);
        weaponDamage.put(Material.STONE_HOE, 1.0f);
        weaponDamage.put(Material.IRON_HOE, 1.0f);
        weaponDamage.put(Material.GOLDEN_HOE, 1.0f);
        weaponDamage.put(Material.DIAMOND_HOE, 1.0f);
        weaponDamage.put(Material.NETHERITE_HOE, 1.0f);
    }

    // ========== ENTITY GROUPS (for Smite / Bane of Arthropods) ==========
    Set<EntityType> undeadMobs = Set.of(
            EntityType.SKELETON, EntityType.WITHER_SKELETON, EntityType.STRAY,
            EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.DROWNED,
            EntityType.HUSK, EntityType.ZOGLIN, EntityType.PHANTOM,
            EntityType.ZOMBIE_HORSE, EntityType.SKELETON_HORSE, EntityType.WITHER,
            EntityType.ZOMBIFIED_PIGLIN, EntityType.BOGGED
    );

    Set<EntityType> arthropodMobs = Set.of(
            EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.BEE,
            EntityType.SILVERFISH, EntityType.ENDERMITE
    );
}

public class DamageCalc implements Gamerule {

    private boolean legacy;
    final DamageCalcValues v = new DamageCalcValues();

    private static final Tag<Double> FALL_DISTANCE = Tag.Transient("dmgCalc.fallDistance");
    private static final Tag<Long> LAST_DAMAGE_TICK = Tag.Transient("dmgCalc.lastDamageTick");
    private static final Tag<Float> LAST_DAMAGE_AMOUNT = Tag.Transient("dmgCalc.lastDamageAmount");
    private static final Tag<Long> LAST_ATTACK_MS = Tag.Transient("dmgCalc.lastAttackMs");

    @Override
    public void startListening(Instance instance, @Nullable String dbKey) {
        String mode = dbKey != null ? Main.JEDIS.hget(dbKey, "DamageCalc") : null;
        this.legacy = "legacy".equalsIgnoreCase(mode);

        //TODO: this should not be here, making the assumption old damage = no sword cooldown for now
        if (this.legacy){
            instance.eventNode().addListener(PlayerSpawnEvent.class, event ->
                event.getPlayer().getAttribute(Attribute.ATTACK_SPEED).setBaseValue(1024.0));
        }

        instance.eventNode().addListener(EntityAttackEvent.class, this::onAttack);
        instance.eventNode().addListener(EntityDamageEvent.class, this::onDamage);
        instance.eventNode().addListener(ProjectileCollideWithEntityEvent.class, this::onProjectileHit);
        instance.eventNode().addListener(PlayerMoveEvent.class, this::onPlayerMove);
        instance.eventNode().addListener(EntityTickEvent.class, this::onEntityTick);
        instance.eventNode().addListener(EntityDamageEvent.class, event -> {
            if (event.getEntity() instanceof Player p) {
                Damage d = event.getDamage();
                p.sendMessage("You took " + d.getAmount() + " of cause " + d.getType().name() );
            }
        });
    }

    // =============================================
    // OFFENSIVE: melee damage calculation
    // =============================================

    private void onAttack(EntityAttackEvent event) {
        if (!(event.getEntity() instanceof LivingEntity attacker)) return;
        if (!(event.getTarget() instanceof LivingEntity target)) return;
        if (target.isDead() || target.isInvulnerable()) return;
        if (attacker instanceof Player p && p.getGameMode() == GameMode.SPECTATOR) return;
        if (target instanceof Player p && (p.getGameMode() == GameMode.SPECTATOR
                || p.getGameMode() == GameMode.CREATIVE)) return;

        float damage = computeMeleeDamage(attacker, target);
        if (damage <= 0) return;

        RegistryKey<DamageType> type = attacker instanceof Player
                ? DamageType.PLAYER_ATTACK : DamageType.MOB_ATTACK;
        target.damage(new Damage(type, attacker, attacker, null, damage));

        applyPostAttackEffects(attacker, target);
    }

    private float computeMeleeDamage(LivingEntity attacker, LivingEntity target) {
        float base;

        if (attacker instanceof Player player) {
            base = weaponDamage(player.getItemInMainHand());
            if (!legacy) {
                float cooldown = attackCooldownProgress(player);
                base *= 0.2f + cooldown * cooldown * 0.8f;
            }
            player.setTag(LAST_ATTACK_MS, System.currentTimeMillis());
        } else {
            base = (float) attacker.getAttributeValue(Attribute.ATTACK_DAMAGE);
        }

        base = applyStrengthWeakness(attacker, base);

        if (isCritical(attacker)) {
            base *= v.criticalHitMultiplier;
        }

        base += enchantmentBonusDamage(attacker, target);
        return Math.max(0, base);
    }

    private float weaponDamage(ItemStack item) {
        if (item.isAir()) return v.fistDamage;
        Float dmg = v.weaponDamage.get(item.material());
        return dmg != null ? dmg : v.fistDamage;
    }

    private float applyStrengthWeakness(LivingEntity entity, float base) {
        TimedPotion strength = entity.getEffect(PotionEffect.STRENGTH);
        if (strength != null) {
            int amp = strength.potion().amplifier() + 1;
            if (legacy) {
                base *= 1.0f + v.strengthLegacyMultiplierPerLevel * amp;
            } else {
                base += v.strengthModernPerLevel * amp;
            }
        }

        TimedPotion weakness = entity.getEffect(PotionEffect.WEAKNESS);
        if (weakness != null) {
            int amp = weakness.potion().amplifier() + 1;
            base += (legacy ? v.weaknessLegacyPerLevel : v.weaknessModernPerLevel) * amp;
        }

        return base;
    }

    private boolean isCritical(LivingEntity attacker) {
        if (!(attacker instanceof Player player)) return false;
        if (player.isOnGround()) return false;

        double fall = player.hasTag(FALL_DISTANCE) ? player.getTag(FALL_DISTANCE) : 0;
        if (fall <= 0) return false;
        if (player.hasEffect(PotionEffect.BLINDNESS)) return false;
        if (player.getVehicle() != null) return false;

        if (!legacy) {
            if (player.isSprinting()) return false;
            if (attackCooldownProgress(player) < v.criticalHitMinCooldown) return false;
        }
        return true;
    }

    private float enchantmentBonusDamage(LivingEntity attacker, LivingEntity target) {
        ItemStack weapon = attacker.getItemInMainHand();
        if (weapon.isAir()) return 0;
        EnchantmentList enc = weapon.get(DataComponents.ENCHANTMENTS);
        if (enc == null) return 0;

        float bonus = 0;

        int sharpness = enc.level(Enchantment.SHARPNESS);
        if (sharpness > 0) {
            bonus += legacy
                    ? sharpness * v.sharpnessLegacyPerLevel
                    : v.sharpnessModernBase + Math.max(0, sharpness - 1) * v.sharpnessModernPerLevel;
        }

        int smite = enc.level(Enchantment.SMITE);
        if (smite > 0 && v.undeadMobs.contains(target.getEntityType())) {
            bonus += smite * v.smitePerLevel;
        }

        int bane = enc.level(Enchantment.BANE_OF_ARTHROPODS);
        if (bane > 0 && v.arthropodMobs.contains(target.getEntityType())) {
            bonus += bane * v.banePerLevel;
        }

        return bonus;
    }

    private void applyPostAttackEffects(LivingEntity attacker, LivingEntity target) {
        ItemStack weapon = attacker.getItemInMainHand();
        if (weapon.isAir()) return;
        EnchantmentList enc = weapon.get(DataComponents.ENCHANTMENTS);
        if (enc == null) return;

        int fireAspect = enc.level(Enchantment.FIRE_ASPECT);
        if (fireAspect > 0) {
            target.setFireTicks(fireAspect * v.fireAspectTicksPerLevel);
        }

        int bane = enc.level(Enchantment.BANE_OF_ARTHROPODS);
        if (bane > 0 && v.arthropodMobs.contains(target.getEntityType())) {
            int ticks = 20 + ThreadLocalRandom.current().nextInt(10 * bane);
            target.addEffect(new Potion(PotionEffect.SLOWNESS, 3, ticks));
        }
    }

    private float attackCooldownProgress(Player player) {
        if (legacy) return 1.0f;
        if (!player.hasTag(LAST_ATTACK_MS)) return 1.0f;

        long elapsed = System.currentTimeMillis() - player.getTag(LAST_ATTACK_MS);
        double attackSpeed = player.getAttributeValue(Attribute.ATTACK_SPEED);
        double cooldownMs = (1.0 / attackSpeed) * 20.0 * 50.0;
        return (float) Math.min(1.0, Math.max(0, elapsed / cooldownMs));
    }

    // =============================================
    // PROJECTILE: bow, crossbow, trident
    // =============================================

    private void onProjectileHit(ProjectileCollideWithEntityEvent event) {
        Entity projectile = event.getEntity();
        EntityType pType = projectile.getEntityType();
        if (pType != EntityType.ARROW && pType != EntityType.SPECTRAL_ARROW
                && pType != EntityType.TRIDENT) return;
        if (!(event.getTarget() instanceof LivingEntity target)) return;
        if (target.isDead() || target.isInvulnerable()) return;
        if (target instanceof Player p && (p.getGameMode() == GameMode.SPECTATOR
                || p.getGameMode() == GameMode.CREATIVE)) return;

        Entity shooter = resolveShooter(projectile);

        RegistryKey<DamageType> damageType = pType == EntityType.TRIDENT
                ? DamageType.TRIDENT : DamageType.ARROW;

        float damage;
        if (pType == EntityType.TRIDENT) {
            damage = v.thrownTridentDamage;
        } else {
            damage = computeArrowDamage(projectile, shooter);
        }

        if (damage <= 0) return;
        target.damage(new Damage(damageType, projectile, shooter, null, damage));

        // Flame: set target on fire if arrow is from a Flame-enchanted bow
        if (pType != EntityType.TRIDENT && shooter instanceof LivingEntity livingShooter) {
            ItemStack bow = livingShooter.getItemInMainHand();
            if (!bow.isAir()) {
                EnchantmentList enc = bow.get(DataComponents.ENCHANTMENTS);
                if (enc != null && enc.level(Enchantment.FLAME) > 0) {
                    target.setFireTicks(v.flameFireTicks);
                }
            }
        }
    }

    private float computeArrowDamage(Entity projectile, Entity shooter) {
        double velocity = projectile.getVelocity().length() / 20.0;
        float baseDamage = v.arrowBaseDamage;

        // Power enchantment from shooter's weapon
        if (shooter instanceof LivingEntity livingShooter) {
            ItemStack weapon = livingShooter.getItemInMainHand();
            if (!weapon.isAir()) {
                EnchantmentList enc = weapon.get(DataComponents.ENCHANTMENTS);
                if (enc != null) {
                    int power = enc.level(Enchantment.POWER);
                    if (power > 0) {
                        baseDamage += power * v.powerPerLevel + v.powerBase;
                    }
                }
            }
        }

        float damage = (float) Math.ceil(velocity * baseDamage);

        boolean critical = false;
        if (projectile.getEntityMeta() instanceof AbstractArrowMeta arrowMeta) {
            critical = arrowMeta.isCritical();
        }
        if (critical) {
            damage += ThreadLocalRandom.current().nextInt((int) (damage / 2) + v.arrowCriticalBonusBase);
        }

        return Math.max(0, damage);
    }

    private Entity resolveShooter(Entity projectile) {
        if (projectile.getEntityMeta() instanceof ProjectileMeta meta) {
            return meta.getShooter();
        }
        if (projectile instanceof EntityProjectile ep) {
            return ep.getShooter();
        }
        return null;
    }

    // =============================================
    // DEFENSIVE: armor, enchants, potions, invuln
    // =============================================

    private void onDamage(EntityDamageEvent event) {
        LivingEntity entity = event.getEntity();
        Damage damage = event.getDamage();
        float amount = damage.getAmount();
        if (amount <= 0) return;

        RegistryKey<DamageType> type = damage.getType();

        // Fishing bobber override
        if (damage.getSource() != null
                && damage.getSource().getEntityType() == EntityType.FISHING_BOBBER) {
            if (v.fishingBobberDamage <= 0) {
                event.setCancelled(true);
                return;
            }
            amount = v.fishingBobberDamage;
        }

        // Fire resistance cancels fire damage
        if (isFireDamage(type) && entity.hasEffect(PotionEffect.FIRE_RESISTANCE)) {
            event.setCancelled(true);
            return;
        }

        // Invulnerability frames
        if (entity.hasTag(LAST_DAMAGE_TICK)) {
            long ticksSinceLast = entity.getAliveTicks() - entity.getTag(LAST_DAMAGE_TICK);
            if (ticksSinceLast < v.invulnerabilityTicks) {
                float lastAmount = entity.hasTag(LAST_DAMAGE_AMOUNT)
                        ? entity.getTag(LAST_DAMAGE_AMOUNT) : 0;
                if (amount <= lastAmount) {
                    event.setCancelled(true);
                    return;
                }
                amount -= lastAmount;
            }
        }

        float preDefenseAmount = amount;

        // Armor reduction
        if (!bypassesArmor(type)) {
            amount = applyArmor(entity, amount);
        }

        // Resistance potion
        if (!bypassesResistance(type)) {
            TimedPotion resistance = entity.getEffect(PotionEffect.RESISTANCE);
            if (resistance != null) {
                int amp = resistance.potion().amplifier() + 1;
                amount *= Math.max(0, 1.0f - amp * v.resistancePerLevel);
            }
        }

        // Enchantment protection
        if (!bypassesEnchantments(type)) {
            amount = applyProtection(entity, type, amount);
        }

        amount = Math.max(0, amount);
        damage.setAmount(amount);

        // Track invulnerability using pre-defense amount
        entity.setTag(LAST_DAMAGE_TICK, entity.getAliveTicks());
        entity.setTag(LAST_DAMAGE_AMOUNT, preDefenseAmount);

        // Thorns
        if ((type == DamageType.PLAYER_ATTACK || type == DamageType.MOB_ATTACK)
                && damage.getAttacker() instanceof LivingEntity attacker) {
            applyThorns(entity, attacker);
        }
    }

    private float applyArmor(LivingEntity entity, float damage) {
        double armor = entity.getAttributeValue(Attribute.ARMOR);
        if (armor <= 0) return damage;

        if (legacy) {
            return damage * (v.armorDivisor - (float) armor) / v.armorDivisor;
        }

        double toughness = entity.getAttributeValue(Attribute.ARMOR_TOUGHNESS);
        float f = v.armorBaseDivisor + (float) toughness / v.toughnessDivisor;
        float effective = Math.max((float) armor * 0.2f, (float) armor - damage / f);
        effective = Math.min(effective, v.maxEffectiveArmor);
        return damage * (1.0f - effective / v.armorDivisor);
    }

    private float applyProtection(LivingEntity entity, RegistryKey<DamageType> type, float damage) {
        int epf = 0;

        for (EquipmentSlot slot : EquipmentSlot.armors()) {
            ItemStack armor = entity.getEquipment(slot);
            if (armor.isAir()) continue;
            EnchantmentList enc = armor.get(DataComponents.ENCHANTMENTS);
            if (enc == null) continue;

            epf += enc.level(Enchantment.PROTECTION) * v.protectionEPF;

            if (isFireDamage(type))
                epf += enc.level(Enchantment.FIRE_PROTECTION) * v.fireProtectionEPF;
            if (isProjectileDamage(type))
                epf += enc.level(Enchantment.PROJECTILE_PROTECTION) * v.projectileProtectionEPF;
            if (isExplosionDamage(type))
                epf += enc.level(Enchantment.BLAST_PROTECTION) * v.blastProtectionEPF;
            if (isFallDamage(type))
                epf += enc.level(Enchantment.FEATHER_FALLING) * v.featherFallingEPF;
        }

        if (epf <= 0) return damage;

        if (legacy) {
            epf = Math.min(epf, v.maxEPF);
            return damage * (v.armorDivisor - epf) / v.armorDivisor;
        }

        float clamped = Math.min(epf, v.maxEPF);
        return damage * (1.0f - clamped / v.armorDivisor);
    }

    private void applyThorns(LivingEntity defender, LivingEntity attacker) {
        for (EquipmentSlot slot : EquipmentSlot.armors()) {
            ItemStack armor = defender.getEquipment(slot);
            if (armor.isAir()) continue;
            EnchantmentList enc = armor.get(DataComponents.ENCHANTMENTS);
            if (enc == null) continue;

            int thorns = enc.level(Enchantment.THORNS);
            if (thorns <= 0) continue;

            if (ThreadLocalRandom.current().nextFloat() < thorns * v.thornsChancePerLevel) {
                float dmg = thorns > v.thornsHighLevelThreshold
                        ? thorns - v.thornsHighLevelThreshold
                        : v.thornsMinDamage + ThreadLocalRandom.current().nextFloat()
                                              * (v.thornsMaxDamage - v.thornsMinDamage);
                attacker.damage(DamageType.THORNS, dmg);
                return;
            }
        }
    }

    // =============================================
    // FALL DAMAGE
    // =============================================

    private void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        double dy = event.getNewPosition().y() - player.getPosition().y();
        double fallDist = player.hasTag(FALL_DISTANCE) ? player.getTag(FALL_DISTANCE) : 0;

        if (player.isFlying()
                || player.hasEffect(PotionEffect.LEVITATION)
                || player.hasEffect(PotionEffect.SLOW_FALLING)
                || dy > 0) {
            player.setTag(FALL_DISTANCE, 0.0);
            return;
        }

        if (!event.isOnGround()) {
            if (dy < 0) player.setTag(FALL_DISTANCE, fallDist - dy);
            return;
        }

        // Landed
        player.setTag(FALL_DISTANCE, 0.0);
        if (player.getGameMode() == GameMode.CREATIVE
                || player.getGameMode() == GameMode.SPECTATOR) return;

        double safeFall = player.getAttributeValue(Attribute.SAFE_FALL_DISTANCE);
        if (fallDist > safeFall) {
            int dmg = (int) Math.ceil(
                    (fallDist - safeFall) * player.getAttributeValue(Attribute.FALL_DAMAGE_MULTIPLIER));
            if (dmg > 0) player.damage(DamageType.FALL, dmg);
        }
    }

    private void onEntityTick(EntityTickEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity instanceof Player) return;

        Pos prev = entity.getPreviousPosition();
        Pos cur = entity.getPosition();
        double dy = cur.y() - prev.y();
        double fallDist = entity.hasTag(FALL_DISTANCE) ? entity.getTag(FALL_DISTANCE) : 0;

        if (entity.hasEffect(PotionEffect.SLOW_FALLING) || dy > 0) {
            entity.setTag(FALL_DISTANCE, 0.0);
            return;
        }

        if (!entity.isOnGround()) {
            if (dy < 0) entity.setTag(FALL_DISTANCE, fallDist - dy);
            return;
        }

        entity.setTag(FALL_DISTANCE, 0.0);
        double safeFall = entity.getAttributeValue(Attribute.SAFE_FALL_DISTANCE);
        if (fallDist > safeFall) {
            int dmg = (int) Math.ceil(
                    (fallDist - safeFall) * entity.getAttributeValue(Attribute.FALL_DAMAGE_MULTIPLIER));
            if (dmg > 0) entity.damage(DamageType.FALL, dmg);
        }
    }

    // =============================================
    // DAMAGE TYPE CLASSIFICATION
    // =============================================

    private static boolean bypassesArmor(RegistryKey<DamageType> t) {
        return t == DamageType.OUT_OF_WORLD || t == DamageType.STARVE
                || t == DamageType.MAGIC || t == DamageType.WITHER
                || t == DamageType.SONIC_BOOM || t == DamageType.DROWN
                || t == DamageType.IN_WALL || t == DamageType.CRAMMING
                || t == DamageType.ON_FIRE || t == DamageType.FLY_INTO_WALL
                || t == DamageType.GENERIC_KILL || t == DamageType.THORNS;
    }

    private static boolean bypassesResistance(RegistryKey<DamageType> t) {
        return t == DamageType.OUT_OF_WORLD || t == DamageType.GENERIC_KILL;
    }

    private static boolean bypassesEnchantments(RegistryKey<DamageType> t) {
        return t == DamageType.SONIC_BOOM || t == DamageType.OUT_OF_WORLD
                || t == DamageType.GENERIC_KILL;
    }

    private static boolean isFireDamage(RegistryKey<DamageType> t) {
        return t == DamageType.IN_FIRE || t == DamageType.ON_FIRE
                || t == DamageType.LAVA || t == DamageType.HOT_FLOOR;
    }

    private static boolean isProjectileDamage(RegistryKey<DamageType> t) {
        return t == DamageType.ARROW || t == DamageType.TRIDENT
                || t == DamageType.FIREBALL || t == DamageType.WITHER_SKULL;
    }

    private static boolean isExplosionDamage(RegistryKey<DamageType> t) {
        return t == DamageType.EXPLOSION || t == DamageType.PLAYER_EXPLOSION;
    }

    private static boolean isFallDamage(RegistryKey<DamageType> t) {
        return t == DamageType.FALL || t == DamageType.STALAGMITE;
    }
}
