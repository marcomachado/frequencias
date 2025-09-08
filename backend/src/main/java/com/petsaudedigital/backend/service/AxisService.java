package com.petsaudedigital.backend.service;

import com.petsaudedigital.backend.api.dto.AxisDtos;
import com.petsaudedigital.backend.api.mapper.AxisMapper;
import com.petsaudedigital.backend.domain.Axis;
import com.petsaudedigital.backend.domain.Project;
import com.petsaudedigital.backend.domain.User;
import com.petsaudedigital.backend.repository.AxisRepository;
import com.petsaudedigital.backend.repository.ProjectRepository;
import com.petsaudedigital.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AxisService {
    private final AxisRepository axisRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AxisMapper axisMapper;

    public AxisDtos.View create(Long projectId, AxisDtos.Create dto) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        Axis axis = axisMapper.toEntity(dto);
        axis.setProject(project);
        if (dto.coordEixoUserId() != null) {
            User u = userRepository.findById(dto.coordEixoUserId()).orElseThrow();
            axis.setCoordEixoUser(u);
        }
        return axisMapper.toView(axisRepository.save(axis));
    }

    public List<AxisDtos.View> list(Long projectId) {
        Project p = projectRepository.findById(projectId).orElseThrow();
        return axisRepository.findByProject(p).stream().map(axisMapper::toView).toList();
    }
}

