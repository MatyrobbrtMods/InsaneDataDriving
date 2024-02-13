package com.matyrobbrt.idd.predicate.script;

import com.matyrobbrt.idd.predicate.PredicateType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public sealed interface FieldType {
    FieldType JAVA_OBJECT = new JavaObject();
    FieldType UNIT = new Unit();

    Class<?> resolveType();

    record Predicate(PredicateType<?> type) implements FieldType {
        @Override
        public String toString() {
            return "Predicate<" + type.id() + ">";
        }

        @Override
        public Class<?> resolveType() {
            return ScriptGeneration.getReferenceClass(type);
        }
    }

    record List(FieldType type) implements FieldType {
        @Override
        public String toString() {
            return "List<" + type + ">";
        }

        @Override
        public Class<?> resolveType() {
            return java.util.List.class;
        }
    }

    record Object(Map<String, FieldType> types) implements FieldType {
        @Override
        public String toString() {
            return "[" + types.entrySet().stream()
                    .map(entry -> entry.getKey() + ": " + entry.getValue())
                    .collect(Collectors.joining(", ")) + "]";
        }

        @Override
        public Class<?> resolveType() {
            return Map.class;
        }
    }

    record MapType(FieldType key, FieldType value) implements FieldType {
        @Override
        public String toString() {
            return "Map<" + key + ", " + value + ">";
        }

        @Override
        public Class<?> resolveType() {
            return Map.class;
        }
    }

    record Optional(FieldType type) implements FieldType {
        @Override
        public String toString() {
            return type + "?";
        }

        @Override
        public Class<?> resolveType() {
            return type.resolveType();
        }
    }

    final class JavaObject implements FieldType {
        private JavaObject() {}
        @Override
        public String toString() {
            return "?";
        }

        @Override
        public Class<?> resolveType() {
            return Object.class;
        }
    }

    final class Unit implements FieldType {
        private Unit() {}

        @Override
        public String toString() {
            return "UNIT";
        }

        @Override
        public Class<?> resolveType() {
            return Void.class;
        }
    }

    final class Primitive<T> implements FieldType {
        public static final Primitive<String> STRING = new Primitive<>(String.class);
        public static final Primitive<Integer> INT = new Primitive<>(int.class);
        public static final Primitive<Double> DOUBLE = new Primitive<>(double.class);

        public final Class<T> type;

        private Primitive(Class<T> type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type.getName();
        }

        @Override
        public Class<?> resolveType() {
            return type;
        }
    }
}
