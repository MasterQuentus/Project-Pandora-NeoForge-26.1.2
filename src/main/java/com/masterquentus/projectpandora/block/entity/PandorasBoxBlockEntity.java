package com.masterquentus.projectpandora.block.entity;

import com.masterquentus.projectpandora.block.custom.PandorasBox;
import com.masterquentus.projectpandora.entity.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LightningBolt;

public class PandorasBoxBlockEntity extends BlockEntity {

    // Dynamic pools (kept but unused for now)
    private static List<Item> COMMON_POOL = new ArrayList<>();
    private static List<Item> UNCOMMON_POOL = new ArrayList<>();
    private static List<Item> RARE_POOL = new ArrayList<>();
    private static List<Item> LEGENDARY_POOL = new ArrayList<>();
    private static boolean poolsInitialized = false;

    // Hardcoded loot tables (replace with real item IDs later if needed)
    private static final List<String> COMMON_CHEST_LOOT = List.of(
            "minecraft:iron_ingot", "minecraft:gold_ingot", "minecraft:coal", "minecraft:arrow"
    );
    private static final List<String> UNCOMMON_CHEST_LOOT = List.of(
            "minecraft:diamond", "minecraft:emerald", "minecraft:golden_apple", "minecraft:experience_bottle"
    );
    private static final List<String> RARE_CHEST_LOOT = List.of(
            "minecraft:netherite_scrap", "minecraft:enchanted_golden_apple", "minecraft:totem_of_undying"
    );
    private static final List<String> LEGENDARY_CHEST_LOOT = List.of(
            "minecraft:netherite_ingot", "minecraft:elytra", "minecraft:nether_star"
    );

