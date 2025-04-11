package com.pvp.smartwatt.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ResponseDAO {
    private Double totalConsumption;
    private Double totalEnergyStored;
    private Double totalEnergyBoughtFromGrid;
    private Double planPrice;}
