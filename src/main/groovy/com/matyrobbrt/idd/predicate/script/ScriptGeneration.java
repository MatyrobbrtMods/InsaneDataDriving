package com.matyrobbrt.idd.predicate.script;

import com.matyrobbrt.idd.predicate.PredicateType;
import groovy.lang.ExpandoMetaClass;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V17;

public class ScriptGeneration {

    public static Class<?> getOrGenerateOwner(PredicateType<?> predicateType) {
        return getEmptyClass("Generated_Predicate_" + predicateType.id());
    }

    private static final Map<String, Class<?>> CLASSES = new ConcurrentHashMap<>();

    public static Class<?> getReferenceClass(String name) {
        var c = CLASSES.get(name);
        if (c != null) return c;
        ClassWriter classWriter = new ClassWriter(0);
        FieldVisitor fieldVisitor;
        MethodVisitor methodVisitor;

        classWriter.visit(V17, ACC_PUBLIC | ACC_SUPER, "com/matyrobbrt/idd/predicate/script/" + name, null, "java/lang/Object", new String[] {"com/matyrobbrt/idd/predicate/PredicateReference"});

        {
            fieldVisitor = classWriter.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "reference", "Ljava/lang/Object;", null, null);
            fieldVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/lang/Object;)V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(6, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(7, label1);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitFieldInsn(Opcodes.PUTFIELD, "com/matyrobbrt/idd/predicate/script/" + name, "reference", "Ljava/lang/Object;");
            Label label2 = new Label();
            methodVisitor.visitLabel(label2);
            methodVisitor.visitLineNumber(8, label2);
            methodVisitor.visitInsn(RETURN);
            Label label3 = new Label();
            methodVisitor.visitLabel(label3);
            methodVisitor.visitLocalVariable("this", "Lcom/matyrobbrt/idd/predicate/script/" + name + ";", null, label0, label3, 0);
            methodVisitor.visitLocalVariable("reference", "Ljava/lang/Object;", null, label0, label3, 1);
            methodVisitor.visitMaxs(2, 2);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "getReference", "()Ljava/lang/Object;", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(12, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(Opcodes.GETFIELD, "com/matyrobbrt/idd/predicate/script/" + name, "reference", "Ljava/lang/Object;");
            methodVisitor.visitInsn(Opcodes.ARETURN);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLocalVariable("this", "Lcom/matyrobbrt/idd/predicate/script/" + name + ";", null, label0, label1, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();

        try {
            final var clz = MethodHandles.lookup()
                    .defineClass(classWriter.toByteArray());
            CLASSES.put(name, clz);
            DefaultGroovyMethods.setMetaClass(clz, new ExpandoMetaClass(clz, true, true));
            return clz;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> getEmptyClass(String name, String... interfaces) {
        var c = CLASSES.get(name);
        if (c != null) return c;

        ClassWriter classWriter = new ClassWriter(0);

        final String internalName = "com/matyrobbrt/idd/predicate/script/" + name;
        classWriter.visit(V17, ACC_PUBLIC | ACC_SUPER, internalName, null, "java/lang/Object", interfaces);

        {
            MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(3, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            methodVisitor.visitInsn(RETURN);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLocalVariable("this", "L" + internalName + ";", null, label0, label1, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();

        try {
            final var clz = MethodHandles.lookup()
                    .defineClass(classWriter.toByteArray());
            CLASSES.put(name, clz);
            DefaultGroovyMethods.setMetaClass(clz, new ExpandoMetaClass(clz, true, true));
            return clz;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
