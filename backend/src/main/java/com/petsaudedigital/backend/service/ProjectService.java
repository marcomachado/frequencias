package com.petsaudedigital.backend.service;

import com.petsaudedigital.backend.api.dto.ProjectDtos;
import com.petsaudedigital.backend.api.mapper.ProjectMapper;
import com.petsaudedigital.backend.domain.Project;
import com.petsaudedigital.backend.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    public ProjectDtos.View create(ProjectDtos.Create dto) {
        Project entity = projectMapper.toEntity(dto);
        return projectMapper.toView(projectRepository.save(entity));
    }

    public List<ProjectDtos.View> list() {
        return projectRepository.findAll().stream().map(projectMapper::toView).toList();
    }
}

