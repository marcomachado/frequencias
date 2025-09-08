package com.petsaudedigital.backend.api.dto;

import java.util.List;

public class ExportDtos {
    public record Request(String scopeType, Long scopeId, String from, String to, List<Long> gtIds, String schemaVersion) {}
}

