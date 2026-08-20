package com.masterquentus.projectpandora.loot;

import com.masterquentus.projectpandora.item.ModItems;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public class AddArchaeologyItemModifier extends LootModifier {

    public static final MapCodec<AddArchaeologyItemModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            LootModifier.codecStart(inst).apply(inst, AddArchaeologyItemModifier::new)
    );

    public AddArchaeologyItemModifier(LootItemCondition[] conditionsIn, int priority) {
        super(conditionsIn, priority);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        // Higher chance for archaeology (e.g., 30%)
        if (context.getRandom().nextFloat() < 0.30f) {
            generatedLoot.add(new ItemStack(ModItems.PANDORAS_BOX_ITEM.get()));
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}