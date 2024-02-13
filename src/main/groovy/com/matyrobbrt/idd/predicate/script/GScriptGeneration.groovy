package com.matyrobbrt.idd.predicate.script


import com.matyrobbrt.idd.predicate.PredicateType
import com.matyrobbrt.idd.util.codec.CodecDecomposer
import com.mojang.datafixers.util.Pair
import com.mojang.logging.LogUtils
import com.mojang.serialization.Codec
import com.mojang.serialization.DynamicOps
import net.minecraft.resources.ResourceLocation
import org.codehaus.groovy.reflection.CachedClass

class GScriptGeneration {
    static <T> void define(CodecDecomposer dec, Class <?> clz, PredicateType predicate, Map<Pair<ResourceLocation, Collection<String>>, Codec<T>> reg) {
        final objClass = ScriptGeneration.getReferenceClass(predicate)

        objClass.metaClass.and << { other ->
            return objClass.newInstance(predicate.concat(delegate.reference, other.reference))
        }.tap { it.parameterTypes[0] = objClass }

        objClass.metaClass.or << { other ->
            return objClass.newInstance(predicate.or(delegate.reference, other.reference))
        }.tap { it.parameterTypes[0] = objClass }

        objClass.metaClass.bitwiseNegate << { ->
            return objClass.newInstance(predicate.not(delegate.reference))
        }

        clz.metaClass {
            ops = null
        }

        final itr = reg.entrySet().iterator()
        while (itr.hasNext()) {
            final it = itr.next()
            final id = it.key.first
            ([it.key.first.path] + it.key.second).each { path ->
                final codec = it.value
                final type = dec.decomposeUnsafe(codec)
                final var cls = (ExpandoMetaClass) resolveLocation(clz, getPath(id.namespace, path)).metaClass

                final mtdName = path.split('/').last()
                final decoder = { Object object, input ->
                    final DynamicOps<Object> ops = object.ops
                    return objClass.newInstance(codec.decode(ops, input)
                            .resultOrPartial({
                                LogUtils.getLogger().error("Failed to decode: $it; input: $input; type: $id")
                            }).orElseThrow().first)
                }

                if (type instanceof FieldType.Object) {
                    if (type.types().size() == 1) {
                        final firstType = type.types().entrySet().first()
                        cls.registerInstanceMethod(new ScriptMethod(mtdName, objClass, [firstType.value.resolveType()]) {
                            @Override
                            CachedClass getDeclaringClass() {
                                return cls.theCachedClass
                            }

                            @Override
                            Object invoke(Object object, Object[] arguments) {
                                if (firstType.value instanceof FieldType.List && arguments.length > 1 && arguments[0] !instanceof List) {
                                    final actualArguments = arguments
                                    arguments = new Object[] { actualArguments.toList() }
                                }
                                return decoder(object, Map.of(firstType.key, arguments[0]))
                            }
                        })
                    }

                    cls.registerInstanceMethod(new ScriptMethod(mtdName, objClass, [type.resolveType()]) {
                        @Override
                        CachedClass getDeclaringClass() {
                            return cls.theCachedClass
                        }

                        @Override
                        Object invoke(Object object, Object[] arguments) {
                            return decoder(object, arguments[0])
                        }
                    })
                } else if (type instanceof FieldType.JavaObject || type instanceof FieldType.Predicate || type instanceof FieldType.Primitive) {
                    cls.registerInstanceMethod(new ScriptMethod(mtdName, objClass, [type.resolveType()]) {
                        @Override
                        CachedClass getDeclaringClass() {
                            return cls.theCachedClass
                        }

                        @Override
                        Object invoke(Object object, Object[] arguments) {
                            return decoder(object, arguments[0])
                        }
                    })
                } else if (type instanceof FieldType.List) {
                    cls.registerInstanceMethod(new ScriptMethod(mtdName, objClass, [type.resolveType()]) {
                        @Override
                        CachedClass getDeclaringClass() {
                            return cls.theCachedClass
                        }

                        @Override
                        Object invoke(Object object, Object[] arguments) {
                            return decoder(object, arguments.collectMany {
                                it instanceof Collection ? it : [it]
                            })
                        }
                    })
                } else if (type instanceof FieldType.Unit) {
                    cls.registerInstanceMethod(new ScriptMethod(mtdName, objClass, []) {
                        @Override
                        CachedClass getDeclaringClass() {
                            return cls.theCachedClass
                        }

                        @Override
                        Object invoke(Object object, Object[] arguments) {
                            return decoder(object, [:])
                        }
                    })
                }
            }
        }
    }

    static List<String> getPath(String namespace, String idPath) {
        final List<String> path = []
        if (namespace != 'idd') {
            path.add(namespace)
        }
        final split = idPath.split('/')
        for (int i = 0; i < split.length - 1; i++) path.add(split[i])
        return path
    }

    static Class<?> resolveLocation(Class<?> parent, List<String> subpath) {
        subpath.forEach { sub ->
            final newCls = ScriptGeneration.getEmptyClass(parent.simpleName + '$' + sub)
            parent.metaClass {
                setProperty(sub, newCls.newInstance())
            }
            parent = newCls
        }
        return parent
    }

    static void removeMethods(MetaClass cls, Object other) {
        cls.is = other.metaClass.is
    }
}
