package com.mydeal.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mydeal.model.ProductDeal;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CsvSourceDealsParserTest {
    @Test
    public void testSourceParser() {
        SourceDealsParser parser = new CsvSourceDealsParser(new CsvFileReader("data/testdata.csv"));
        List<ProductDeal> deals = parser.parse();
        assertNotNull(deals);
        assertEquals(5, deals.size());
        //deals.stream().limit(10).forEach(System.out::println);
    }

    @Test
    public void testProductDealToJson() {
        SourceDealsParser parser = new CsvSourceDealsParser(new CsvFileReader("data/testdata.csv"));
        List<ProductDeal> deals = parser.parse();
        ObjectMapper mapper = new ObjectMapper();

        deals.stream().map(deal -> {
            try {
                return mapper.writeValueAsString(deal);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        }).forEach(System.out::println);
    }

    @Test
    @Ignore
    public void testWatchService() {
        try {
            WatchService watcher = FileSystems.getDefault().newWatchService();
            Watchable w = Path.of("test/");
            w.register(watcher, ENTRY_CREATE);
            while (true) {
                WatchKey key = watcher.poll();

                if(key == null) continue;
                List<WatchEvent<?>> events = key.pollEvents();

                for (WatchEvent event : events) {
                    WatchEvent.Kind eventKind = event.kind();
                    if (eventKind == ENTRY_CREATE) {
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path filename = ev.context();
                        SourceDealsParser parser = new CsvSourceDealsParser(new CsvFileReader("data/" + filename));
                        List<ProductDeal> deals = parser.parse();
                        System.out.println(deals);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

