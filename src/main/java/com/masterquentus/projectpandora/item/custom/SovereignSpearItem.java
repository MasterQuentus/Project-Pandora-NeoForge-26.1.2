package com.masterquentus.projectpandora.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Consumer;

public class SovereignSpearItem extends Item {

    private static final String UPGRADED_TAG = "upgradedSovereignSpear";

    public SovereignSpearItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isUpgraded(stack);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Base behavior for unupgraded spear
        attacker.heal(1.0F);

        if (isUpgraded(stack)) {
            // Enhanced lifesteal when bound to Pandora's contract
            attacker.heal(1.5F);

            // Pandora's Plague: Unleashing curses from the box upon striking a target
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1));

            // Soul Siphon Execution Effect: If the player is at max health, discharge soul energy
            if (attacker instanceof Player player && player.getHealth() >= player.getMaxHealth()) {
                target.hurt(player.damageSources().magic(), 4.0F);

                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.SOUL,
                            target.getX(),
                            target.getY() + 1,
                            target.getZ(),
                            8,
                            0.3,
                            0.3,
                            0.3,
                            0.05
                    );
                }
            }
        }

        super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {

        builder.accept(
                Component.literal("Weapon: Sovereign Spear")
                        .withStyle(ChatFormatting.GOLD)
        );

        if (isUpgraded(stack)) {
            builder.accept(
                    Component.literal("Sovereign Pact Bound")
                            .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC)
            );
            builder.accept(
                    Component.literal("Passive: Soul Siphon & Pandora's Plagues")
                            .withStyle(ChatFormatting.DARK_RED)
            );
        } else {
            builder.accept(
                    Component.literal("Requires Pandora's Soul & Contract")
                            .withStyle(ChatFormatting.GRAY)
            );
        }
    }

    private boolean isUpgraded(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return false;
        }
        return data.copyTag().getBoolean(UPGRADED_TAG).orElse(false);
    }

    public static void applyUpgrade(ItemStack stack) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(UPGRADED_TAG, true);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}