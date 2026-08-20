package com.masterquentus.projectpandora.datagen;

import com.masterquentus.projectpandora.ProjectPandora;
import com.masterquentus.projectpandora.block.ModBlocks;
import com.masterquentus.projectpandora.item.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;

public class ModModelProvider extends ModelProvider {
    public ModModelProvider(PackOutput output) {
        super(output, ProjectPandora.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(ModItems.PANDORA_CONTRACT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.PANDORA_SOUL.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateSpear(ModItems.SOVEREIGN_SPEAR.get());


        blockModels.createNonTemplateModelBlock(ModBlocks.PANDORAS_BOX.get());

    }
}
