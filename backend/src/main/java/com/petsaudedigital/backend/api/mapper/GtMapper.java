package com.petsaudedigital.backend.api.mapper;

import com.petsaudedigital.backend.api.dto.GtDtos;
import com.petsaudedigital.backend.domain.Gt;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GtMapper {
    Gt toEntity(GtDtos.Create dto);

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "axisId", source = "axis.id")
    @Mapping(target = "coordGtUserId", source = "coordGtUser.id")
    GtDtos.View toView(Gt entity);
}

