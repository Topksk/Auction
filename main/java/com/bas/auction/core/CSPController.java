package com.bas.auction.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/cspreport")
public class CSPController {
    private final Conf conf;

    @Autowired
    public CSPController(Conf conf) {
        this.conf = conf;
    }

    @RequestMapping(method = POST)
    public void report(HttpServletRequest req) throws IOException {
        String cspReportPath = conf.getCspReportPath();
        Path dir = Paths.get(cspReportPath);
        if (!Files.exists(dir) || !Files.isDirectory(dir))
            Files.createDirectory(dir);
        if (Files.exists(dir) && Files.isDirectory(dir)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss.SSS");
            String name = cspReportPath + File.separator + sdf.format(new Date()) + ".json";
            Path report = Paths.get(name);
            try (Writer w = Files.newBufferedWriter(report, StandardCharsets.UTF_8); Reader r = req.getReader()) {
                int count;
                char[] cbuf = new char[8192];
                while ((count = r.read(cbuf)) > 0) {
                    w.write(cbuf, 0, count);
                }
            }
        }
    }
}
