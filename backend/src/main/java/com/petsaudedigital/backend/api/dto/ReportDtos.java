package com.petsaudedigital.backend.api.dto;

import java.util.List;

public class ReportDtos {
    public record HoursByUserItem(Long userId, String email, double horas) {}
    public record HoursByUser(List<HoursByUserItem> itens) {}
    public record PendingItem(Long attendanceId, Long activityId, String titulo, Long userId, String email, String data, String inicio, String fim) {}
    public record PendingAttendances(List<PendingItem> itens) {}
}
