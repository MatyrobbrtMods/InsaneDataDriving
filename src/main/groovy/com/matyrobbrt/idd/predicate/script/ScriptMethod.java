package com.matyrobbrt.idd.predicate.script;

import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.reflection.ReflectionUtils;

import java.util.List;

public abstract class ScriptMethod extends MetaMethod {
    private final String name;
    private final Class<?> returnType;
    public ScriptMethod(String name, Class<?> returnType, List<Class<?>> parameters) {
        this.name = name;
        this.returnType = returnType;
        this.parameterTypes = parameters.stream().map(ReflectionCache::getCachedClass).toArray(CachedClass[]::new);
    }

    @Override
    public int getModifiers() {
        return 0;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getReturnType() {
        return returnType;
    }
}
