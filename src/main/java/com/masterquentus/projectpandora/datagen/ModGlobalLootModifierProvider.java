package com.masterquentus.projectpandora.datagen;

import com.masterquentus.projectpandora.ProjectPandora;
import com.masterquentus.projectpandora.loot.AddArchaeologyItemModifier;
import com.masterquentus.projectpandora.loot.AddItemModifier;
import com.masterquentus.projectpandora.loot.ModLootModifiers;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import java.util.concurrent.CompletableFuture;

public class ModGlobalLootModifierProvider extends GlobalLootModifierProvider {
    public ModGlobalLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, ProjectPandora.MOD_ID);
    }

    @Override
    protected void start() {
        // High-tier / Endgame Structures (Best chances)
        addChest("pandoras_box_stronghold_library", "minecraft", "chests/stronghold_library");
        addChest("pandoras_box_stronghold_corridor", "minecraft", "chests/stronghold_corridor");
        addChest("pandoras_box_ancient_city", "minecraft", "chests/ancient_city");
        addChest("pandoras_box_bastion_treasure", "minecraft", "chests/bastion_treasure");
        addChest("pandoras_box_end_city_treasure", "minecraft", "chests/end_city_treasure");

        // Mid-tier Structures
        addChest("pandoras_box_desert_pyramid", "minecraft", "chests/desert_pyramid");
        addChest("pandoras_box_jungle_temple", "minecraft", "chests/jungle_temple");
        addChest("pandoras_box_nether_bridge", "minecraft", "chests/nether_bridge");
        addChest("pandoras_box_ruined_portal", "minecraft", "chests/ruined_portal");
        addChest("pandoras_box_woodland_mansion", "minecraft", "chests/woodland_mansion");
        addChest("pandoras_box_trial_chambers_reward", "minecraft", "chests/trial_chambers/reward");

        // Low-tier Structures
        addChest("pandoras_box_simple_dungeon", "minecraft", "chests/simple_dungeon");
        addChest("pandoras_box_abandoned_mineshaft", "minecraft", "chests/abandoned_mineshaft");
        addChest("pandoras_box_shipwreck_treasure", "minecraft", "chests/shipwreck_treasure");
        addChest("pandoras_box_buried_treasure", "minecraft", "chests/buried_treasure");
        addChest("pandoras_box_pillager_outpost", "minecraft", "chests/pillager_outpost");

        // Archaeology (More common when brushing sand/gravel)
        addArchaeology("pandoras_box_trail_ruins", "minecraft", "archaeology/trail_ruins_rare");
        addArchaeology("pandoras_box_desert_pyramid_brush", "minecraft", "archaeology/desert_pyramid");
    }

    // Helper method for chests
    private void addChest(String name, String namespace, String path) {
        this.add(
                ModLootModifiers.ADD_ITEM.get(),
                name,
                new AddItemModifier(
                        new LootItemCondition[] {
                                LootTableIdCondition.builder(
                                        Identifier.fromNamespaceAndPath(namespace, path)
                                ).build()
                        },
                        1
                )
        );
    }

    // Helper method for archaeology sand/gravel
    private void addArchaeology(String name, String namespace, String path) {
        this.add(
                ModLootModifiers.ADD_ARCHAEOLOGY_ITEM.get(),
                name,
                new AddArchaeologyItemModifier(
                        new LootItemCondition[] {
                                LootTableIdCondition.builder(
                                        Identifier.fromNamespaceAndPath(namespace, path)
                                ).build()
                        },
                        1
                )
        );
    }

    // Changed parameter from AddItemModifier to IGlobalLootModifier so it accepts both types
    private void add(MapCodec<? extends IGlobalLootModifier> codec, String name, IGlobalLootModifier modifier) {
        super.add(name, modifier);
    }
}