package com.petsaudedigital.backend.service;

import com.petsaudedigital.backend.api.dto.GtDtos;
import com.petsaudedigital.backend.api.mapper.GtMapper;
import com.petsaudedigital.backend.domain.Axis;
import com.petsaudedigital.backend.domain.Gt;
import com.petsaudedigital.backend.domain.Project;
import com.petsaudedigital.backend.domain.User;
import com.petsaudedigital.backend.repository.AxisRepository;
import com.petsaudedigital.backend.repository.GtRepository;
import com.petsaudedigital.backend.repository.ProjectRepository;
import com.petsaudedigital.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GtService {
    private final GtRepository gtRepository;
    private final ProjectRepository projectRepository;
    private final AxisRepository axisRepository;
    private final UserRepository userRepository;
    private final GtMapper gtMapper;

    public GtDtos.View create(Long axisId, GtDtos.Create dto) {
        Axis axis = axisRepository.findById(axisId).orElseThrow();
        Project project = axis.getProject();
        Gt gt = gtMapper.toEntity(dto);
        gt.setAxis(axis);
        gt.setProject(project);
        if (dto.coordGtUserId() != null) {
            User u = userRepository.findById(dto.coordGtUserId()).orElseThrow();
            gt.setCoordGtUser(u);
        }
        return gtMapper.toView(gtRepository.save(gt));
    }

    public List<GtDtos.View> listByAxis(Long axisId) {
        Axis axis = axisRepository.findById(axisId).orElseThrow();
        return gtRepository.findByAxis(axis).stream().map(gtMapper::toView).toList();
    }
}

