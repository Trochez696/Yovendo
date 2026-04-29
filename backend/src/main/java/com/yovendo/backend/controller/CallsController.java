package com.yovendo.backend.controller;

import com.yovendo.backend.dto.CallDTO;
import com.yovendo.backend.entity.User;
import com.yovendo.backend.repository.UserRepository;
import com.yovendo.backend.service.CallService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calls")
@RequiredArgsConstructor
public class CallsController {

    private final CallService callService;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'CONSULTOR')")
    public ResponseEntity<List<CallDTO>> listCalls(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(callService.getCallsForUser(user));
    }

    @PostMapping
    @PreAuthorize("hasRole('CONSULTOR')")
    public ResponseEntity<CallDTO> createCall(@RequestBody CallDTO request, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(callService.createCall(request, userDetails.getUsername()));
    }
}
