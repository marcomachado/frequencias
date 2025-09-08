package com.petsaudedigital.backend.api.mapper;

import com.petsaudedigital.backend.api.dto.SubgroupDtos;
import com.petsaudedigital.backend.domain.Subgroup;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubgroupMapper {
    Subgroup toEntity(SubgroupDtos.Create dto);

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "gtId", source = "gt.id")
    SubgroupDtos.View toView(Subgroup entity);
}

