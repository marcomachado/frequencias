package com.petsaudedigital.backend.api;

import com.petsaudedigital.backend.api.dto.MemberDtos;
import com.petsaudedigital.backend.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gts/{gtId}/members")
@RequiredArgsConstructor
public class MembersController {
    private final MemberService memberService;

    @PostMapping
    @PreAuthorize("@scope.has(authentication, 'exportar_dados', 'GT', #gtId)")
    public ResponseEntity<Void> add(@PathVariable Long gtId, @RequestBody @Valid MemberDtos.Add req) {
        memberService.addMember(gtId, req);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("@scope.has(authentication, 'exportar_dados', 'GT', #gtId)")
    public ResponseEntity<Void> patch(@PathVariable Long gtId, @PathVariable Long userId, @RequestBody @Valid MemberDtos.Patch req) {
        memberService.patchMember(gtId, userId, req);
        return ResponseEntity.noContent().build();
    }
}

