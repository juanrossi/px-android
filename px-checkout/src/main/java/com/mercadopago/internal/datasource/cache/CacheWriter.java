package com.mercadopago.internal.datasource.cache;

import java.io.File;

/**
 * {@link Runnable} class for writing to disk.
 */
/* default */ final class CacheWriter implements Runnable {
    private final FileManager fileManager;
    private final File fileToWrite;
    private final String fileContent;

    /* default */ CacheWriter(final FileManager fileManager, final File fileToWrite, final String fileContent) {
        this.fileManager = fileManager;
        this.fileToWrite = fileToWrite;
        this.fileContent = fileContent;
    }

    @Override
    public void run() {
        fileManager.writeToFile(fileToWrite, fileContent);
    }
}
