package java.util.jar;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.SortedMap;

/**
 * Compile-time stub for the Pack200 API removed after Java 13.
 *
 * <p>The runtime implementation is supplied elsewhere; this class only lets
 * legacy Forge/FML sources compile on modern JDKs.</p>
 */
public abstract class Pack200 {
    private Pack200() {
    }

    public static Packer newPacker() {
        return new StubPacker();
    }

    public static Unpacker newUnpacker() {
        return new StubUnpacker();
    }

    public interface Packer {
        String SEGMENT_LIMIT = "pack.segment.limit";
        String KEEP_FILE_ORDER = "pack.keep.file.order";
        String EFFORT = "pack.effort";
        String DEFLATE_HINT = "pack.deflate.hint";
        String MODIFICATION_TIME = "pack.modification.time";
        String PASS_FILE_PFX = "pack.pass.file.";
        String UNKNOWN_ATTRIBUTE = "pack.unknown.attribute";
        String CLASS_ATTRIBUTE_PFX = "pack.class.attribute.";
        String FIELD_ATTRIBUTE_PFX = "pack.field.attribute.";
        String METHOD_ATTRIBUTE_PFX = "pack.method.attribute.";
        String CODE_ATTRIBUTE_PFX = "pack.code.attribute.";
        String PROGRESS = "pack.progress";
        String KEEP = "keep";
        String PASS = "pass";
        String STRIP = "strip";
        String ERROR = "error";
        String TRUE = "true";
        String FALSE = "false";
        String LATEST = "latest";

        SortedMap<String, String> properties();

        void pack(JarFile in, OutputStream out) throws IOException;

        void pack(JarInputStream in, OutputStream out) throws IOException;

        void addPropertyChangeListener(PropertyChangeListener listener);

        void removePropertyChangeListener(PropertyChangeListener listener);
    }

    public interface Unpacker {
        String DEFLATE_HINT = "unpack.deflate.hint";
        String PROGRESS = "unpack.progress";
        String TRUE = "true";
        String FALSE = "false";
        String KEEP = "keep";

        SortedMap<String, String> properties();

        void unpack(InputStream in, JarOutputStream out) throws IOException;

        void unpack(File in, JarOutputStream out) throws IOException;

        void addPropertyChangeListener(PropertyChangeListener listener);

        void removePropertyChangeListener(PropertyChangeListener listener);
    }

    private static final class StubPacker implements Packer {
        public SortedMap<String, String> properties() {
            throw unsupported();
        }

        public void pack(JarFile in, OutputStream out) throws IOException {
            throw unsupported();
        }

        public void pack(JarInputStream in, OutputStream out) throws IOException {
            throw unsupported();
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            throw unsupported();
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            throw unsupported();
        }
    }

    private static final class StubUnpacker implements Unpacker {
        public SortedMap<String, String> properties() {
            throw unsupported();
        }

        public void unpack(InputStream in, JarOutputStream out) throws IOException {
            throw unsupported();
        }

        public void unpack(File in, JarOutputStream out) throws IOException {
            throw unsupported();
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            throw unsupported();
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            throw unsupported();
        }
    }

    private static UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException("Pack200 compile-time stub should not be used at runtime");
    }
}
