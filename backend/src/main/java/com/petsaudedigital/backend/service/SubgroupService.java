package com.petsaudedigital.backend.service;

import com.petsaudedigital.backend.api.dto.SubgroupDtos;
import com.petsaudedigital.backend.api.mapper.SubgroupMapper;
import com.petsaudedigital.backend.domain.Gt;
import com.petsaudedigital.backend.domain.Project;
import com.petsaudedigital.backend.domain.Subgroup;
import com.petsaudedigital.backend.repository.GtRepository;
import com.petsaudedigital.backend.repository.SubgroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubgroupService {
    private final SubgroupRepository subgroupRepository;
    private final GtRepository gtRepository;
    private final SubgroupMapper subgroupMapper;

    public SubgroupDtos.View create(Long gtId, SubgroupDtos.Create dto) {
        Gt gt = gtRepository.findById(gtId).orElseThrow();
        Project p = gt.getProject();
        Subgroup s = subgroupMapper.toEntity(dto);
        s.setGt(gt);
        s.setProject(p);
        return subgroupMapper.toView(subgroupRepository.save(s));
    }

    public List<SubgroupDtos.View> list(Long gtId) {
        Gt gt = gtRepository.findById(gtId).orElseThrow();
        return subgroupRepository.findByGt(gt).stream().map(subgroupMapper::toView).toList();
    }
}

