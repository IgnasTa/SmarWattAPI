package com.pvp.smartwatt.model;

import lombok.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ConsumptionModel {
    private OffsetDateTime dateTime;
    private Double totalConsumption = 0d;
    private Double totalStoring = 0d;
    private Double totalStoringReturned = 0d;
    private Double totalBoughtFromGrid = 0d;
    private Double rollingCost = 0.0;
}
