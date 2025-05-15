package com.pvp.smartwatt.controller;

import com.pvp.smartwatt.model.ConsumptionModel;
import com.pvp.smartwatt.model.GenUseDataHourly;
import com.pvp.smartwatt.model.ResponseDAO;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1")

public class BusinessController {

    @PostMapping("/upload/plan1")
    public ResponseEntity<?> uploadUsageFilePlan1(
            @RequestParam(value = "file", required = true) MultipartFile csvFile,
            @RequestParam(value = "file2", required = false) MultipartFile csvFile2,
            @RequestParam(value = "file3", required = false) MultipartFile csvFile3) {
        List<GenUseDataHourly> combinedData = mapToGenUseData(csvFile);
        combinedData = mergeUsageConsuptionData(combinedData, csvFile2);
        combinedData = mergeUsageConsuptionData(combinedData, csvFile3);

        double electricityPrice = 0.221;
        double storageMonetaryFee = 0.0;
        double storagePercentageCutFee = 0.035;
        return getPriceCalculations(combinedData, electricityPrice, storageMonetaryFee, storagePercentageCutFee);
    }




    @PostMapping("/upload/plan2")
    public ResponseEntity<?> uploadUsageFilePlan2(
            @RequestParam(value = "file", required = true) MultipartFile csvFile,
            @RequestParam(value = "file2", required = false) MultipartFile csvFile2,
            @RequestParam(value = "file3", required = false) MultipartFile csvFile3) {
        List<GenUseDataHourly> combinedData = mapToGenUseData(csvFile);
        combinedData = mergeUsageConsuptionData(combinedData, csvFile2);
        combinedData = mergeUsageConsuptionData(combinedData, csvFile3);

        double electricityPrice = 0.221;
        double storageMonetaryFee = 0.065;
        double storagePercentageCutFee = 0.0;
        return getPriceCalculations(combinedData, electricityPrice, storageMonetaryFee, storagePercentageCutFee);
    }

    @PostMapping("/upload/plan3/{kwCapacity}")
    public ResponseEntity<?> uploadUsageFilePlan3(
            @PathVariable(value = "kwCapacity", required = true) Double kwCapacity) {

        double kwPrice = 10.0;
        double price = 12 * kwCapacity * kwPrice;
        return ResponseEntity.ok(price);
    }

    private ResponseEntity<?> getPriceCalculations(List<GenUseDataHourly> combinedData, double electricityPrice, double storageMonetaryFee, double storagePercentageCutFee) {
        List<ConsumptionModel> chartData = new ArrayList<>();
        double hourlyPrice = electricityPrice;
        double rollingCost = 0.0;
        double boughtFromGridTotal = 0.0;
        double totalConsuption = 0.0;
        double totalGeneration = 0.0;
        double totalGenerationStorage = 0.0;
        double totalGenerationStorageReturned = 0.0;
        double storingBalance = 0.0;

        for(var row : combinedData){
            double consumption = row.getConsumption();
            double generation = row.getGeneration();

            totalConsuption += consumption;
            totalGeneration += generation;
            totalGenerationStorage += generation * (1 - storagePercentageCutFee);
            if (storingBalance <= consumption){
                totalGenerationStorageReturned += storingBalance;
                boughtFromGridTotal  += consumption - storingBalance;
                rollingCost += (consumption - storingBalance) * hourlyPrice;
                storingBalance = 0;
            }else{
                storingBalance -= consumption;
                totalGenerationStorageReturned += consumption;
            }
            storingBalance += generation * (1 - storagePercentageCutFee);

            chartData.add(ConsumptionModel.builder()
                    .dateTime(row.getDateTime())
                    .totalConsumption(round(totalConsuption))
                    .totalStoring(round(totalGenerationStorage))
                    .totalStoringReturned(round(totalGenerationStorageReturned))
                    .totalBoughtFromGrid(round(boughtFromGridTotal))
                    .rollingCost(round(rollingCost))
                    .build());

        }

        double totalPrice = boughtFromGridTotal * electricityPrice + totalGenerationStorageReturned * storageMonetaryFee;
        var result = ResponseDAO.builder()
                .totalConsumption(round(totalConsuption))
                .totalEnergyStored(round(totalGenerationStorageReturned))
                .totalEnergyBoughtFromGrid(round(boughtFromGridTotal))
                .planPrice(round(totalPrice))
                .chartData(chartData)
                .build();

        return ResponseEntity.ok(result);
    }
    private double round(double positive){
        return Math.floor(positive * 100) / 100;
    }
    private List<GenUseDataHourly> mapToGenUseData(MultipartFile csvFile){
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(csvFile.getInputStream()));
            CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setDelimiter(';')
                    .setQuote('"')
                    .build());
            List<GenUseDataHourly> data = (List<GenUseDataHourly>) parser.stream()
                    .filter(record -> record.get("Energijos tipas").equals("P-") || record.get("Energijos tipas").equals("P+"))
                    .map(record -> GenUseDataHourly.builder()
                            .dateTime(OffsetDateTime.parse(record.get("Data, valanda")))
                            .generation(record.get("Energijos tipas").equals("P-") ? Double.parseDouble(record.get("Kiekis, kWh")) : 0.0)
                            .consumption(record.get("Energijos tipas").equals("P+") ? Double.parseDouble(record.get("Kiekis, kWh")) : 0.0)
                            .build()
                    )
                    .collect(Collectors.groupingBy(
                            GenUseDataHourly::getDateTime,
                            Collectors.collectingAndThen(
                                    Collectors.toList(),
                                    list -> GenUseDataHourly.builder()
                                            .dateTime(list.getFirst().getDateTime())
                                            .generation(list.stream().mapToDouble(GenUseDataHourly::getGeneration).sum())
                                            .consumption(list.stream().mapToDouble(GenUseDataHourly::getConsumption).sum())
                                            .build()
                            )
                    ))
                    .values()
                    .stream()
                    .sorted(Comparator.comparing(GenUseDataHourly::getDateTime))
                    .toList();

            return data;
        }
        catch (Exception e){
            return null;
        }
    }
    private List<GenUseDataHourly> mergeUsageConsuptionData(List<GenUseDataHourly>originalList, MultipartFile csvFile){
        if (csvFile != null) {
            List<GenUseDataHourly> additionalList = mapToGenUseData(csvFile);
            if(additionalList != null){
                return Stream.concat(originalList.stream(), additionalList.stream())
                        .collect(Collectors.groupingBy(
                                GenUseDataHourly::getDateTime,
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> GenUseDataHourly.builder()
                                                .dateTime(list.getFirst().getDateTime())
                                                .generation(list.stream().mapToDouble(GenUseDataHourly::getGeneration).sum())
                                                .consumption(list.stream().mapToDouble(GenUseDataHourly::getConsumption).sum())
                                                .build()
                                )
                        ))
                        .values()
                        .stream()
                        .sorted(Comparator.comparing(GenUseDataHourly::getDateTime))
                        .collect(Collectors.toList());
            }
        }
        return originalList;
    }
}
