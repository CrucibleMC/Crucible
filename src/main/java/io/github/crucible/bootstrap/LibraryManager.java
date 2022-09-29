package io.github.crucible.bootstrap;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class LibraryManager {

    public static void downloadMavenLibraries(Path baseDir, String[] repos, String... libraries) throws InterruptedException {
        if (repos.length == 0) {
            throw new IllegalArgumentException("A repo url list must be provided");
        }

        URI[] reposUri = new URI[repos.length];
        for (int i = 0; i < repos.length; i++) {
            reposUri[i] = URI.create(repos[i]);
        }

        ForkJoinPool pool = new ForkJoinPool((Runtime.getRuntime().availableProcessors() * 2) + 1);
        List<DownloadTask<Boolean>> tasks = new ArrayList<>(libraries.length);
        for (String library : libraries) {
            tasks.add(makeMavenDownloadTask(baseDir, reposUri, library));
        }

        DownloadProgressBar progressBar = new DownloadProgressBar(tasks);
        progressBar.start();
        List<Future<Boolean>> futures = pool.invokeAll(tasks);
        progressBar.finish();

        boolean failed = false;
        for (Future<Boolean> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                failed = true;
                e.printStackTrace();
            }
        }

        if (failed) {
            throw new RuntimeException("Failed to download one or more essential files for the server, check your logs for more information");
        }
    }

    private static DownloadTask<Boolean> makeMavenDownloadTask(Path baseDir, URI[] repos, String library) {
        String[] identifiers = library.split(":");
        if (identifiers.length != 3) {
            throw new IllegalArgumentException("Invalid identifier " + library);
        }
        return new DownloadTask<Boolean>() {
            final String jarRelativeName = String.format("./%1$s/%2$s/%3$s/%2$s-%3$s.jar", identifiers[0].replace('.', '/'),
                    identifiers[1], identifiers[2]);
            final Path jarFile = baseDir.resolve(jarRelativeName).normalize().toAbsolutePath();
            final Path checksumFie = baseDir.resolve(jarRelativeName + ".md5").normalize().toAbsolutePath();
            boolean complete;
            CountableInputStream countableInputStream;
            AtomicInteger counter;

            @Override
            public double getProgress() {
                if (complete) {
                    return 100;
                } else if (countableInputStream != null) {
                    return countableInputStream.getPercentage();
                } else {
                    return 0;
                }
            }

            @Override
            public boolean isInProgress() {
                return !complete && countableInputStream != null;
            }

            @Override
            public String displayName() {
                return jarFile.getFileName().toString();
            }

            @Override
            public void offerFinishCounter(AtomicInteger counter) {
                this.counter = counter;
            }

            @Override
            public Boolean call() throws Exception {
                if (Files.isRegularFile(jarFile) && Files.isRegularFile(checksumFie)) {
                    String checksum = String.join("", Files.readAllLines(checksumFie));
                    if (checksum.equals("skip")) {
                        return false;
                    }

                    MessageDigest digest = MessageDigest.getInstance("MD5");
                    if (checksum.equalsIgnoreCase(encodeHex(digest.digest(Files.readAllBytes(jarFile))))) {
                        return false;
                    }
                }

                List<Exception> downloadErrors = new ArrayList<>();
                URL file = null;
                for (URI repo : repos) {
                    try {
                        MessageDigest digest = MessageDigest.getInstance("MD5");
                        URI uriPath = repo.resolve(jarRelativeName).normalize();
                        file = uriPath.toURL();
                        HttpURLConnection connection = (HttpURLConnection) file.openConnection();
                        connection.setRequestMethod("GET");
                        connection.connect();
                        if (connection.getResponseCode() >= 200 || connection.getResponseCode() <= 399) {
                            try (DigestInputStream in = new DigestInputStream(countableInputStream =
                                    new CountableInputStream(connection.getInputStream(), connection.getContentLength()), digest)) {
                                Files.createDirectories(jarFile.getParent());
                                Files.copy(in, jarFile, StandardCopyOption.REPLACE_EXISTING);
                                Files.copy(new ByteArrayInputStream(encodeHex(digest.digest()).getBytes(StandardCharsets.UTF_8)),
                                        checksumFie, StandardCopyOption.REPLACE_EXISTING);
                            }
                        } else {
                            throw new IOException(String.format("Bad response (%s) from %s", connection.getResponseMessage(), file));
                        }
                        connection.disconnect();
                        complete = true;
                        counter.incrementAndGet();
                        return true;
                    } catch (MalformedURLException | NoSuchAlgorithmException e) {
                        throw new RuntimeException("An exception that should never happen actually happened", e);
                    } catch (IOException e) {
                        e.addSuppressed(new IOException("Unable to download from url " + file));
                        downloadErrors.add(e);
                    }
                }
                System.out.println("The following errors happened while trying to download " + jarFile.getFileName());
                for (Exception error : downloadErrors) {
                    error.printStackTrace();
                }
                throw new IllegalStateException("Unable to download " + jarFile.getFileName());
            }
        };
    }

    public static boolean checkIntegrity(Path libraryRoot, String[] neededLibraries) throws IOException, NoSuchAlgorithmException {
        for (String neededLibrary : neededLibraries) {
            String[] identifiers = neededLibrary.split(":");
            if (identifiers.length != 3) {
                throw new IllegalArgumentException("Invalid identifier " + neededLibrary);
            }
            final String jarRelativeName = String.format("./%1$s/%2$s/%3$s/%2$s-%3$s.jar", identifiers[0].replace('.', '/'),
                    identifiers[1], identifiers[2]);
            final Path jarFile = libraryRoot.resolve(jarRelativeName).normalize().toAbsolutePath();
            final Path checksumFie = libraryRoot.resolve(jarRelativeName + ".md5").normalize().toAbsolutePath();
            if (Files.isRegularFile(jarFile) && Files.isRegularFile(checksumFie)) {
                String checksum = String.join("", Files.readAllLines(checksumFie));
                if (checksum.equals("skip")) {
                    System.out.println("[Crucible] Skipping verification of " + neededLibrary);
                    continue;
                }

                MessageDigest digest = MessageDigest.getInstance("MD5");
                if (!checksum.equalsIgnoreCase(encodeHex(digest.digest(Files.readAllBytes(jarFile))))) {
                    return false;
                }
            } else {
                return false;
            }

        }
        return true;
    }

    //==================================================================================================================
    // From https://stackoverflow.com/a/58118078
    //==================================================================================================================
    private static final char[] LOOKUP_TABLE_LOWER = new char[]{0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66};
    private static final char[] LOOKUP_TABLE_UPPER = new char[]{0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46};

    public static String encodeHex(byte[] byteArray, boolean upperCase, ByteOrder byteOrder) {

        // our output size will be exactly 2x byte-array length
        final char[] buffer = new char[byteArray.length * 2];

        // choose lower or uppercase lookup table
        final char[] lookup = upperCase ? LOOKUP_TABLE_UPPER : LOOKUP_TABLE_LOWER;

        int index;
        for (int i = 0; i < byteArray.length; i++) {
            // for little endian we count from last to first
            index = (byteOrder == ByteOrder.BIG_ENDIAN) ? i : byteArray.length - i - 1;

            // extract the upper 4 bit and look up char (0-A)
            buffer[i << 1] = lookup[(byteArray[index] >> 4) & 0xF];
            // extract the lower 4 bit and look up char (0-A)
            buffer[(i << 1) + 1] = lookup[(byteArray[index] & 0xF)];
        }
        return new String(buffer);
    }

    public static String encodeHex(byte[] byteArray) {
        return encodeHex(byteArray, false, ByteOrder.BIG_ENDIAN);
    }
    //==================================================================================================================

    public static class CountableInputStream extends FilterInputStream {

        private final long size;
        private long readCount = 0;

        public double getPercentage() {
            if (size > 1) {
                return ((readCount * 100.0) / size);
            } else {
                return 0;
            }
        }

        public CountableInputStream(InputStream in, long size) {
            super(in);
            this.size = size;
        }

        @Override
        public int read() throws IOException {
            int count = in.read();
            if (count >= 0) {
                readCount++;
            }

            return count;
        }

        @Override
        public int read(@NotNull byte[] b) throws IOException {
            int count = in.read(b);
            if (count > 0) {
                readCount += count;
            }

            return count;
        }

        @Override
        public int read(@NotNull byte[] b, int off, int len) throws IOException {
            int count = in.read(b, off, len);

            if (count > 0) {
                readCount += count;
            }

            return count;
        }

        @Override
        public long skip(long n) throws IOException {
            long count = in.skip(n);
            if (count > 0) {
                readCount += count;
            }

            return count;
        }


        @Override
        public synchronized void reset() throws IOException {
            in.reset();
            readCount = size - in.available();
        }
    }

    public interface DownloadTask<T> extends Callable<T>, ProgressiveObject {
    }
}
