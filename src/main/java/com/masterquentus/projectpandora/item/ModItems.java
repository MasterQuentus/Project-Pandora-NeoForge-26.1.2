package com.masterquentus.projectpandora.item;

import com.masterquentus.projectpandora.ProjectPandora;
import com.masterquentus.projectpandora.block.ModBlocks;
import com.masterquentus.projectpandora.item.custom.SovereignSpearItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.KineticWeapon;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ProjectPandora.MOD_ID);

    public static final DeferredItem<Item> PANDORA_CONTRACT = ITEMS.registerSimpleItem("pandora_contract");

    public static final DeferredItem<Item> PANDORA_SOUL = ITEMS.registerSimpleItem("pandora_soul");

    public static final DeferredItem<BlockItem> PANDORAS_BOX_ITEM = ITEMS.registerSimpleBlockItem(
            "pandoras_box_item",
            ModBlocks.PANDORAS_BOX
    );

    public static final DeferredItem<Item> SOVEREIGN_SPEAR = ITEMS.registerItem(
            "sovereign_spear",
            properties -> new SovereignSpearItem(properties.spear(
                    ModToolTiers.SOVEREIGN,
                    0.95f, 0.7f, 0.7f,
                    3.5f, 13f, 8.5f, 5.1f, 13.37f, 4.67f
            ))
    );

    private static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder()
                .add(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath(ProjectPandora.MOD_ID, "weapon.attack_damage"),
                                6.0D,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(
                        Attributes.ATTACK_SPEED,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath(ProjectPandora.MOD_ID, "weapon.attack_speed"),
                                -2.4D,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(
                        Attributes.ENTITY_INTERACTION_RANGE,
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath(ProjectPandora.MOD_ID, "weapon.reach"),
                                1.5D,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.MAINHAND
                )
                .build();
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}