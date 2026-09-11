package com.example.skillswap.controller;

import com.example.skillswap.dto.request.SkillDetailsRequestDto;
import com.example.skillswap.dto.request.SkillRequestDto;
import com.example.skillswap.dto.response.SkillDetailsResponseDto;
import com.example.skillswap.dto.response.SkillResponseDto;
import com.example.skillswap.entity.User;
import com.example.skillswap.service.SkillService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/skills")
public class skillController {
    private final SkillService service;

    public skillController(SkillService service) {
        this.service = service;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public SkillResponseDto createSkill(@Valid @RequestBody SkillRequestDto dto){
        return service.createSkill(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public SkillResponseDto updateSkill(@PathVariable Long id, @Valid @RequestBody SkillRequestDto dto){
        return service.updateSkill(id, dto);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<Page< SkillResponseDto>>getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.findAll(page,size)) ;
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<SkillResponseDto>getSkillById(@PathVariable Long id){
        return ResponseEntity.ok(service.findSkillById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteSkill(@PathVariable Long id){
        service.deleteSkill(id);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/users/{userId}/skill")
    public ResponseEntity<SkillDetailsResponseDto>addSkillToUser(@PathVariable Long userId, @RequestBody SkillDetailsRequestDto requestDto,@AuthenticationPrincipal User currentUser){
        service.addSkillToUser(userId, requestDto,currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @DeleteMapping("/users/{userId}/skills/{skillId}")
    public void removeSkillFromUser(@PathVariable Long userId,@PathVariable Long skillId,@AuthenticationPrincipal User currentUser){
        service.removeSkillFromUser(userId,skillId,currentUser);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/users/{userId}/skills")
    public ResponseEntity<Page<SkillDetailsResponseDto>>getUserSkills(@PathVariable Long userId, @PageableDefault(page = 0, size = 10) Pageable pageable,@AuthenticationPrincipal User currentUser){
        return ResponseEntity.ok(service.getUserSkills(userId, pageable,currentUser));
    }

}
