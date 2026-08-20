package com.masterquentus.projectpandora.item;

import com.masterquentus.projectpandora.tags.ModTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ToolMaterial;

public class ModToolTiers {
    public static final ToolMaterial SOVEREIGN = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, // Endgame tool tier fallback
            2500,                                   // Durability (very high for a boss weapon)
            9.0F,                                   // Mining speed
            4.0F,                                   // Base attack damage from material
            25,                                     // Enchantability
            ModTags.Items.SOVEREIGN_SPEAR
    );
}