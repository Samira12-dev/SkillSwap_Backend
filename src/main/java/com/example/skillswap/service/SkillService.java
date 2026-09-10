package com.example.skillswap.service;

import com.example.skillswap.dto.request.SkillDetailsRequestDto;
import com.example.skillswap.dto.request.SkillRequestDto;
import com.example.skillswap.dto.response.SkillDetailsResponseDto;
import com.example.skillswap.dto.response.SkillResponseDto;
import com.example.skillswap.entity.Skill;
import com.example.skillswap.entity.SkillDetails;
import com.example.skillswap.entity.User;
import com.example.skillswap.mapper.SkillDetailsMapper;
import com.example.skillswap.mapper.SkillMapper;
import com.example.skillswap.repository.SkillDetailsRepo;
import com.example.skillswap.repository.SkillRepo;
import com.example.skillswap.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillService {
    private final SkillRepo repo;
    private  final SkillMapper mapper;
    private final UserRepo userRepo;
    private final SkillDetailsRepo detailsRepo;
    private final SkillDetailsMapper detailsMapper;

    @Transactional
    public SkillResponseDto createSkill(SkillRequestDto requestDto){
    if(repo.existsByName(requestDto.getName())){
    throw  new RuntimeException("Skill already exists");
   }
        Skill skill= mapper.toEntity(requestDto);
       Skill savedSkill=repo.save(skill);
       return mapper.toResponse(savedSkill);
    }

    @Transactional
    public SkillResponseDto updateSkill(Long id,SkillRequestDto requestDto){
        Skill skill = repo.findById(id).orElseThrow(()->
                new RuntimeException("Skill  not found"));

        mapper.updateSkill(requestDto,skill);
        return mapper.toResponse(skill);
    }

    @Transactional
    public List<SkillResponseDto> findAll(){
        return repo.findAll().stream()
                .map(mapper::toResponse).
                toList();
    }

    @Transactional
    public SkillResponseDto findSkillById(Long id){
        Skill skill=repo.findById(id).orElseThrow(()->new RuntimeException("Skill not found"));
        return mapper.toResponse(skill);
    }

    @Transactional
    public void deleteSkill(Long id){
        repo.deleteById(id);
    }

    @Transactional
    public void  addSkillToUser(Long userId, SkillDetailsRequestDto requestDto){
        User user = userRepo.findById(userId).orElseThrow(()->new RuntimeException("User not found"));
        Skill skill =repo.findById(requestDto.getSkillId()).orElseThrow(()->new RuntimeException("Skill not found"));
        SkillDetails skillDetails =new SkillDetails();
        skillDetails.setUser(user);
        skillDetails.setSkill(skill);
        skillDetails.setType(requestDto.getType());
        skillDetails.setLevel(requestDto.getLevel());
        detailsRepo.save(skillDetails);
    }

    @Transactional
    public void removeSkillFromUser(Long userId,Long skillId){
        SkillDetails skillDetails= detailsRepo.findByUserIdAndSkillId(userId,skillId).orElseThrow(()->new RuntimeException("skill not found for this user"));
        detailsRepo.delete(skillDetails);
    }

    @Transactional
    public List<SkillDetailsResponseDto> getUserSkills(Long userId){
       List<SkillDetails> listSkills=detailsRepo.findByUserId(userId);
       return listSkills.stream().map(detailsMapper::toResponse).toList();
    }

}
