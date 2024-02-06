package com.juanmuscaria.uncode.cleaners;

import org.objectweb.asm.*;

/**
 * Remove most attributes from a class, leaving only public methods without a body, public fields and inner classes.
 * All synthetic members are removed, classes produced by this is not intended to by loaded by the jvm.
 */
public class ClassCleaner extends ClassVisitor {

  public ClassCleaner(ClassVisitor cv) {
    super(Opcodes.ASM9, cv);
  }

  @Override
  public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
    // We only want public classes
    if ((access & Opcodes.ACC_PUBLIC) == 0) {
      throw new IllegalArgumentException("Class is not public, skipping");
    } else if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
      throw new IllegalArgumentException("Class is synthetic (compiler generated), skipping");
    }
    super.visit(version, access, name, signature, superName, interfaces);
  }

  @Override
  public void visitSource(String source, String debug) {
    // Warns this class was touched by the code killer
    super.visitSource("uncoded", null);
  }

  @Override
  public ModuleVisitor visitModule(String name, int access, String version) {
    // NO-OP - Remove element
    return null;
  }

  @Override
  public void visitNestHost(String nestHost) {
    // NO-OP - Remove element
  }

  @Override
  public void visitOuterClass(String owner, String name, String descriptor) {
    super.visitOuterClass(owner, name, descriptor);
  }

  @Override
  public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
    return super.visitAnnotation(descriptor, visible);
  }

  @Override
  public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
    return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
  }

  @Override
  public void visitAttribute(Attribute attribute) {
    // NO-OP - Remove element
  }

  @Override
  public void visitNestMember(String nestMember) {
    // NO-OP - Remove element
  }

  @Override
  public void visitPermittedSubclass(String permittedSubclass) {
    // NO-OP - Remove element
  }

  @Override
  public void visitInnerClass(String name, String outerName, String innerName, int access) {
    // We only want public classes
    if ((access & Opcodes.ACC_PUBLIC) == 0) {
      return;
    } else if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
      return;
    }
    super.visitInnerClass(name, outerName, innerName, access);
  }

  @Override
  public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
    // NO-OP - Remove element
    return null;
  }

  @Override
  public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
    // We only want public fields
    if ((access & Opcodes.ACC_PUBLIC) == 0) {
      return null;
    } else if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
      return null;
    }
    // Keep the initial value if it's a constant
    return new FieldCleaner(super.visitField(access, name, descriptor, signature, (access & Opcodes.ACC_STATIC) != 0 ? value : null));
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
    // We only want public methods
    if ((access & Opcodes.ACC_PUBLIC) == 0) {
      return null;
    } else if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
      return null;
    }
    return new MethodCleaner(super.visitMethod(access, name, descriptor, signature, exceptions), name, descriptor);
  }

  @Override
  public void visitEnd() {
    super.visitEnd();
  }
}
