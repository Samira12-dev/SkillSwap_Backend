package com.example.skillswap.service;

import com.example.skillswap.dto.request.SkillDetailsRequestDto;
import com.example.skillswap.dto.response.SkillDetailsResponseDto;
import com.example.skillswap.entity.Skill;
import com.example.skillswap.entity.SkillDetails;
import com.example.skillswap.entity.User;
import com.example.skillswap.mapper.SkillDetailsMapper;
import com.example.skillswap.repository.SkillDetailsRepo;
import com.example.skillswap.repository.SkillRepo;
import com.example.skillswap.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SkillDetailsService {
    private final SkillDetailsRepo skillDetailsRepo;
    private final UserRepo userRepo;
     private final SkillRepo skillRepo;
     private final SkillDetailsMapper mapper;
    @Transactional
    public SkillDetailsResponseDto create(SkillDetailsRequestDto requestDto,Long userId){
        User user = userRepo.findById(userId).orElseThrow(()->new RuntimeException("User not found"));
        Skill skill =skillRepo.findById(requestDto.getSkillId()).orElseThrow(()->new RuntimeException("Skill not found"));
        SkillDetails skillDetails =new SkillDetails();
        skillDetails.setType(requestDto.getType());
        skillDetails.setLevel(requestDto.getLevel());
        skillDetails.setUser(user);
        skillDetails.setSkill(skill);

        SkillDetails saved= skillDetailsRepo.save(skillDetails);
       return mapper.toResponse(saved);
    }

    @Transactional
    public Page<SkillDetailsResponseDto> getAllSkills(Pageable pageable){
        return skillDetailsRepo.findAll(pageable).map(mapper::toResponse);
    }

    public Page<SkillDetailsResponseDto> getAllMySkills(Long userId, Pageable pageable) {
        return skillDetailsRepo.findByUserId(userId, pageable).map(mapper::toResponse);
    }

    @Transactional
    public SkillDetailsResponseDto getById(Long id){
        SkillDetails skillDetails= skillDetailsRepo.findById(id).orElseThrow(()->new RuntimeException("Skill detail not found"));
        return mapper.toResponse(skillDetails);
    }

    @Transactional
    public SkillDetailsResponseDto update(Long id,SkillDetailsRequestDto requestDto){
        SkillDetails skillDetails = skillDetailsRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Skill details not found"));

        Skill skill = skillRepo.findById(requestDto.getSkillId())
                .orElseThrow(() ->
                        new RuntimeException("Skill not found"));
        skillDetails.setType(requestDto.getType());
        skillDetails.setLevel(requestDto.getLevel());
        skillDetails.setSkill(skill);

        SkillDetails updated = skillDetailsRepo.save(skillDetails);
        return mapper.toResponse(updated);
    }

    @Transactional
    public void  delete(Long id){
        SkillDetails skillDetails = skillDetailsRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Skill details not found"));
        skillDetailsRepo.delete(skillDetails);
    }
}