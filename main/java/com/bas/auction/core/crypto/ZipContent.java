package com.bas.auction.core.crypto;

import java.nio.file.Path;

public class ZipContent {
    public final Path file;
    public final Path signatureFile;
    public final Path certificateFile;
    public final String fileName;

    public ZipContent(Path file, Path signatureFile, Path certificateFile, String fileName) {
        this.file = file;
        this.signatureFile = signatureFile;
        this.certificateFile = certificateFile;
        this.fileName = fileName;
    }
}
