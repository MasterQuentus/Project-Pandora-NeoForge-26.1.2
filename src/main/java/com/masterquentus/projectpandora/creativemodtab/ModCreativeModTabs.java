package com.masterquentus.projectpandora.creativemodtab;

import com.masterquentus.projectpandora.ProjectPandora;
import com.masterquentus.projectpandora.block.ModBlocks;
import com.masterquentus.projectpandora.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ProjectPandora.MOD_ID);


    public static final Supplier<CreativeModeTab> PROJECT_LILITH_ITEMS_TAB = CREATIVE_MODE_TABS.register("project_pandora_items_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.PANDORAS_BOX_ITEM.get()))
                    .title(Component.translatable("creativetab.projectpandora_items"))
                    .withTabsBefore(CreativeModeTabs.INGREDIENTS)
                    .withTabsAfter(Identifier.fromNamespaceAndPath(ProjectPandora.MOD_ID, "project_pandora_blocks_tab"))
                    .displayItems((itemDisplayParameters, output) -> {

                        output.accept(ModBlocks.PANDORAS_BOX);


                    }).build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}