package com.masterquentus.projectpandora;

import com.masterquentus.projectpandora.block.ModBlocks;
import com.masterquentus.projectpandora.creativemodtab.ModCreativeModTabs;
import com.masterquentus.projectpandora.entity.ModEntities;
import com.masterquentus.projectpandora.item.ModItems;
import com.masterquentus.projectpandora.loot.ModLootModifiers;
import com.masterquentus.projectpandora.recipe.ModRecipes;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(ProjectPandora.MOD_ID)
public class ProjectPandora {
    public static final String MOD_ID = "projectpandora";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ProjectPandora(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        ModCreativeModTabs.register(modEventBus);

        ModRecipes.register(modEventBus);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModEntities.register(modEventBus);
        ModLootModifiers.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }
}