package com.wallet.storage;

import com.wallet.model.Transaction;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/** Persists every transaction to a CSV file; synchronized so concurrent writers never interleave lines. */
public class TransactionLogger {
    private final Path file;

    public TransactionLogger(String fileName) {
        this.file = Paths.get(fileName);
    }

    public synchronized void log(Transaction t) {
        try (BufferedWriter w = Files.newBufferedWriter(file,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            w.write(t.toCsv());
            w.newLine();
        } catch (IOException e) {
            System.err.println("Could not write transaction log: " + e.getMessage());
        }
    }

    public synchronized List<String> readAll() {
        List<String> lines = new ArrayList<>();
        if (!Files.exists(file)) return lines;
        try (BufferedReader r = Files.newBufferedReader(file)) {
            String line;
            while ((line = r.readLine()) != null) lines.add(line);
        } catch (IOException e) {
            System.err.println("Could not read transaction log: " + e.getMessage());
        }
        return lines;
    }
}
