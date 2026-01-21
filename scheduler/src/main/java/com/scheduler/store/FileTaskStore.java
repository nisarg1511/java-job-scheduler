package com.scheduler.store;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Append-only, crash-safe task log.
 */
public final class FileTaskStore implements TaskStore {

    private final Path logFile;
    private final ObjectMapper mapper; // Removed inline initialization

    public FileTaskStore(Path logFile) throws IOException {
        this.logFile = logFile;

        // --- CONFIGURE MAPPER ---
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        // This ensures the Instant is saved as a readable ISO-8601 string
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Ensure parent directories exist
        Files.createDirectories(logFile.getParent());

        // Create file if it does not exist
        if (!Files.exists(logFile)) {
            Files.createFile(logFile);
        }
    }

    @Override
    public void append(TaskRecord record) throws IOException {
        try (FileChannel fs = new FileOutputStream(logFile.toFile(), true).getChannel()) {
            byte[] json = mapper.writeValueAsBytes(record);
            String jsonStr = new String(json, StandardCharsets.UTF_8);
            System.out.println("Appending JSON: " + jsonStr);  // Debug print

            byte[] newLine = "\n".getBytes();
            ByteBuffer buffer = ByteBuffer.allocate(json.length + newLine.length);
            buffer.put(json);
            buffer.put(newLine);
            buffer.flip();

            fs.write(buffer);
            fs.force(true);
            System.out.println("Append successful for task ID: " + record.getTaskId());
        } catch (IOException e) {
            System.err.println("Error while appending for task ID " + record.getTaskId() + ": " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<TaskRecord> loadAll() throws IOException {
        List<TaskRecord> records = new ArrayList<>();
        long lastGoodPos = 0;
        boolean needsTruncation = false;
        long truncateAt = 0;

        try (RandomAccessFile raf = new RandomAccessFile(logFile.toFile(), "r")) {
            while (true) {
                byte[] lineBytes;

                try {
                    lineBytes = readNextLineBytes(raf);
                    if (lineBytes == null) {
                        break; // clean EOF
                    }

                    String json = new String(lineBytes, StandardCharsets.UTF_8);
                    TaskRecord record = mapper.readValue(json, TaskRecord.class);

                    records.add(record);
                    lastGoodPos = raf.getFilePointer();

                } catch (IOException e) {
                    System.err.println("Corrupted record found at position " + lastGoodPos + ": " + e.getMessage());
                    needsTruncation = true;
                    truncateAt = lastGoodPos;
                    break;
                }
            }
        } // Close the read-only file here

        // Truncate OUTSIDE the read operation, and ONLY if corruption was detected
        if (needsTruncation && truncateAt > 0) { // Added check to prevent truncating to 0
            System.out.println("Truncating file to " + truncateAt + " bytes due to corruption");
            try (RandomAccessFile truncRaf = new RandomAccessFile(logFile.toFile(), "rw")) {
                truncRaf.setLength(truncateAt);
            }
        }

        return records;
    }

    private byte[] readNextLineBytes(RandomAccessFile raf) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        while (true) {
            int b = raf.read();
            if (b == -1) {
                if (buffer.size() == 0) {
                    return null; // clean EOF
                }
                throw new IOException("Unexpected EOF while reading record");
            }

            if (b == '\n') {
                break;
            }
            // Logic cleanup: ensure we don't write the character twice
            if (b != '\r') {
                buffer.write(b);
            }
        }

        return buffer.toByteArray();
    }
}
