package com.masterquentus.projectpandora.tags;

import com.masterquentus.projectpandora.ProjectPandora;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModTags {
    public static class Items {
        public static final TagKey<Item> SOVEREIGN_SPEAR = createTag("sovereign_spear");

        private static TagKey<Item> createTag(String name) {
            return ItemTags.create(Identifier.fromNamespaceAndPath(ProjectPandora.MOD_ID, name));
        }
    }
}
