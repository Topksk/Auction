package com.bas.auction.core.utils;

import com.bas.auction.core.crypto.ZipContent;
import com.bas.auction.docfiles.dto.DocFile;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Part;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtils {
	private final static Logger logger = LoggerFactory.getLogger(ZipUtils.class);
	private static final int BUFFER_SIZE = 8192;
	private Map<String, Path> content = new HashMap<>();
	private Properties fileNames = new Properties();

    public ZipContent unzipArchiveWithSignedFile(Part filePart) throws IOException {
        unzipArchive(filePart.getInputStream());
        String fileName = fileNames.getProperty("file_1");
        return new ZipContent(content.get("file_1"), content.get("file_2"), content.get("file_3"), fileName);
    }

    public ZipContent unzipArchiveWithSignature(Part filePart) throws IOException {
        unzipArchive(filePart.getInputStream());
        return new ZipContent(null, content.get("file_1"), content.get("file_2"), null);
    }

    public ZipContent unzipArchiveWithUnsignedFile(Part filePart) throws IOException {
        unzipArchive(filePart.getInputStream());
        String fileName = fileNames.getProperty("file_1");
        return new ZipContent(content.get("file_1"), null, null, fileName);
    }

    public void unzipArchive(InputStream fis) throws IOException {
        ZipEntry entry;
		try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis))) {
			while ((entry = zis.getNextEntry()) != null) {
                Path entryFile = writeZipEntryToFile(zis, entry);
                content.put(entry.getName(), entryFile);
			}
		} finally {
			fis.close();
		}
		extractFileNames();
	}

    private Path writeZipEntryToFile(ZipInputStream zis, ZipEntry entry) throws IOException {
        int count;
        byte data[] = new byte[BUFFER_SIZE];
        Path entryFile = Files.createTempFile("unzip_" + entry.getName(), null);
        logger.trace("extracting zip entry {} into file {}", entry.getName(), entryFile);
        try (OutputStream dest = new BufferedOutputStream(Files.newOutputStream(entryFile))) {
            while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1) {
                dest.write(data, 0, count);
            }
        }
        return entryFile;
    }

	public static File zipFiles(List<DocFile> files) throws ZipException, IOException {
		ZipFile zipFile = new ZipFile(getTmpZip());
		zipFile.setFileNameCharset("IBM866");
		ZipParameters parameters = new ZipParameters();
		parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
		parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
		for (DocFile doc : files) {
			try (InputStream is = Files.newInputStream(doc.getFile())) {
				parameters.setFileNameInZip(doc.getFileName());
				parameters.setSourceExternalStream(true);
				zipFile.addStream(is, parameters);
				logger.trace("added into zip: {}", doc.getFileName());
			}
		}
		return zipFile.getFile();
	}

	private static File getTmpZip() {
		int tryTimes = 100, i = 0;
		String tmpDir = System.getProperty("java.io.tmpdir") + File.separator;
		File tmpZip = new File(tmpDir + System.nanoTime() + ".zip");
		logger.trace("zipping into: {}", tmpZip);
		while (++i <= tryTimes && tmpZip.exists())
			tmpZip = new File(tmpDir + System.nanoTime() + ".zip");
		return tmpZip;
	}

	private void extractFileNames() throws IOException {
		try (InputStream fis = Files.newInputStream(content.get("file_name_mappings.xml"))) {
			fileNames.loadFromXML(fis);
		}
		logger.trace("extracted: {}", fileNames);
	}

	public void deleteTmpFiles() throws IOException {
		if (content != null)
			for (Path p : content.values()) {
				if (Files.deleteIfExists(p))
					logger.trace("removed: {}", p);
				else
					logger.error("can't remove tmp file: {}", p);
			}
	}
}
