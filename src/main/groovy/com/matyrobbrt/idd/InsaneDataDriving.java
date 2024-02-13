package com.matyrobbrt.idd;

import com.matyrobbrt.idd.block.expdrop.ExperienceDropRegistry;
import com.matyrobbrt.idd.block.friction.BlockFrictionRegistry;
import com.matyrobbrt.idd.block.toolaction.CombinedToolModification;
import com.matyrobbrt.idd.block.toolaction.ConstantToolModification;
import com.matyrobbrt.idd.block.toolaction.StateMatchesToolModification;
import com.matyrobbrt.idd.block.toolaction.ToolModificationRegistry;
import com.matyrobbrt.idd.predicate.PredicateCodec;
import com.matyrobbrt.idd.predicate.PredicateTypes;
import com.matyrobbrt.idd.predicate.entity.EntityPredicate;
import com.matyrobbrt.idd.predicate.entity.EntityPredicates;
import com.matyrobbrt.idd.predicate.item.ItemPredicates;
import com.matyrobbrt.idd.predicate.number.NumberPredicates;
import com.mojang.logging.LogUtils;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.JavaOps;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceGateBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.ToolActions;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import org.intellij.lang.annotations.Language;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mod("idd")
public class InsaneDataDriving {
    public InsaneDataDriving(IEventBus bus) {
        ExperienceDropRegistry.REGISTRY.register(bus);
        BlockFrictionRegistry.REGISTRY.register(bus);
        ToolModificationRegistry.REGISTRY.register(bus);
        EntityPredicates.REGISTRY.register(bus);
        ItemPredicates.REGISTRY.register(bus);
        NumberPredicates.REGISTRY.register(bus);

        bus.addListener(RegisterDataMapTypesEvent.class, event -> event.register(ToolModificationRegistry.DATA_MAP));

        bus.addListener(GatherDataEvent.class, event -> event.getGenerator().addProvider(event.includeServer(), new DataMapProvider(event.getGenerator().getPackOutput(), event.getLookupProvider()) {
            @Override
            protected void gather() {
                builder(ToolModificationRegistry.DATA_MAP)
                        .add(Blocks.ACACIA_FENCE_GATE.builtInRegistryHolder(), Map.of(
                                ToolActions.AXE_STRIP, new CombinedToolModification(List.of(
                                        new StateMatchesToolModification(StatePropertiesPredicate.Builder.properties()
                                                .hasProperty(FenceGateBlock.OPEN, true)
                                                .build().orElseThrow(),
                                                new ConstantToolModification(Blocks.IRON_BLOCK.defaultBlockState())),
                                        new ConstantToolModification(Blocks.DIRT.defaultBlockState())
                                )),
                                ToolActions.HOE_TILL, new ConstantToolModification(Blocks.BIRCH_FENCE_GATE.defaultBlockState()
                                        .setValue(FenceGateBlock.OPEN, true))
                        ), false);
            }
        }));

        bus.addListener(FMLCommonSetupEvent.class, event -> {
            PredicateTypes.register(EntityPredicates.TYPE);
            PredicateTypes.register(ItemPredicates.TYPE);
            PredicateTypes.register(NumberPredicates.TYPE);
            PredicateTypes.init();

            @Language("groovy")
            final String decoding = """
                ~is('allay') & (~isOnFire() | isHolding(
                    hand: 'main_hand',
                    item: item.is('carrot') & (item.count(number.equal(15)) | item.count(number.greater(20)))
                ))""";
            System.out.println(EntityPredicates.CODEC.decode(new PredicateCodec.PredicateOps<Object>(
                    RegistryOps.create(JavaOps.INSTANCE, new RegistryOps.RegistryInfoLookup() {
                        @Override
                        public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> pRegistryKey) {
                            return Optional.empty();
                        }
                    }),
                    true
            ), decoding).resultOrPartial(LogUtils.getLogger()::error).orElseThrow().getFirst());

            System.exit(0);
        });
    }
}
