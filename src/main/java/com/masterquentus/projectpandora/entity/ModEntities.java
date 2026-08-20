package com.masterquentus.projectpandora.entity;

import com.masterquentus.projectpandora.ProjectPandora;
import com.masterquentus.projectpandora.block.ModBlocks;
import com.masterquentus.projectpandora.block.entity.PandorasBoxBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, ProjectPandora.MOD_ID);

    // You need this line
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ProjectPandora.MOD_ID);

    public static final Supplier<BlockEntityType<PandorasBoxBlockEntity>> PANDORAS_BOX_ENTITY =
            BLOCK_ENTITIES.register("pandoras_box",
                    () -> new BlockEntityType<>(
                            PandorasBoxBlockEntity::new,
                            false,
                            ModBlocks.PANDORAS_BOX.get()
                    ));

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);   // also register this one
    }
}