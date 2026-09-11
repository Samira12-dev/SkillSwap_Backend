package com.example.skillswap.controller;

import com.example.skillswap.dto.request.SkillDetailsRequestDto;
import com.example.skillswap.dto.request.SkillRequestDto;
import com.example.skillswap.dto.response.SkillDetailsResponseDto;
import com.example.skillswap.dto.response.SkillResponseDto;
import com.example.skillswap.service.SkillService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List< SkillResponseDto>>getAll(){
        return ResponseEntity.ok(service.findAll()) ;
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

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/users/{userId}/skill")
    public ResponseEntity<SkillDetailsResponseDto>addSkillToUser(@PathVariable Long userId, @RequestBody SkillDetailsRequestDto requestDto){
        service.addSkillToUser(userId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/users/{userId}/skills/{skillId}")
    public void removeSkillFromUserremoveSkillFromUser(@PathVariable Long userId,@PathVariable Long skillId){
        service.removeSkillFromUser(userId,skillId);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/users/{userId}/skills")
    public ResponseEntity<List<SkillDetailsResponseDto>>getUserSkills(@PathVariable Long userId){
        return ResponseEntity.ok(service.getUserSkills(userId));
    }

}
