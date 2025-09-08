package com.petsaudedigital.backend.api.mapper;

import com.petsaudedigital.backend.api.dto.AxisDtos;
import com.petsaudedigital.backend.domain.Axis;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AxisMapper {
    Axis toEntity(AxisDtos.Create dto);

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "coordEixoUserId", source = "coordEixoUser.id")
    AxisDtos.View toView(Axis entity);
}

