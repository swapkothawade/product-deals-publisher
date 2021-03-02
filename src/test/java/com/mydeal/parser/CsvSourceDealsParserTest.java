package com.mydeal.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mydeal.model.ProductDeal;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CsvSourceDealsParserTest {
    @Test
    public void testSourceParser(){
        SourceDealsParser parser = new CsvSourceDealsParser(new CsvFileReader("data/testdata.csv"));
        List<ProductDeal> deals = parser.parse();
        assertNotNull(deals);
        assertEquals(5,deals.size());
        //deals.stream().limit(10).forEach(System.out::println);
    }
    @Test
    public void testProductDealToJson(){
        SourceDealsParser parser = new CsvSourceDealsParser(new CsvFileReader("data/testdata.csv"));
        List<ProductDeal> deals = parser.parse();
        ObjectMapper mapper = new ObjectMapper();

        deals.stream().map(deal-> {
            try {
                return mapper.writeValueAsString(deal);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        }).forEach(System.out::println);
    }
}
