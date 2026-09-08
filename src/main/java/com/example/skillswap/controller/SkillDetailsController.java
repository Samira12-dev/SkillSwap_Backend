package com.example.skillswap.controller;

import com.example.skillswap.dto.request.SkillDetailsRequestDto;
import com.example.skillswap.dto.response.SkillDetailsResponseDto;
import com.example.skillswap.service.SkillDetailsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skilldetails")
@RequiredArgsConstructor
public class SkillDetailsController {
    private final SkillDetailsService service;

    @PostMapping("/{userId}")
    public SkillDetailsResponseDto create( @Valid @RequestBody SkillDetailsRequestDto dto,@PathVariable Long userId){
        return  service.create(dto,userId);
    }

    @GetMapping
    public ResponseEntity<List<SkillDetailsResponseDto>> getAllSkills(){
        return ResponseEntity.ok(service.getAllSkills()) ;
    }
    @GetMapping("/my/{userId}")
    public ResponseEntity<List<SkillDetailsResponseDto>>getAllMySkills(@PathVariable Long userId){
        return ResponseEntity.ok(service.getAllMySkills(userId));
    }
    @GetMapping("/{id}")
    public SkillDetailsResponseDto getById(@PathVariable Long id){
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public SkillDetailsResponseDto update(@PathVariable Long id,@Valid @RequestBody SkillDetailsRequestDto dto){
        return service.update(id,dto);
    }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable  Long id){
        service.delete(id);
    }
}
