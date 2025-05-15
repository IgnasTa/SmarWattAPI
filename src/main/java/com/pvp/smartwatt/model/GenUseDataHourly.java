package com.pvp.smartwatt.model;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

@Data
@Getter
@SuperBuilder
public class GenUseDataHourly {
    private OffsetDateTime dateTime;
    private Double consumption = 0d;
    private Double generation = 0d;

}
