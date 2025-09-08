package com.petsaudedigital.backend.api.mapper;

import com.petsaudedigital.backend.api.dto.ProjectDtos;
import com.petsaudedigital.backend.domain.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    Project toEntity(ProjectDtos.Create dto);

    @Mapping(target = "createdAt", source = "createdAt")
    ProjectDtos.View toView(Project entity);
}

