package com.juanmuscaria.uncode.cleaners;

import org.objectweb.asm.*;

/**
 * Cleans annotation and attributes from a field.
 */
public class FieldCleaner extends FieldVisitor {

  public FieldCleaner(FieldVisitor fieldVisitor) {
    super(Opcodes.ASM9, fieldVisitor);
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
    // NO-OP
  }

  @Override
  public void visitEnd() {
    super.visitEnd();
  }
}
