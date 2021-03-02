package com.mydeal.parser;

import com.mydeal.model.ProductDeal;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CsvSourceDealsParser implements SourceDealsParser {
    DateTimeFormatter simpleDateFormat = DateTimeFormatter.ofPattern("M/d/yyyy");
    Predicate<String> ignorePayment = (line) -> line.contains("PAYMENT - THANK YOU");
    Predicate<String> ignorePostedDate = (line) -> line.contains("Posted Date");


    private SourceReader csvReader;

    public CsvSourceDealsParser(SourceReader csvReader) {
        this.csvReader = csvReader;
    }

    @Override
    public List<ProductDeal> parse() {
        List<String> transactions = (List<String>) csvReader.read();
        return parseSource(transactions);

    }

    private List<ProductDeal> parseSource(List<String> lines) {

        return lines.stream()
                .filter(ignorePayment.or(ignorePostedDate).negate())
                .map(this::parseLine).filter(Objects::nonNull).collect(Collectors.toList());

    }

    private ProductDeal parseLine(String line) {
        try {
            String[] str = line.split(",");

            return new ProductDeal(str[0], str[1], str[2], str[3], str[4], str[5]);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return null;
    }

    /**
     * externalize mapping , this will be usefull incase you have payee appears with different names in transactions
     * e.g. AMZON,amzn, WallMart, WL-MART etc
     *
     * @param payeeNameInTransaction - Payeename which has multiple names like Amazon,amzn
     * @return returns mapping information
     */
    private String payeeMapping(String payeeNameInTransaction) {
        String amzn = "amazon,amzn";
        Optional<String> payeeName = Arrays.stream(amzn.split(",")).filter(str -> payeeNameInTransaction.toLowerCase().contains(str)).findFirst();
        if (payeeName.isPresent())
            return "Amazon";
        return payeeNameInTransaction;


    }

}
