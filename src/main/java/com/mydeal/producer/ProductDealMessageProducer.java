package com.mydeal.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mydeal.model.ProductDeal;
import com.mydeal.parser.SourceDealsParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

@Slf4j
public class ProductDealMessageProducer {

    private final ObjectMapper mapper;
    private final SourceDealsParser parser;
    private final KafkaProducer<String, String> producer;
    private final String TOPIC ="product_deals";

    public ProductDealMessageProducer(SourceDealsParser parser) {
        this.parser = parser;
        this.mapper = new ObjectMapper();
        this.producer = new KafkaProducer(getProducerConfig());
    }


    public void produce() {
        List<ProductDeal> deals = this.parser.parse();
        log.info("Received: {} ",deals.size());
        List<Future<RecordMetadata>> recordMetaDataList = new ArrayList<>();
        for(ProductDeal deal : deals){
            try {
                recordMetaDataList.add(sendMessage(deal));
            } catch (JsonProcessingException e) {
                log.error("Failed to process {}, skipping failed record",deal.toString());
                e.printStackTrace();
            }
        }
        deals.stream().forEach(deal-> {
            try {
                sendMessage(deal);
            } catch (JsonProcessingException e) {

                e.printStackTrace();
            }
        });
        log.info("Published All deals into kafka.... Waiting for new file");
        producer.close();
    }

    public Future<RecordMetadata> sendMessage(ProductDeal deal) throws JsonProcessingException {
        log.info("Stringify Deal");
        String message = mapper.writeValueAsString(deal);
        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, deal.getItem(),message);
        Future<RecordMetadata> result = producer.send(record);
        return result;
    }


    /**
     * TODO : Read config from properties
     *
     * @return
     */
    private Map<String, String> getProducerConfig() {
        Map<String, String> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092,localhost:9093,localhost:9094");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return config;
    }


}
