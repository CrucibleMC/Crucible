package com.juanmuscaria.uncode.cleaners;

import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * Cleans a method from all its code, annotations and attributes.
 */
public class MethodCleaner extends MethodVisitor {

  private final String name;
  private final String descriptor;

  public MethodCleaner(MethodVisitor methodVisitor, final String name,
                       final String descriptor) {
    super(Opcodes.ASM9, methodVisitor);
    this.name = name;
    this.descriptor = descriptor;
  }

  @Override
  public void visitParameter(String name, int access) {
    super.visitParameter(name, access);
  }

  @Override
  public AnnotationVisitor visitAnnotationDefault() {
    // NO-OP - Remove element
    return super.visitAnnotationDefault();
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
  public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
    super.visitAnnotableParameterCount(parameterCount, visible);
  }

  @Override
  public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
    return super.visitParameterAnnotation(parameter, descriptor, visible);
  }

  @Override
  public void visitAttribute(Attribute attribute) {
    // NO-OP - Remove element
  }

  @Override
  public void visitCode() {
    var type = Type.getType(descriptor);
    super.visitCode();

    if ("<init>".equals(name)) { // It's a constructor, call super
      super.visitLabel(new Label());
      // Load "this" into the operand stack
      super.visitVarInsn(ALOAD, 0);
      // Calls the super constructor
      super.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
      // Return
      super.visitInsn(RETURN);
    } else if (type.getReturnType().equals(Type.VOID_TYPE)) { // Void return type
      // Return
      super.visitInsn(RETURN);
    } else {
      // Load null into the operand stack
      super.visitInsn(ACONST_NULL);
      // Return
      super.visitInsn(ARETURN);
    }

  }

  @Override
  public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
    // NO-OP - Remove element
  }

  @Override
  public void visitInsn(int opcode) {
    // NO-OP - Remove element
  }

  @Override
  public void visitIntInsn(int opcode, int operand) {
    // NO-OP - Remove element
  }

  @Override
  public void visitVarInsn(int opcode, int varIndex) {
    // NO-OP - Remove element
  }

  @Override
  public void visitTypeInsn(int opcode, String type) {
    // NO-OP - Remove element
  }

  @Override
  public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
    // NO-OP - Remove element
  }


  @Override
  public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
    // NO-OP - Remove element
  }

  @Override
  public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
    // NO-OP - Remove element
  }

  @Override
  public void visitJumpInsn(int opcode, Label label) {
    // NO-OP - Remove element
  }

  @Override
  public void visitLabel(Label label) {
    // NO-OP - Remove element
  }

  @Override
  public void visitLdcInsn(Object value) {
    // NO-OP - Remove element
  }

  @Override
  public void visitIincInsn(int varIndex, int increment) {
    // NO-OP - Remove element
  }

  @Override
  public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
    // NO-OP - Remove element
  }

  @Override
  public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
    // NO-OP - Remove element
  }

  @Override
  public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
    // NO-OP - Remove element
  }

  @Override
  public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
    // NO-OP - Remove element
    return null;
  }

  @Override
  public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
    // NO-OP - Remove element
  }

  @Override
  public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
    // NO-OP - Remove element
    return null;
  }

  @Override
  public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
    // NO-OP - Remove element
  }

  @Override
  public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
    // NO-OP - Remove element
    return null;
  }

  @Override
  public void visitLineNumber(int line, Label start) {
    // NO-OP - Remove element
  }

  @Override
  public void visitMaxs(int maxStack, int maxLocals) {
    super.visitMaxs(1, 1);
  }

  @Override
  public void visitEnd() {
    super.visitEnd();
  }
}
