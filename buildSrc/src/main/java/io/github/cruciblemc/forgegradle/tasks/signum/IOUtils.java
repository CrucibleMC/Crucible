package io.github.cruciblemc.forgegradle.tasks.signum;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

class IOUtils {

  public static byte[] readStreamFully(InputStream is) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.max(8192, is.available()));
    byte[] buffer = new byte[8192];
    int read;
    while ((read = is.read(buffer)) >= 0) {
      baos.write(buffer, 0, read);
    }
    return baos.toByteArray();
  }

  public static ClassNode readClassFromBytes(byte[] bytes) {
    ClassNode classNode = new ClassNode();
    ClassReader classReader = new ClassReader(bytes);
    classReader.accept(classNode, 0);
    return classNode;
  }

  public static byte[] writeClassToBytes(ClassNode classNode) {
    ClassWriter classWriter = new ClassWriter(0);
    classNode.accept(classWriter);
    return classWriter.toByteArray();
  }

  public static int getSecondToLastIndexOf(String string, char character) {
    String temp = string.substring(0, string.lastIndexOf(character));
    return temp.lastIndexOf(character);
  }

}