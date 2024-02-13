package com.matyrobbrt.idd;

import com.matyrobbrt.idd.block.expdrop.ExperienceDropRegistry;
import com.matyrobbrt.idd.block.friction.BlockFrictionRegistry;
import com.matyrobbrt.idd.block.toolaction.CombinedToolModification;
import com.matyrobbrt.idd.block.toolaction.ConstantToolModification;
import com.matyrobbrt.idd.block.toolaction.StateMatchesToolModification;
import com.matyrobbrt.idd.block.toolaction.ToolModificationRegistry;
import com.matyrobbrt.idd.predicate.PredicateReference;
import com.matyrobbrt.idd.predicate.entity.EntityPredicates;
import com.matyrobbrt.idd.predicate.script.GScriptGeneration;
import com.matyrobbrt.idd.predicate.script.ScriptGeneration;
import com.matyrobbrt.idd.util.codec.CodecDecomposer;
import com.mojang.datafixers.util.Pair;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
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
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mod("idd")
public class InsaneDataDriving {
    public InsaneDataDriving(IEventBus bus) {
        ExperienceDropRegistry.REGISTRY.register(bus);
        BlockFrictionRegistry.REGISTRY.register(bus);
        ToolModificationRegistry.REGISTRY.register(bus);
        EntityPredicates.REGISTRY.register(bus);

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
            final var cls = ScriptGeneration.getOrGenerateOwner(EntityPredicates.TYPE);
            GScriptGeneration.define(new CodecDecomposer()
                    .registerDefaults()
                    .registerPrimitives(), cls, EntityPredicates.TYPE, EntityPredicates.REGISTRY.registry()
                    .holders().collect(Collectors.toMap(
                            h -> {
                                final var id = h.unwrapKey().orElseThrow().location();
                                return new Pair<>(id, EntityPredicates.TYPE.getAliases().get(id));
                            },
                            h -> h.value()
                    )));

            try {
                final var instance = cls.newInstance();
                DefaultGroovyMethods.getMetaClass(instance).setProperty(instance, "ops", JavaOps.INSTANCE);
                final var shell = new GroovyShell(new Binding());
                shell.setVariable("entity", instance);
                System.out.println(((PredicateReference)
                    shell.evaluate("~(entity.is('minecraft:allay') | entity.is(entity: 'minecraft:player')) & entity.isOnFire()"))
                        .getReference());
            } catch (Exception ignored) {
                throw new RuntimeException(ignored);
            }

            System.exit(0);
        });
    }
}
