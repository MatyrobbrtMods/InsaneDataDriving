package com.matyrobbrt.idd.block.expdrop;

import com.matyrobbrt.idd.DataCodecRegistry;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

public class ExperienceDropRegistry {
    public static final ResourceKey<Registry<Codec<ExperienceDropProvider>>> REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation("idd:experience_drop_type"));
    public static final DataCodecRegistry<ExperienceDropProvider> REGISTRY = new DataCodecRegistry<>(
            REGISTRY_KEY, ExperienceDropProvider::codec
    );
    public static final DataMapType<Block, ExperienceDropProvider> DATA_MAP = REGISTRY.dataMap("experience_drop", Registries.BLOCK);

    public static final Codec<ConstantExperienceDrop> CONSTANT = REGISTRY.registerDefault("constant", ConstantExperienceDrop.CODEC);
    public static final Codec<RangeExperienceDrop> RANGE = REGISTRY.register("range", RangeExperienceDrop.CODEC);
}
