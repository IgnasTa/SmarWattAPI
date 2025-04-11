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
    private Double consumption = 0d;
    private Double totalConsumption = 0d;
    private Double storingAmount = 0d;
    private Double storingBalance = 0d;
    private Double boughtFromGrid = 0d;
    private Double boughtFromGridBalance = 0d;
    private Double totalStoring = 0d;

    @Override
    public String toString() {
        return "ConsumptionModel{" +
                "dateTime=" + dateTime +
                ", consumption=" + consumption +
                ", totalConsumption=" + totalConsumption +
                ", storingAmount=" + storingAmount +
                ", storingBalance=" + storingBalance +
                ", totalstoring=" + totalStoring +
                ", boughtFromGrid=" + boughtFromGrid +
                ", boughtFromGridBalance=" + boughtFromGridBalance +
                '}';
    }
}