    public PandorasBoxBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }

    public static NonNullList<ItemStack> generateRandomChestLoot(RandomSource random) {
        List<Item> allItems = new ArrayList<>(BuiltInRegistries.ITEM.stream().toList());

        if (allItems.isEmpty()) {
            return NonNullList.withSize(1, new ItemStack(Items.ROTTEN_FLESH));
        }

        int rollCount = random.nextInt(5) + 3;
        NonNullList<ItemStack> chestInventory = NonNullList.withSize(27, ItemStack.EMPTY);

        for (int i = 0; i < rollCount; i++) {
            Item randomItem = allItems.get(random.nextInt(allItems.size()));
            ItemStack stack = new ItemStack(randomItem);
            int maxStack = stack.getMaxStackSize();
            int count = random.nextInt(Math.min(maxStack, 16)) + 1;
            stack.setCount(count);

            int slot;
            do {
                slot = random.nextInt(chestInventory.size());
            } while (!chestInventory.get(slot).isEmpty());

            chestInventory.set(slot, stack);
        }
        return chestInventory;
    }

    public NonNullList<ItemStack> inventory = NonNullList.withSize(27, ItemStack.EMPTY);

    private int animationTicks = 0;
    public boolean isOpening = false;

    private int currentRound = 0;
    private int totalRounds = 5 + new Random().nextInt(6);

    private int roundCooldown = 100;

    private long cooldownEndTime = 0;
    private UUID activePlayerUUID = null;

    private boolean warnedAboutMobs = false;

    private final ServerBossEvent bossBar = new ServerBossEvent(
            UUID.randomUUID(),
            Component.literal("Pandora's Box Event"),
            BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.PROGRESS
    );

    public PandorasBoxBlockEntity(BlockPos pos, BlockState state) {
        super(ModEntities.PANDORAS_BOX_ENTITY.get(), pos, state);
    }

    public void activatePandorasBox(Level level, BlockPos pos) {
        // empty on purpose
    }

    public void startOpeningAnimation(Player player) {
        if (isOpening) return;

        if (!player.isCreative() && level.getDifficulty() == Difficulty.PEACEFUL) {
            player.sendSystemMessage(Component.literal("❌ Pandora’s Box cannot be used in Peaceful mode!")
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
            return;
        }

        if (!player.isCreative() && System.currentTimeMillis() < cooldownEndTime) {
            long remainingTime = (cooldownEndTime - System.currentTimeMillis()) / 1000;
            player.sendSystemMessage(Component.literal("⏳ Pandora’s Box is still recharging! Try again in " + remainingTime + " seconds.")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            return;
        }

        isOpening = true;
        animationTicks = 0;
        setCooldown();
        activePlayerUUID = player.getUUID();
        setChanged();

        spawnOpeningParticles();
        broadcastMessage("⚡ Pandora's Box has been opened! The darkness spreads...", ChatFormatting.DARK_PURPLE);

        activatePandorasBox(level, worldPosition);

        // 20% chance for good event
        if (new Random().nextDouble() < 0.20) {
            triggerGoodEvent();
            return;
        }

        currentRound = 0;
        totalRounds = 3 + new Random().nextInt(5);
        roundCooldown = 100;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        UUID playerUUID = this.getActivePlayerUUID();
        Player player = (playerUUID != null && this.level != null)
                ? this.level.getPlayerByUUID(playerUUID)
                : null;

        this.failEvent(player);

        super.preRemoveSideEffects(pos, state);
    }

    private void triggerGoodEvent() {
        if (level == null || level.isClientSide()) return;

        Random random = new Random();
        int eventRoll = random.nextInt(4);

        switch (eventRoll) {
            case 0 -> {
                spawnTieredRewardChest();
                broadcastMessage("🎁 A treasure chest appears!", ChatFormatting.GOLD);
            }
            case 1 -> {
                applyPositiveEffects();
                broadcastMessage("✨ You feel a surge of power!", ChatFormatting.AQUA);
            }
            case 2 -> {
                grantXPReward();
                broadcastMessage("🟢 You gain mystical knowledge!", ChatFormatting.GREEN);
            }
            case 3 -> {
                spawnLuckyDrop();
                broadcastMessage("💎 Pandora’s Box grants you riches!", ChatFormatting.BLUE);
            }
        }

        givePlayerReward();
        setCooldown();

        this.isOpening = false;
        BlockState state = level.getBlockState(worldPosition);

        if (state.hasProperty(PandorasBox.OPEN)) {
            level.setBlock(worldPosition, state.setValue(PandorasBox.OPEN, false), 3);
            level.playSound(null, worldPosition, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 1.0F, 0.8F);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.POOF,
                        worldPosition.getX() + 0.5,
                        worldPosition.getY() + 0.5,
                        worldPosition.getZ() + 0.5,
                        1, 0, 0, 0, 0
                );
            }
        }

        this.bossBar.removeAllPlayers();
        setChanged();
    }

    private void applyPositiveEffects() {
        Player player = level.getNearestPlayer(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 10, false);
        if (player == null) return;

        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 1));
        player.addEffect(new MobEffectInstance(MobEffects.INSTANT_DAMAGE, 600, 0));
        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 600, 1));
    }

    private void grantXPReward() {
        Player player = level.getNearestPlayer(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 10, false);
        if (player == null) return;
        player.giveExperiencePoints(500 + new Random().nextInt(500));
    }

    private void spawnLuckyDrop() {
        Player player = level.getNearestPlayer(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 10, false);
        if (player == null) return;

        Random random = new Random();
        ItemStack drop = random.nextBoolean()
                ? new ItemStack(Items.DIAMOND, 2 + random.nextInt(3))
                : new ItemStack(Items.EMERALD, 3 + random.nextInt(4));

        level.addFreshEntity(new ItemEntity(level, player.getX(), player.getY() + 1, player.getZ(), drop));
    }

    private void setCooldown() {
        int cooldownSeconds = 60;
        cooldownEndTime = System.currentTimeMillis() + (cooldownSeconds * 1000L);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PandorasBoxBlockEntity entity) {
        if (!entity.isOpening) return;

        if (level.isClientSide()) {
            if (level.getRandom().nextFloat() < 0.2f) {
                level.addParticle(ParticleTypes.WITCH, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 0, 0.05, 0);
            }
            return;
        }

        if (state.hasProperty(PandorasBox.OPEN) && !state.getValue(PandorasBox.OPEN)) {
            level.setBlock(pos, state.setValue(PandorasBox.OPEN, true), 3);
        }

        List<LivingEntity> activeMobs = level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(32),
                e -> e.getPersistentData().getBoolean("PandorasMob").orElse(false));

        for (LivingEntity mob : activeMobs) {
            double distSq = mob.distanceToSqr(Vec3.atCenterOf(pos));

            if (distSq > 256) {
                mob.teleportTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
                level.playSound(null, mob.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 1.0F);
            }

            if (mob instanceof Warden warden) {
                Player nearest = level.getNearestPlayer(warden, 20);
                if (nearest != null) {
                    warden.increaseAngerAt(nearest, 30, false);
                    warden.getBrain().eraseMemory(MemoryModuleType.DIG_COOLDOWN);
                    if (warden.getTarget() == null) warden.setTarget(nearest);
                }
            }
        }

        entity.checkForFailure();

        if (entity.arePandoraMobsAlive()) {
            entity.animationTicks = 0;
            return;
        }

        if (entity.currentRound < entity.totalRounds) {
            entity.roundCooldown--;
            if (entity.roundCooldown <= 0) {
                entity.startNextRound();
                entity.roundCooldown = 100;
                entity.setChanged();
            }
        } else {
            entity.spawnTieredRewardChest();
            entity.endEventSuccess();
        }

        if (entity.totalRounds > 0) {
            float progress = Math.min(1.0f, (float) entity.currentRound / (float) entity.totalRounds);
            entity.bossBar.setProgress(progress);
        }
    }

    private void finishEvent(Level level, BlockPos pos, BlockState state) {
        this.isOpening = false;
        level.setBlock(pos, state.setValue(PandorasBox.OPEN, false), 3);
        this.bossBar.removeAllPlayers();
        spawnTieredRewardChest();

        if (level instanceof ServerLevel serverLevel) {
            // serverLevel.setDayTime(0); // Removed because setDayTime is no longer a method on ServerLevel
            broadcastMessage("☀️ A new day dawns as Pandora’s Box closes!", ChatFormatting.GOLD);
        }
        setChanged();
    }

    public int getAnimationTicks() {
        return animationTicks;
    }

    private void startNextRound() {
        if (level == null || level.isClientSide()) return;

        if (arePandoraMobsAlive()) {
            if (!warnedAboutMobs) {
                broadcastMessage("⚠️ You must defeat all spawned mobs before advancing!", ChatFormatting.RED);
                warnedAboutMobs = true;
            }
            return;
        }
        warnedAboutMobs = false;

        currentRound++;

        if (currentRound > totalRounds) {
            broadcastMessage("🎉 You have survived Pandora’s Box! Your rewards await!", ChatFormatting.GOLD);
            spawnTieredRewardChest();
            endEventSuccess();
            return;
        }

        boolean isFinalRound = (currentRound == totalRounds);
        boolean isEliteRound = (currentRound % 3 == 0);

        if (isFinalRound) {
            broadcastMessage("🔥 **Final Round!** Survive this last wave!", ChatFormatting.DARK_RED);
            if (new Random().nextDouble() < 0.10) spawnMiniBoss();
        } else if (isEliteRound) {
            broadcastMessage("⚠️ **Elite Round!** Enemies are stronger!", ChatFormatting.GOLD);
        }

        broadcastMessage("⚔ Round " + currentRound + " of " + totalRounds + " begins!", ChatFormatting.DARK_PURPLE);

        if (currentRound % 2 == 0) triggerRandomEffect();

        Random random = new Random();
        int baseSpawnCount = 6;
        int extraMobs = (int) Math.floor(currentRound * 1.5);
        int spawnCount = Math.min(baseSpawnCount + extraMobs, 20);

        Player nearestPlayer = level.getNearestPlayer(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 10, false);
        if (nearestPlayer == null) return;

        for (int i = 0; i < spawnCount; i++) {
            EntityType<?> entityType = selectRandomMob();
            Entity entity = entityType.create((ServerLevel) level, EntitySpawnReason.EVENT);

            if (entity instanceof Mob mobEntity) {
                double angle = random.nextDouble() * Math.PI * 2;
                double distance = 4 + random.nextDouble() * 4;
                int spawnX = Mth.floor(nearestPlayer.getX() + Math.cos(angle) * distance);
                int spawnZ = Mth.floor(nearestPlayer.getZ() + Math.sin(angle) * distance);
                int spawnY = level.getHeight(Heightmap.Types.WORLD_SURFACE, spawnX, spawnZ);

                mobEntity.setPos(spawnX + 0.5, spawnY, spawnZ + 0.5);
                mobEntity.setCustomName(Component.literal(getRandomPandoraMobName()).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
                mobEntity.setCustomNameVisible(true);

                mobEntity.getPersistentData().putBoolean("PandorasMob", true);
                mobEntity.setPersistenceRequired();

                if (isEliteRound) {
                    mobEntity.addEffect(new MobEffectInstance(MobEffects.INSTANT_DAMAGE, 1200, 1));
                    mobEntity.addEffect(new MobEffectInstance(MobEffects.SPEED, 1200, 1));
                    mobEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1200, 0));
                }

                // giveMobWeapon(mobEntity, level.getRandom()); // uncomment if you still have this method

                if (mobEntity instanceof Warden warden) {
                    warden.increaseAngerAt(nearestPlayer, 150, true);
                    warden.setTarget(nearestPlayer);
                    warden.getBrain().setMemory(MemoryModuleType.ROAR_TARGET, nearestPlayer);
                    warden.getBrain().eraseMemory(MemoryModuleType.DIG_COOLDOWN);
                }

                level.addFreshEntity(mobEntity);
            }
        }

        // Chaos mode completely removed
        roundCooldown = 100;
        applyRandomBattleEffects();
    }

    private static final List<String> MINI_BOSSES = List.of(
            "The Bone Tyrant",
            "The Cursed Warlock",
            "The Void Revenant"
    );

    private void triggerRandomEffect() {
        if (level == null || level.isClientSide()) return;

        Random random = new Random();
        int effect = random.nextInt(5);

        switch (effect) {
            case 0 -> {
                broadcastMessage("⚡ A lightning storm begins!", ChatFormatting.AQUA);
                for (int i = 0; i < 5; i++) {
                    BlockPos strikePos = worldPosition.offset(random.nextInt(10) - 5, 0, random.nextInt(10) - 5);
                    if (level instanceof ServerLevel serverLevel) {
                        LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, serverLevel);
                        Vec3 center = Vec3.atCenterOf(strikePos);
                        bolt.setPos(center.x, center.y, center.z);
                        serverLevel.addFreshEntity(bolt);
                    }
                }
            }
            case 1 -> broadcastMessage("👁️ A dark fog surrounds you!", ChatFormatting.DARK_GRAY);
            case 2 -> broadcastMessage("💀 Creatures become hostile!", ChatFormatting.DARK_RED);
            case 3 -> broadcastMessage("🌪️ Gravity shifts unexpectedly!", ChatFormatting.LIGHT_PURPLE);
            case 4 -> broadcastMessage("🔥 Fire rains from the heavens!", ChatFormatting.GOLD);
        }
    }

    private void applyEffectToPlayers() {
        if (level == null || level.isClientSide()) return;

        List<Holder<MobEffect>> effects = List.of(
                MobEffects.BLINDNESS,
                MobEffects.WEAKNESS,
                MobEffects.LEVITATION,
                MobEffects.SLOW_FALLING,
                MobEffects.SPEED
        );

        Random random = new Random();
        Holder<MobEffect> selected = effects.get(random.nextInt(effects.size()));
        int duration = 200 + random.nextInt(400);

        for (Player player : level.players()) {
            if (player.distanceToSqr(Vec3.atCenterOf(worldPosition)) <= 100) {
                player.addEffect(new MobEffectInstance(selected, duration, 0));
            }
        }
        broadcastMessage("🌀 A strange force affects you...", ChatFormatting.DARK_PURPLE);
    }

    private boolean arePandoraMobsAlive() {
        for (Entity entity : level.getEntities(null, new AABB(worldPosition).inflate(15))) {
            if (entity instanceof Mob mob && mob.getPersistentData().getBoolean("PandorasMob").orElse(false)) {
                return true;
            }
        }
        return false;
    }

    private void spawnMiniBoss() {
        if (level == null || level.isClientSide()) return;

        Random random = new Random();
        String bossName = MINI_BOSSES.get(random.nextInt(MINI_BOSSES.size()));

        Warden boss = new Warden(EntityType.WARDEN, level);
        if (boss instanceof Mob mobEntity) {
            mobEntity.setPos(worldPosition.getX(), worldPosition.getY() + 1, worldPosition.getZ());
            mobEntity.setCustomName(Component.literal(bossName).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
            mobEntity.setCustomNameVisible(true);
            mobEntity.getPersistentData().putBoolean("PandorasMob", true);

            mobEntity.addEffect(new MobEffectInstance(MobEffects.INSTANT_DAMAGE, 1200, 2));
            mobEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1200, 1));
            mobEntity.addEffect(new MobEffectInstance(MobEffects.SPEED, 1200, 1));

            level.addFreshEntity(mobEntity);
        }
        broadcastMessage("💀 " + bossName + " has risen from the darkness!", ChatFormatting.RED);
    }

    // Chaos mode method completely deleted

    private void spawnTieredRewardChest() {
        if (level == null || level.isClientSide()) return;

        Random random = new Random();
        double roll = random.nextDouble();

        List<String> selectedLoot;
        String chestTier;

        if (roll < 0.05) {          // 5% Legendary
            selectedLoot = LEGENDARY_CHEST_LOOT;
            chestTier = "LEGENDARY";
        } else if (roll < 0.20) {   // 15% Rare
            selectedLoot = RARE_CHEST_LOOT;
            chestTier = "RARE";
        } else if (roll < 0.50) {   // 30% Uncommon
            selectedLoot = UNCOMMON_CHEST_LOOT;
            chestTier = "UNCOMMON";
        } else {
            selectedLoot = COMMON_CHEST_LOOT;
            chestTier = "COMMON";
        }

        BlockPos chestPos = worldPosition.offset(1, 0, 1);
        level.setBlockAndUpdate(chestPos, Blocks.CHEST.defaultBlockState());

        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof ChestBlockEntity chest) {
            for (int i = 0; i < 3 + random.nextInt(4); i++) {
                String rewardId = selectedLoot.get(random.nextInt(selectedLoot.size()));

                // Fixed lookup using ResourceLocation and checking against Items.AIR explicitly
                Identifier itemLocation = Identifier.tryParse(rewardId);
                Item rewardItem = (itemLocation != null)
                        ? BuiltInRegistries.ITEM.get(itemLocation).map(Holder::value).orElse(Items.AIR)
                        : Items.AIR;

                if (rewardItem != Items.AIR) {
                    ItemStack stack = new ItemStack(rewardItem, 1 + random.nextInt(2));
                    chest.setItem(random.nextInt(chest.getContainerSize()), stack);
                }
            }
        }
        broadcastMessage("📦 A " + chestTier + " Reward Chest has spawned! Open it for rewards!", ChatFormatting.GOLD);
    }

    private void endEventSuccess() {
        if (level == null || level.isClientSide()) return;

        this.isOpening = false;
        this.bossBar.removeAllPlayers();

        BlockState state = level.getBlockState(worldPosition);
        if (state.hasProperty(PandorasBox.OPEN)) {
            level.setBlock(worldPosition, state.setValue(PandorasBox.OPEN, false), 3);
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.getServer().getCommands().performPrefixedCommand(
                    serverLevel.getServer().createCommandSourceStack(),
                    "time set day"
            );
            broadcastMessage("☀️ The sun rises once more!", ChatFormatting.YELLOW);
        }

        level.playSound(null, worldPosition, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.BLOCKS, 1.0F, 1.0F);
        setChanged();
    }

    private void givePlayerReward() {
        if (level == null || level.isClientSide()) return;
        Player nearest = level.getNearestPlayer(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 10, false);
        if (nearest == null) return;
        broadcastMessage("🎉 You have survived Pandora’s Box! Rewards await!", ChatFormatting.GOLD);
    }

    private EntityType<?> selectRandomMob() {
        Random random = new Random();

        // Build a dynamic pool of all hostile mobs (vanilla + modded), excluding bosses and dragons
        List<EntityType<?>> pool = new ArrayList<>();

        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (type.getCategory() == MobCategory.MONSTER
                    && type != EntityType.ENDER_DRAGON
                    && type != EntityType.WARDEN
                    && type != EntityType.WITHER
                    && type != EntityType.GIANT
                    && type != EntityType.CREAKING){   // ← Excludes Warden and Wither from standard waves

                pool.add(type);
            }
        }

        // Fallback
        if (pool.isEmpty()) {
            return EntityType.ZOMBIE;
        }

        return pool.get(random.nextInt(pool.size()));
    }

    private void checkForFailure() {
        if (level == null || level.isClientSide() || activePlayerUUID == null) return;

        Player player = level.getPlayerByUUID(activePlayerUUID);
        boolean failed = false;

        if (player == null || player.isDeadOrDying()) {
            broadcastMessage("☠ You have perished! Pandora’s Box remains undefeated...", ChatFormatting.RED);
            applyFailureEffects(player);
            failed = true;
        } else {
            // Hardcoded leave radius = 32 blocks
            if (player.distanceToSqr(Vec3.atCenterOf(worldPosition)) > 32 * 32) {
                broadcastMessage("⚠️ The event was abandoned! Pandora’s Box remains restless...", ChatFormatting.RED);
                applyFailureEffects(player);
                grantConsolationPrize();
                failed = true;
            }
        }

        if (failed) {
            despawnPandoraMobs();
            isOpening = false;
            activePlayerUUID = null;
            currentRound = -1;

            BlockState state = level.getBlockState(worldPosition);
            if (state.hasProperty(PandorasBox.OPEN)) {
                level.setBlock(worldPosition, state.setValue(PandorasBox.OPEN, false), 3);
                level.playSound(null, worldPosition, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 1.0F, 0.5F);
            }

            this.bossBar.removeAllPlayers();
            setCooldown();
            setChanged();
        }
    }

    public UUID getActivePlayerUUID() {
        return this.activePlayerUUID;
    }

    public void failEvent(Player player) {
        if (this.bossBar != null) {
            this.bossBar.removeAllPlayers();
        }

        if (player != null) {
            applyFailureEffects(player);
        }

        despawnPandoraMobs();

        if (level != null && !level.getBlockState(worldPosition).isAir()) {
            grantConsolationPrize();
            setCooldown();
        }

        isOpening = false;
        activePlayerUUID = null;
        currentRound = totalRounds + 1;
        roundCooldown = -1;

        if (level != null && !level.isClientSide()) {
            setChanged();
        }
    }

    private void despawnPandoraMobs() {
        if (level == null) return;
        for (Entity entity : level.getEntities(null, new AABB(worldPosition).inflate(30))) {
            if (entity instanceof Mob mob && mob.getPersistentData().getBoolean("PandorasMob").orElse(false)) {
                mob.discard();
            }
        }
    }

    private void applyFailureEffects(Player player) {
        if (player == null) return;

        Random random = new Random();
        int roll = random.nextInt(3);

        switch (roll) {
            case 0 -> {
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 1));
                broadcastMessage("💀 You feel drained of your strength...", ChatFormatting.DARK_RED);
            }
            case 1 -> {
                player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 600, 1));
                broadcastMessage("⛏️ Your hands feel heavy...", ChatFormatting.GRAY);
            }
            case 2 -> {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 400, 0));
                broadcastMessage("👁️ Darkness engulfs you...", ChatFormatting.BLACK);
            }
        }
    }

    private void applyRandomBattleEffects() {
        if (level == null || level.isClientSide()) return;

        Random random = new Random();
        if (random.nextDouble() > 0.30) return;

        Player player = level.getNearestPlayer(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 10, false);
        if (player == null) return;

        int roll = random.nextInt(6);
        switch (roll) {
            case 0 -> {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 1));
                broadcastMessage("✨ A warm energy surrounds you!", ChatFormatting.AQUA);
            }
            case 1 -> {
                player.addEffect(new MobEffectInstance(MobEffects.INSTANT_DAMAGE, 400, 0));
                broadcastMessage("⚔️ You feel a surge of power!", ChatFormatting.RED);
            }
            case 2 -> {
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 400, 1));
                broadcastMessage("💨 Your movements become swift!", ChatFormatting.GREEN);
            }
            case 3 -> {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0));
                broadcastMessage("🌙 Your vision sharpens in the dark!", ChatFormatting.LIGHT_PURPLE);
            }
            case 4 -> {
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 400, 1));
                broadcastMessage("💀 A dark force weakens you!", ChatFormatting.DARK_RED);
            }
            case 5 -> {
                player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 200, 0));
                broadcastMessage("🌪️ The air lifts you up!", ChatFormatting.YELLOW);
            }
        }
    }

    private void grantConsolationPrize() {
        if (level == null || level.isClientSide()) return;

        Player nearest = level.getNearestPlayer(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 10, false);
        if (nearest == null) return;

        broadcastMessage("😢 You abandoned Pandora’s Box, but you still receive a small gift.", ChatFormatting.GRAY);
        level.addFreshEntity(new ItemEntity(level, nearest.getX(), nearest.getY() + 1, nearest.getZ(), new ItemStack(Items.COAL, 1)));
    }

    private void spawnOpeningParticles() {
        if (level == null || level.isClientSide()) return;

        ((ServerLevel) level).sendParticles(
                ParticleTypes.PORTAL,
                worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                20, 0.5, 0.5, 0.5, 0.1
        );
    }

    private String getRandomPandoraMobName() {
        List<String> names = List.of(
                "Shadow Fiend",
                "Cursed Wretch",
                "Void Spawn",
                "Night Stalker",
                "Pandora's Minion",
                "Dark Herald",
                "Abyssal Horror",
                "Twilight Reaper"
        );
        return names.get(new Random().nextInt(names.size()));
    }

    public boolean isOpening() {
        return this.isOpening;
    }

    private void broadcastMessage(String message, ChatFormatting color) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.players().forEach(player ->
                    player.sendSystemMessage(Component.literal(message).withStyle(color, ChatFormatting.BOLD))
            );
        }
    }
}