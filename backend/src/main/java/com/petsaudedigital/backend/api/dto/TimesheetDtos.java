package com.petsaudedigital.backend.api.dto;

import java.util.List;

public class TimesheetDtos {
    public record Item(Long activityId, String data, String inicio, String fim, long minutos) {}
    public record Summary(long totalMinutos, double totalHoras, boolean horasInsuficientes, List<Item> itens) {}
}

