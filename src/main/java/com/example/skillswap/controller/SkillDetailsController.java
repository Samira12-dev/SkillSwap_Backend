package com.example.skillswap.controller;

import com.example.skillswap.dto.request.SkillDetailsRequestDto;
import com.example.skillswap.dto.response.SkillDetailsResponseDto;
import com.example.skillswap.service.SkillDetailsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/skilldetails")
@RequiredArgsConstructor
public class SkillDetailsController {
    private final SkillDetailsService service;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{userId}")
    public SkillDetailsResponseDto create( @Valid @RequestBody SkillDetailsRequestDto dto,@PathVariable Long userId){
        return  service.create(dto,userId);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<Page<SkillDetailsResponseDto>> getAllSkills(@PageableDefault(page = 0, size = 10) Pageable pageable){
        return ResponseEntity.ok(service.getAllSkills(pageable)) ;
    }
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my/{userId}")
    public ResponseEntity<Page<SkillDetailsResponseDto>>getAllMySkills(@PathVariable Long userId, @PageableDefault(page = 0, size = 10) Pageable pageable){
        return ResponseEntity.ok(service.getAllMySkills(userId, pageable));
    }
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public SkillDetailsResponseDto getById(@PathVariable Long id){
        return service.getById(id);
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{id}")
    public SkillDetailsResponseDto update(@PathVariable Long id,@Valid @RequestBody SkillDetailsRequestDto dto){
        return service.update(id,dto);
    }
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable  Long id){
        service.delete(id);
    }
}