package com.mydeal;

import com.mydeal.parser.CsvFileReader;
import com.mydeal.parser.CsvSourceDealsParser;
import com.mydeal.parser.SourceDealsParser;
import com.mydeal.producer.ProductDealMessageProducer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

@Slf4j
public class DealProducerApplication {
    public static void main(String[] args) throws InterruptedException {
        log.info("Main Application started ");
        CountDownLatch latch = new CountDownLatch(1);
        DealsFileWatcher fileWatcher = new DealsFileWatcher("test/", latch);
        Thread watcherServiceThread = new Thread(fileWatcher,"Filewatcher-Thread");
        watcherServiceThread.start();
        log.info("Watcher ServiceStarted ");
        latch.await();
        log.info("Main Application Existing");
    }
}

/**
 * Class Should be responsible for watching data directory for new file.
 * If found, records will be send to kafka topic
 */
@Slf4j
class DealsFileWatcher implements Runnable {
    private final String resourcePath;
    private final CountDownLatch latch;

    public DealsFileWatcher(String resourcePath, CountDownLatch latch) {
        this.resourcePath = resourcePath;
        this.latch = latch;
    }

    @Override
    public void run() {
        try {
            log.info("Watcher Thread Started in background");
            WatchService watcher = FileSystems.getDefault().newWatchService();
            Watchable w = Path.of(resourcePath);
            w.register(watcher, ENTRY_CREATE);
            while (true) {
                WatchKey key = watcher.poll(60, TimeUnit.MILLISECONDS);

                if (key == null) continue;
                List<WatchEvent<?>> events = key.pollEvents();

                for (WatchEvent event : events) {
                    WatchEvent.Kind eventKind = event.kind();
                    if (eventKind == ENTRY_CREATE) {
                        log.info("Watcher service detected new Event");
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path filename = ev.context();
                        log.info("Processing file {}", filename);
                        SourceDealsParser parser = new CsvSourceDealsParser(new CsvFileReader(resourcePath + filename));
                        ProductDealMessageProducer messageProducer = new ProductDealMessageProducer(parser);
                        messageProducer.produce();
                        log.info("Processing completed for {}", filename);

                    }
                }
            }

        } catch (IOException | InterruptedException e) {
            log.error("Exception {} ", e.getMessage());
            e.printStackTrace();
            latch.countDown();
        }
    }
}
