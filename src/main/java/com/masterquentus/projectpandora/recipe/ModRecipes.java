package com.masterquentus.projectpandora.recipe;

import com.masterquentus.projectpandora.ProjectPandora;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipes {

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, ProjectPandora.MOD_ID);

    public static final DeferredHolder<
                RecipeSerializer<?>,
                RecipeSerializer<SovereignSpearUpgradeRecipe>> SOVEREIGN_SPEAR_UPGRADE_SERIALIZER =
            SERIALIZERS.register("sovereign_spear_upgrade", () ->
                    new RecipeSerializer<>(
                            MapCodec.unit(SovereignSpearUpgradeRecipe::new),
                            StreamCodec.unit(new SovereignSpearUpgradeRecipe())
                    )
            );

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}