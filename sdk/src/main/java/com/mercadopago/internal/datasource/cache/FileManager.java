package com.mercadopago.internal.datasource.cache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Helper class to do operations on regular files/directories.
 */

public final class FileManager {

    public FileManager() {
    }

    /**
     * Writes a file to Disk.
     * This is an I/O operation, so it is recommended to
     * perform this operation using another thread.
     *
     * @param file The file to write to Disk.
     */
    /* default */ synchronized void writeToFile(final File file, final String fileContent) {
        if (!file.exists()) {
            try {
                final FileWriter writer = new FileWriter(file);
                writer.write(fileContent);
                writer.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Reads a content from a file.
     * This is an I/O operation and this method, so it is recommended to
     * perform the operation using another thread.
     *
     * @param file The file to read from.
     * @return A string with the content of the file.
     */
    /* default */ synchronized String readFileContent(final File file) {
        final StringBuilder fileContentBuilder = new StringBuilder();
        if (file.exists()) {
            String stringLine;
            try {
                final FileReader fileReader = new FileReader(file);
                final BufferedReader bufferedReader = new BufferedReader(fileReader);
                while ((stringLine = bufferedReader.readLine()) != null) {
                    fileContentBuilder.append(stringLine).append("\n");
                }
                bufferedReader.close();
                fileReader.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return fileContentBuilder.toString();
    }

    /**
     * Returns a boolean indicating whether this file can be found on the underlying file system.
     *
     * @param file The file to check existence.
     * @return true if this file exists, false otherwise.
     */
    /* default */ synchronized boolean exists(final File file) {
        return file.exists();
    }

    /**
     * Warning: Deletes the content of a directory.
     * This is an I/O operation, so it is recommended to
     * perform the operation using another thread.
     *
     * @param file The directory which its content will be deleted.
     */
    /* default */ synchronized boolean removeFile(final File file) {
        boolean result = false;
        if (file.exists()) {
            result = file.delete();
        }
        return result;
    }
}