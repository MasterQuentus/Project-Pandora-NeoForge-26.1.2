package com.masterquentus.projectpandora;

import com.masterquentus.projectpandora.datagen.ModAdvancementProvider;
import com.masterquentus.projectpandora.datagen.ModBlockLootTableProvider;
import com.masterquentus.projectpandora.datagen.ModGlobalLootModifierProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;

@EventBusSubscriber(modid = ProjectPandora.MOD_ID)
public class ProjectPandoraDataGen {
    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        var lookupProvider = event.getLookupProvider();

        generator.addProvider(true, new LootTableProvider(packOutput, Collections.emptySet(),
                List.of(new LootTableProvider.SubProviderEntry(ModBlockLootTableProvider::new, LootContextParamSets.BLOCK)), lookupProvider));

        generator.addProvider(true, new ModGlobalLootModifierProvider(packOutput, lookupProvider));

        // Advancements
        event.createProvider(output -> new net.minecraft.data.advancements.AdvancementProvider(
                output,
                lookupProvider,
                List.of(new ModAdvancementProvider())
        ));
    }
}