package com.matyrobbrt.idd.block.toolaction;

import com.matyrobbrt.idd.DataCodecRegistry;
import com.matyrobbrt.idd.util.Codecs;
import com.matyrobbrt.idd.util.MapRemover;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.ToolAction;
import net.neoforged.neoforge.registries.datamaps.AdvancedDataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapValueMerger;

import java.util.Map;

public class ToolModificationRegistry {
    public static final ResourceKey<Registry<Codec<ToolModificationProvider>>> REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation("idd:tool_modification_type"));
    public static final DataCodecRegistry<ToolModificationProvider> REGISTRY = new DataCodecRegistry<>(
            REGISTRY_KEY, ToolModificationProvider::codec
    );

    public static final Codec<Map<ToolAction, ToolModificationProvider>> CODEC = Codec.unboundedMap(
            ToolAction.CODEC, REGISTRY.codec()
    );

    public static final AdvancedDataMapType<Block, Map<ToolAction, ToolModificationProvider>, MapRemover<Block, ToolAction, ToolModificationProvider>> DATA_MAP = AdvancedDataMapType.builder(new ResourceLocation("idd", "tool_modification"), Registries.BLOCK, CODEC)
            .merger(DataMapValueMerger.mapMerger())
            .remover(MapRemover.codec(ToolAction.CODEC))
            .synced(CODEC, true)
            .build();

    public static final Codec<ConstantToolModification> CONSTANT = REGISTRY.registerDefault("constant", ConstantToolModification.CODEC);
    public static final Codec<CombinedToolModification> COMBINED = REGISTRY.register("combined", () -> CombinedToolModification.CODEC);
    public static final Codec<StateMatchesToolModification> STATE_MATCHES = REGISTRY.register("state_matches", () -> StateMatchesToolModification.CODEC);
}
