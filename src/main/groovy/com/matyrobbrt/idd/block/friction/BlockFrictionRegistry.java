package com.matyrobbrt.idd.block.friction;

import com.matyrobbrt.idd.DataCodecRegistry;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

public class BlockFrictionRegistry {
    public static final ResourceKey<Registry<Codec<BlockFrictionProvider>>> REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation("idd:block_friction_type"));
    public static final DataCodecRegistry<BlockFrictionProvider> REGISTRY = new DataCodecRegistry<>(
            REGISTRY_KEY, BlockFrictionProvider::codec
    );
    public static final DataMapType<Block, BlockFrictionProvider> DATA_MAP = REGISTRY.dataMap("friction", Registries.BLOCK);

    public static final Codec<ConstantBlockFriction> CONSTANT = REGISTRY.registerDefault("constant", ConstantBlockFriction.CODEC);
}
