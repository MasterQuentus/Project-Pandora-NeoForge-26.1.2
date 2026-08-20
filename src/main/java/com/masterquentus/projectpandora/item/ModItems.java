package com.masterquentus.projectpandora.item;

import com.masterquentus.projectpandora.ProjectPandora;
import com.masterquentus.projectpandora.block.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ProjectPandora.MOD_ID);

    public static final DeferredItem<BlockItem> PANDORAS_BOX_ITEM = ITEMS.registerSimpleBlockItem(
            "pandoras_box_item",
            ModBlocks.PANDORAS_BOX
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}