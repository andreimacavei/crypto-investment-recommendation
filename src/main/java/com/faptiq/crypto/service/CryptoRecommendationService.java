package com.faptiq.crypto.service;

import com.faptiq.crypto.dao.CryptoRecordDao;
import com.faptiq.crypto.entity.CryptoRecord;
import com.faptiq.crypto.pojo.CryptoDetails;
import com.faptiq.crypto.pojo.CryptoDetailsComparator;
import com.faptiq.crypto.pojo.CryptoSymbol;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CryptoRecommendationService {

    private CryptoRecordDao cryptoRepository;

    @Value("classpath:prices")
    private Resource resourcesDir;

    @Autowired
    ResourceLoader resourceLoader;

    private Map<CryptoSymbol, CryptoDetails> cryptoDetailsPerMonthCache;

    private Map<CryptoSymbol, HashMap<String, ArrayList<Double>>> mapOfMinMaxPerCryptoPerDate;

    private CryptoDetailsComparator cryptoDetailsComparator;

    private LocalDate startDate;

    private LocalDate endDate;

    @Autowired
    public CryptoRecommendationService(CryptoRecordDao cryptoRepository) {
        this.cryptoRepository = cryptoRepository;
    }

    @PostConstruct
    private void setupDb() {
        cryptoDetailsPerMonthCache = new HashMap<>();
        cryptoDetailsComparator = new CryptoDetailsComparator();
        mapOfMinMaxPerCryptoPerDate = new HashMap<>();
        startDate = LocalDate.parse("2022-01-01");
        endDate = LocalDate.parse("2022-01-31");
        List<CryptoRecord> cryptoRecords = readCryptoRecords();
        // save records to DB
        saveAll(cryptoRecords);
    }


    public CryptoRecord save(CryptoRecord record) {
        return cryptoRepository.save(record);
    }

    public void saveAll(List<CryptoRecord> records) {
        cryptoRepository.saveAll(records);
    }

    public boolean validate(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public CryptoDetails getCryptoDetails(CryptoSymbol cryptoSymbol) {

        // Return from cache if exists
        if (cryptoDetailsPerMonthCache.containsKey(cryptoSymbol)) {
            return cryptoDetailsPerMonthCache.get(cryptoSymbol);
        }

        // Check if a new records file was added and update cache
        CryptoDetails cryptoDetails = computeCryptoDetails(cryptoSymbol);

        return cryptoDetails;
    }


    public List<CryptoSymbol> getCryptoSortedByNormalizedRange() {
        List<CryptoDetails> sortedList = cryptoDetailsPerMonthCache.values().stream().sorted(cryptoDetailsComparator).toList();
//        System.out.println("Sorted Normalized Values:");
//        sortedList.forEach(crypto -> System.out.println(crypto.getCryptoSymbol() + " - " + crypto.getNormalizedRange()));
        return cryptoDetailsPerMonthCache.values().stream()
                .sorted(cryptoDetailsComparator)
                .map(CryptoDetails::getCryptoSymbol)
                .collect(Collectors.toList());

    }

    public CryptoSymbol getHighestNormalizedRange(LocalDate date) {
        String dateString = date.toString();
        double highestRange = Double.MIN_VALUE;
        CryptoSymbol cryptoSymbol = null;

        for (Map.Entry<CryptoSymbol, HashMap<String, ArrayList<Double>>> entry: mapOfMinMaxPerCryptoPerDate.entrySet()) {
            ArrayList<Double> minMaxValues = entry.getValue().get(dateString);
            // We may not have records for a day
            if (minMaxValues != null) {
                double normalizedRange = minMaxValues.get(1) != Double.MIN_VALUE ?
                        (minMaxValues.get(1) - minMaxValues.get(0)) / minMaxValues.get(0) : 0;
//                System.out.println("Crypto:" + entry.getKey().toString() + " - normalizedRange: " + normalizedRange);

                if (normalizedRange > highestRange) {
                    highestRange = normalizedRange;
                    cryptoSymbol = entry.getKey();
                }
            }
        }

        return cryptoSymbol;
    }


    private CryptoDetails computeCryptoDetails(CryptoSymbol cryptoSymbol) {
        CryptoDetails cryptoDetails = null;
        String fileName = cryptoSymbol.toString() + "_values.csv";
        Resource resource = resourceLoader.getResource("classpath:" + fileName);

        if (resource.exists()) {
            readRecordsFromFile(resource.getFilename());
            cryptoDetails = cryptoDetailsPerMonthCache.get(cryptoSymbol);

        } else {
            return null;
        }
        return cryptoDetails;
    }

    private List<CryptoRecord> readCryptoRecords() {
        List<CryptoRecord> allRecords = new ArrayList<>();
        try {
            List<String> files = getResourceFiles();
            for (String file : files) {
                List<CryptoRecord> records = readRecordsFromFile(file);
                allRecords.addAll(records);
            }

        } catch (IOException e) {
            System.out.println("Invalid resource path - " + resourcesDir.toString());
        }
        return allRecords;
    }


    private List<CryptoRecord> readRecordsFromFile(String fileName) {
        List<CryptoRecord> records = new ArrayList<>();
        try {
            File file = ResourceUtils.getFile("classpath:prices/" + fileName);

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                // Skip the first line (header)
                br.readLine();
                String line;

                CryptoDetails cryptoDetails = null;
                CryptoSymbol cryptoSymbol = null;
                Long newest = Long.MIN_VALUE, oldest = Long.MAX_VALUE;
                Double minValue = Double.MAX_VALUE, maxValue = 0.0;

                // For each record in a crypto file
                while ((line = br.readLine()) != null) {
                    String[] tokens = line.split(",");
                    long timestamp = Long.parseLong(tokens[0]);
                    cryptoSymbol = CryptoSymbol.valueOf(tokens[1]);
                    Double price = Double.valueOf(tokens[2]);
                    int timeInSecs = Math.toIntExact(timestamp / 1000);

                    LocalDate localDate = null;
                    try {
                        localDate = Timestamp.from(Instant.ofEpochSecond(timeInSecs)).toLocalDateTime().toLocalDate();

                    } catch (IllegalArgumentException e) {
                        // skip record with bad formatted timestamp
                        System.out.println("Cannot convert to local date from timestamp:" + timestamp);
                        continue;
                    }

                    CryptoRecord cryptoRecord = new CryptoRecord(timestamp, cryptoSymbol, price, localDate);
                    records.add(cryptoRecord);

                    // Calculate the min/max/oldest/newest values for a month period so that we can have them cached
                    if (cryptoRecord.getTimestamp() > newest)
                        newest = cryptoRecord.getTimestamp();
                    if (cryptoRecord.getTimestamp() < oldest)
                        oldest = cryptoRecord.getTimestamp();
                    if (minValue > cryptoRecord.getPrice())
                        minValue = cryptoRecord.getPrice();
                    if (maxValue < cryptoRecord.getPrice())
                        maxValue = cryptoRecord.getPrice();

                    // Init map of min/max prices per crypto (per date)
                    if (!mapOfMinMaxPerCryptoPerDate.containsKey(cryptoSymbol)) {
                        mapOfMinMaxPerCryptoPerDate.put(cryptoSymbol, new HashMap<>());
                    }

                    // Update cache map for min/max prices per current date (per current crypto):
                    // set arrayList[0] = min price for current date
                    // and arrayList[1] = max price for current date
                    HashMap<String, ArrayList<Double>> mapOfMinMax = mapOfMinMaxPerCryptoPerDate.get(cryptoSymbol);
                    String currentDate = localDate.toString();

                    // If this is the first read on current day for this record - init the list that will hold min/max prices
                    if (!mapOfMinMax.containsKey(currentDate)) {
                        mapOfMinMax.put(currentDate, new ArrayList<>());
                        mapOfMinMax.get(currentDate).add(Double.MAX_VALUE);
                        mapOfMinMax.get(currentDate).add(Double.MIN_VALUE);
                    }

                    if (cryptoRecord.getPrice() < mapOfMinMax.get(currentDate).get(0)) {
                        mapOfMinMax.get(currentDate).set(0, cryptoRecord.getPrice());
                    }
                    if (cryptoRecord.getPrice() > mapOfMinMax.get(currentDate).get(1)) {
                        mapOfMinMax.get(currentDate).set(1, cryptoRecord.getPrice());
                    }
                }

                // update cache of lowest/oldest/min/max values per entire month
                cryptoDetails = new CryptoDetails(cryptoSymbol, minValue, maxValue, oldest, newest);
                cryptoDetailsPerMonthCache.put(cryptoSymbol, cryptoDetails);
            }

        } catch (IOException e) {
            System.out.println("Exception when reading files.\n" + e.getMessage());
        }

        return records;
    }


    private List<String> getResourceFiles() throws IOException {
        try (Stream<Path> stream = Files.list(Paths.get(resourcesDir.getURI()))) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        }
    }

}
