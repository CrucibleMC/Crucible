package com.juanmuscaria.uncode;

import com.google.common.io.ByteStreams;
import com.juanmuscaria.uncode.cleaners.ClassCleaner;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ASMCodeRemover {

  private static final Logger logger
          = LoggerFactory.getLogger(ASMCodeRemover.class);

  /**
   * Removes all the code, assets, and private elements from given jar,
   * keeping only public classes, methods and fields with no code body.
   *
   * @param jarFile    the jar file to process
   * @param outputFile the output file
   * @param overwrite  if the output file should be overwritten if it already exists
   * @return a map with jarEntry-reason for all entries from the input jar that where removed (resources and class files)
   * @throws IOException if an I/O error occurs
   */
  public static Map<String, String> removeContent(Path jarFile, Path outputFile, boolean overwrite) throws IOException {
    if (!Files.exists(jarFile)) {
      throw new IllegalArgumentException("Input file does not exist");
    } else if (!Files.isReadable(jarFile)) {
      throw new IllegalArgumentException("Input file is not readable");
    } else if (Files.exists(outputFile) && !overwrite) {
      throw new IllegalArgumentException("Output file already exists");
    }

    var failedEntries = new LinkedHashMap<String, String>();
    try (var out = new ZipOutputStream(Files.newOutputStream(outputFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING))) {
      try (var zip = new ZipFile(jarFile.toFile())) {
        Collections.list(zip.entries()).iterator().forEachRemaining(entry -> {
          if (entry.getName().endsWith(".class")) {
            try {
              var classBytes = processClass(ByteStreams.toByteArray(zip.getInputStream(entry)));
              var newEntry = new ZipEntry(entry.getName());
              out.putNextEntry(newEntry);
              out.write(classBytes);
              out.closeEntry();
            } catch (Exception e) {
              failedEntries.put(entry.getName(), e.getMessage());
              logger.debug("Failed to process class: {}", entry.getName());
              logger.debug("Exception:", e);
            }
          } else if (!entry.getName().endsWith("/")) {
            failedEntries.put(entry.getName(), "Not a class file");
          }
        });
      } catch (ZipException e) {
        throw new IllegalArgumentException("Input file is corrupted or not a valid jar file: " + e.getMessage(), e);
      }
    }

    return failedEntries;
  }

  /**
   * Removes all the code from a class, keeping only its public members without any code body.
   *
   * @param classBytes input class bytes
   * @return the processed class bytes
   * @throws IllegalArgumentException if the class is not readable by the current ASM version,
   *                                  if the class is synthetic or if the class is not public
   */
  public static byte[] processClass(byte[] classBytes) throws IllegalArgumentException {
    var classReader = new ClassReader(classBytes);
    var classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
    classReader.accept(new ClassCleaner(classWriter), 0);
    return classWriter.toByteArray();
  }
}
