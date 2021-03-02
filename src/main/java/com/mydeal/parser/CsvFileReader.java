package com.mydeal.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CsvFileReader implements SourceReader {

    private final String resourcePath;

    public CsvFileReader(String resourcePath) {
        this.resourcePath = resourcePath;
    }


    @Override
    public List<String> read() {
        Path path = Path.of(resourcePath);
        if (!Files.isDirectory(path)) {
            try {
                return Files.readAllLines(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

            List<String> allTransactionRecords = new ArrayList<>();
            try {
                Stream<Path> filePath;
                filePath = Files.list(path);
                filePath.forEach(newPath -> {
                    try {
                        List<String> templines = Files.readAllLines(newPath);
                        allTransactionRecords.addAll(templines);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                });
                return allTransactionRecords;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return null;
    }
}
