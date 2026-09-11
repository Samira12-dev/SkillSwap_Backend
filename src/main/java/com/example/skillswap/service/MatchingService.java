package com.example.skillswap.service;

import com.example.skillswap.dto.response.MatchingResponseDto;
import com.example.skillswap.entity.SkillDetails;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.SkillType;
import com.example.skillswap.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchingService {
    private  final UserRepo userRepo;


    public int calculateScore(User user1, User user2){
        int score = 0;
        for(SkillDetails skill1:user1.getSkillDetails()){
            for (SkillDetails skill2 :user2.getSkillDetails()){
                if(skill1.getType() == SkillType.OFFER && skill2.getType()
                        ==SkillType.WANTED && skill1.getSkill().getId().equals(skill2.getSkill().getId())
                ){
                    score +=50;
                }
                if(skill1.getType()==SkillType.WANTED &&skill2.getType() == SkillType.OFFER &&
                skill1.getSkill().getId().equals(skill2.getSkill().getId())){
                    score += 50;
                }
            }
        }
        return score;
    }

    @Transactional
    public Page<MatchingResponseDto> findMatches(Long userId, Pageable pageable){
        User currentUser= userRepo.findById(userId).orElseThrow(()->new RuntimeException("User not found"));
        List<User> users =userRepo.findAll();
        List<MatchingResponseDto> matches= new ArrayList<>();
        for(User user:users){
            if(user.getId().equals(userId)){
                continue;
            }
            int score = calculateScore(currentUser,user);
            if(score >0){
                MatchingResponseDto dto= new MatchingResponseDto();
                dto.setUserId(user.getId());
                dto.setFirstName(user.getFirstName());
                dto.setLastName(user.getLastName());
                dto.setScore(score);
                matches.add(dto);
            }
        }
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), matches.size());
        if (start >= matches.size()) {
            return new PageImpl<>(List.of(), pageable, matches.size());
        }
        return new PageImpl<>(matches.subList(start, end), pageable, matches.size());
    }

}