package com.petsaudedigital.backend.service;

import com.petsaudedigital.backend.api.dto.MemberDtos;
import com.petsaudedigital.backend.domain.Gt;
import com.petsaudedigital.backend.domain.User;
import com.petsaudedigital.backend.domain.UserGt;
import com.petsaudedigital.backend.repository.GtRepository;
import com.petsaudedigital.backend.repository.UserGtRepository;
import com.petsaudedigital.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final UserGtRepository userGtRepository;
    private final UserRepository userRepository;
    private final GtRepository gtRepository;

    public void addMember(Long gtId, MemberDtos.Add req) {
        Gt gt = gtRepository.findById(gtId).orElseThrow();
        User user = userRepository.findById(req.userId()).orElseThrow();

        if ("tutor".equalsIgnoreCase(req.roleInGt())) {
            // Garante no máximo 1 GT para tutor
            boolean exists = userGtRepository.findAll().stream()
                    .anyMatch(ug -> ug.getUser().getId().equals(user.getId()) && "tutor".equalsIgnoreCase(ug.getRoleInGt()));
            if (exists) throw new IllegalArgumentException("Tutor já vinculado a um GT");
        }

        UserGt ug = new UserGt();
        ug.setId(new UserGt.Id(user.getId(), gt.getId()));
        ug.setUser(user);
        ug.setGt(gt);
        ug.setRoleInGt(req.roleInGt());
        userGtRepository.save(ug);
    }

    public void patchMember(Long gtId, Long userId, MemberDtos.Patch req) {
        UserGt.Id id = new UserGt.Id(userId, gtId);
        UserGt ug = userGtRepository.findById(id).orElseThrow();
        if ("tutor".equalsIgnoreCase(req.roleInGt())) {
            boolean exists = userGtRepository.findAll().stream()
                    .anyMatch(x -> x.getUser().getId().equals(userId) && "tutor".equalsIgnoreCase(x.getRoleInGt()) && !x.getGt().getId().equals(gtId));
            if (exists) throw new IllegalArgumentException("Tutor já vinculado a um GT");
        }
        ug.setRoleInGt(req.roleInGt());
        userGtRepository.save(ug);
    }
}

