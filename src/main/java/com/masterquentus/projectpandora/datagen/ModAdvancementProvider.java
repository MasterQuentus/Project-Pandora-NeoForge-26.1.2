package com.masterquentus.projectpandora.datagen;

import com.masterquentus.projectpandora.ProjectPandora;
import com.masterquentus.projectpandora.block.ModBlocks;
import com.masterquentus.projectpandora.item.ModItems;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.criterion.AnyBlockInteractionTrigger;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.network.chat.Component;

import java.util.Optional;
import java.util.function.Consumer;

public class ModAdvancementProvider implements AdvancementSubProvider {

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver) {
        // 1. Curiosity Killed the Box (Root: Finding the box)
        AdvancementHolder findPandorasBox = Advancement.Builder.advancement()
                .display(
                        ModBlocks.PANDORAS_BOX.get(),
                        Component.translatable("advancements.projectpandora.curiosity_killed_the_box.title"),
                        Component.translatable("advancements.projectpandora.curiosity_killed_the_box.description"),
                        Identifier.fromNamespaceAndPath("minecraft", "gui/advancements/backgrounds/adventure"),
                        AdvancementType.TASK,
                        true, true, false
                )
                .addCriterion("has_pandoras_box", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.PANDORAS_BOX.get()))
                .save(saver, Identifier.fromNamespaceAndPath(ProjectPandora.MOD_ID, "curiosity_killed_the_box").toString());

        // 2. Unleashed Chaos (Child: Right-clicking / interacting with a block)
        AdvancementHolder unleashedChaos = Advancement.Builder.advancement()
                .parent(findPandorasBox)
                .display(
                        ModBlocks.PANDORAS_BOX.get(),
                        Component.translatable("advancements.projectpandora.unleashed_chaos.title"),
                        Component.translatable("advancements.projectpandora.unleashed_chaos.description"),
                        null,
                        AdvancementType.GOAL,
                        true, true, false
                )
                // We wrap the raw TriggerInstance into a Criterion using CriteriaTriggers
                .addCriterion("activated_box", CriteriaTriggers.ANY_BLOCK_USE.createCriterion(
                        new AnyBlockInteractionTrigger.TriggerInstance(Optional.empty(), Optional.empty())
                ))
                .save(saver, Identifier.fromNamespaceAndPath(ProjectPandora.MOD_ID, "unleashed_chaos").toString());

        // 3. Master of Relics (Milestone: Future gear or boss drops)
        AdvancementHolder masterOfRelics = Advancement.Builder.advancement()
                .parent(unleashedChaos)
                .display(
                        ModBlocks.PANDORAS_BOX.get(),
                        Component.translatable("advancements.projectpandora.master_of_relics.title"),
                        Component.translatable("advancements.projectpandora.master_of_relics.description"),
                        null,
                        AdvancementType.CHALLENGE,
                        true, true, true
                )
                .addCriterion("master_milestone", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SOVEREIGN_SPEAR.get()))
                .save(saver, Identifier.fromNamespaceAndPath(ProjectPandora.MOD_ID, "master_of_relics").toString());
    }
}