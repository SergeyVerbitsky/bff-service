package com.verbitsky.testutil;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class TestDataReader {
    public static List<String> readDataFile(String resourceName) throws IOException, URISyntaxException {
        URL resourceUrl = TestDataReader.class.getClassLoader().getResource(resourceName);
        return Files.readAllLines(Path.of(resourceUrl.toURI()), Charset.defaultCharset());
    }
}
