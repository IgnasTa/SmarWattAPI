package com.pvp.smartwatt.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ResponseDAO {
    private Double totalConsumption;
    private Double totalEnergyStored;
    private Double totalEnergyStoredReturned;
    private Double totalEnergyBoughtFromGrid;
    private Double planPrice;
    private List<ConsumptionModel> chartData;

}
