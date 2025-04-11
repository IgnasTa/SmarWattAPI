package com.pvp.smartwatt.controller;

import com.pvp.smartwatt.model.ConsumptionModel;
import com.pvp.smartwatt.model.ResponseDAO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1")

public class BusinessController {

    @PostMapping("/upload/plan1")
    public ResponseEntity<?> uploadUsageFilePlan1(@RequestParam("file") MultipartFile csvFile) {
        if (csvFile.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty. Please upload a valid CSV file.");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvFile.getInputStream()))) {
            String line;
            List<ConsumptionModel> data = new ArrayList<>();


            String linezero = reader.readLine();
            Double boughtFromGridTotal = 0d;
            Double totalConsuption = 0d;
            Double storingBalance = 0d;
            Double totalStoring = 0d;
            boolean invertConsuption = true;
            while ((line = reader.readLine()) != null) {

                String[] values = line.split(";");
                OffsetDateTime dateTime = OffsetDateTime.parse(values[10].replace("\"", "").trim());
                Double consumption = invertConsuption ? Double.parseDouble(values[13].replace("\"", "").trim()) : -Double.parseDouble(values[13].replace("\"", "").trim());
                invertConsuption = !invertConsuption;
                totalConsuption += consumption > 0 ? consumption : 0;
                Double storingAmount = 0d;
                Double boughtFromGrid = 0d;

                if (consumption < 0){
                    storingAmount = -consumption;
                    storingBalance += storingAmount * 0.65;
                }
                if(consumption > 0){
                    if(consumption < storingBalance){
                        storingBalance -= consumption;
                    }
                    else if(storingBalance > 0){
                        boughtFromGrid = consumption - storingBalance;
                        storingBalance = 0d;
                        boughtFromGridTotal += boughtFromGrid;
                    }
                    else{
                        boughtFromGrid = consumption;
                        boughtFromGridTotal += consumption;
                    }
                }
                totalStoring +=storingAmount * 0.65;


                ConsumptionModel entry = ConsumptionModel.builder()
                        .dateTime(dateTime)
                        .consumption(consumption)
                        .totalConsumption(totalConsuption)
                        .storingAmount(storingAmount)
                        .storingBalance(storingBalance)
                        .boughtFromGrid(boughtFromGrid)
                        .boughtFromGridBalance(boughtFromGridTotal)
                        .totalStoring(totalStoring)
                        .build();
                data.add(entry);
            }

            data.forEach(row -> System.out.println(row.toString()));
            ConsumptionModel totals = data.getLast();
            ResponseDAO responseDAO = ResponseDAO.builder()
                    .totalConsumption(Math.round(totals.getTotalConsumption() *100.0)/100.0)
                    .totalEnergyBoughtFromGrid(Math.round(totals.getBoughtFromGridBalance() *100.0)/100.0)
                    .totalEnergyStored(Math.round(totals.getTotalStoring() *100.0)/100.0)
                    .planPrice(Math.round(totals.getBoughtFromGridBalance() * 0.221 *100.0)/100.0)
                    .build();
            return ResponseEntity.ok(responseDAO);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error processing file: " + e.getMessage());
        }
    }

    @PostMapping("/upload/plan2")
    public ResponseEntity<?> uploadUsageFilePlan2(@RequestParam("file") MultipartFile csvFile) {
        if (csvFile.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty. Please upload a valid CSV file.");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvFile.getInputStream()))) {
            String line;
            List<ConsumptionModel> data = new ArrayList<>();


            String linezero = reader.readLine();
            Double boughtFromGridTotal = 0d;
            Double totalConsuption = 0d;
            Double storingBalance = 0d;
            Double totalStoring = 0d;
            boolean invertConsuption = true;
            while ((line = reader.readLine()) != null) {

                String[] values = line.split(";");
                OffsetDateTime dateTime = OffsetDateTime.parse(values[10].replace("\"", "").trim());
                Double consumption = invertConsuption ? Double.parseDouble(values[13].replace("\"", "").trim()) : -Double.parseDouble(values[13].replace("\"", "").trim());
                invertConsuption = !invertConsuption;
                totalConsuption += consumption > 0 ? consumption : 0;
                Double storingAmount = 0d;
                Double boughtFromGrid = 0d;

                if (consumption < 0){
                    storingAmount = -consumption;
                    storingBalance += storingAmount ;
                }
                if(consumption > 0){
                    if(consumption < storingBalance){
                        storingBalance -= consumption;
                    }
                    else if(storingBalance > 0){
                        boughtFromGrid = consumption - storingBalance;
                        storingBalance = 0d;
                        boughtFromGridTotal += boughtFromGrid;
                    }
                    else{
                        boughtFromGrid = consumption;
                        boughtFromGridTotal += consumption;
                    }
                }
                totalStoring +=storingAmount;


                ConsumptionModel entry = ConsumptionModel.builder()
                        .dateTime(dateTime)
                        .consumption(consumption)
                        .totalConsumption(totalConsuption)
                        .storingAmount(storingAmount)
                        .storingBalance(storingBalance)
                        .boughtFromGrid(boughtFromGrid)
                        .boughtFromGridBalance(boughtFromGridTotal)
                        .totalStoring(totalStoring)
                        .build();
                data.add(entry);
            }

            data.forEach(row -> System.out.println(row.toString()));
            ConsumptionModel totals = data.getLast();

            ResponseDAO responseDAO = ResponseDAO.builder()
                    .totalConsumption(Math.round(totals.getTotalConsumption() *100.0)/100.0)
                    .totalEnergyBoughtFromGrid(Math.round(totals.getBoughtFromGridBalance() *100.0)/100.0)
                    .totalEnergyStored(Math.round(totals.getTotalStoring() *100.0)/100.0)
                    .planPrice(Math.round((totals.getTotalStoring()*0.06655 + totals.getBoughtFromGridBalance() * 0.221) *100.0)/100.0)
                    .build();




//            ResponseDAO responseDAO = ResponseDAO.builder()
//                    .totalConsumption(totals.getTotalConsumption())
//                    .totalEnergyBoughtFromGrid(totals.getBoughtFromGridBalance())
//                    .totalEnergyStored(totals.getTotalStoring())
//                    .planPrice(totals.getTotalStoring()*0.06655 + totals.getBoughtFromGridBalance() * 0.221)
//                    .build();
            return ResponseEntity.ok(responseDAO);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error processing file: " + e.getMessage());
        }
    }
}
