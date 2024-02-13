package com.matyrobbrt.idd.predicate;

import com.matyrobbrt.idd.predicate.script.GScriptGeneration;
import com.matyrobbrt.idd.predicate.script.ScriptGeneration;
import com.matyrobbrt.idd.util.codec.CodecDecomposer;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import groovy.lang.Binding;
import groovy.lang.ExpandoMetaClass;
import groovy.lang.GroovyShell;
import groovy.lang.MetaClass;
import groovy.util.DelegatingScript;
import net.minecraft.core.Holder;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.intellij.lang.annotations.Language;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PredicateTypes {
    public static final Map<String, PredicateType<?>> TYPES = new ConcurrentHashMap<>();
    public static final Map<String, PredicateFactory<?>> FACTORIES = new ConcurrentHashMap<>();
    public static final CustomGShell GROOVY_SHELL = CustomGShell.make();

    public static final CodecDecomposer DECOMPOSER = new CodecDecomposer()
            .registerPrimitives()
            .registerDefaults();

    public static void register(PredicateType<?> type) {
        TYPES.put(type.id(), type);
    }

    public static <T> PredicateFactory<T> getFactory(PredicateType<T> type) {
        return (PredicateFactory<T>) FACTORIES.get(type.id());
    }

    public static void init() {
        TYPES.values().forEach(LamdbaExceptionUtils.rethrowConsumer(type -> {
            final var owner = ScriptGeneration.getOrGenerateOwner(type);
            GScriptGeneration.define(
                    DECOMPOSER, owner, type, type.registry().holders().collect(Collectors.toMap(
                            h -> {
                                final var id = h.unwrapKey().orElseThrow().location();
                                return new Pair<>(id, type.getAliases().get(id));
                            },
                            Holder.Reference::value
                    ))
            );
            final var ctor = owner.getDeclaredConstructor();
            FACTORIES.put(type.id(), new PredicateFactory<>() {
                @Override
                public Object create(DynamicOps<Object> ops) {
                    final Object value;
                    try {
                        value = ctor.newInstance();
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                    DefaultGroovyMethods.hasProperty(value, "ops").setProperty(value, ops);
                    DefaultGroovyMethods.getMetaClass(value).setProperty(value, "ops", ops);
                    return value;
                }

                @Override
                public Object evaluate(String script, DynamicOps<Object> ops) {
                    final var instance = create(ops);
                    final MetaClass target = DefaultGroovyMethods.getMetaClass(instance);
                    final Binding binding = new Binding();
                    FACTORIES.forEach((id, fact) -> {
                        if (fact == this) return;

                        binding.setVariable(id, fact.create(ops));
                    });

                    final DelegatingScript sc = (DelegatingScript) GROOVY_SHELL.parse(script, GROOVY_SHELL.genScript(), binding);
                    sc.setDelegate(instance);
                    sc.setMetaClass(target);
                    return ((PredicateReference)sc.run()).getReference();
                }
            });
        }));
    }

    public interface PredicateFactory<T> {
        Object create(DynamicOps<Object> ops);

        T evaluate(@Language("groovy") String script, DynamicOps<Object> ops);

        default DataResult<T> evaluateOrError(@Language("groovy") String script, DynamicOps<Object> ops) {
            try {
                return DataResult.success(evaluate(script, ops));
            } catch (Throwable ex) {
                LogUtils.getLogger().error("Err: ", ex);
                return DataResult.error(() -> "Could not evaluate groovy script: " + ex);
            }
        }
    }

    public static final class CustomGShell extends GroovyShell {

        public CustomGShell(CompilerConfiguration config) {
            super(config);
        }

        public static CustomGShell make() {
            final var config = new CompilerConfiguration();
            config.setScriptBaseClass(DelegatingScript.class.getName());
            return new CustomGShell(config);
        }

        public String genScript() {
            return generateScriptName();
        }
    }
}
