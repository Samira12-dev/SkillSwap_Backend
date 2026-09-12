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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SkillDetailsServiceTest {
    @Mock
    private SkillDetailsRepo skillDetailsRepo;
    @Mock
    private SkillDetailsMapper mapper;
    @Mock
    private UserRepo userRepo;
    @Mock
    private SkillRepo skillRepo;

    @InjectMocks
    private SkillDetailsService service;

    @Test
    void create() {
        Skill skill= new Skill();
        skill.setId(5L);
        SkillDetailsRequestDto requestDto= new SkillDetailsRequestDto();
        requestDto.setSkillId(5L);
        User user=new User();
        user.setId(1L);

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(skillRepo.findById(5L)).thenReturn(Optional.of(skill));

        SkillDetails skillDetails =new SkillDetails();
        skillDetails.setId(10L);

        when(skillDetailsRepo.save(any(SkillDetails.class))).thenReturn(skillDetails);
        SkillDetailsResponseDto responseDto =new SkillDetailsResponseDto();
        when(mapper.toResponse(skillDetails)).thenReturn(responseDto);

        SkillDetailsResponseDto result= service.create(requestDto,1L);
        assertEquals(responseDto,result);
    }

    @Test
    void delete() {
        SkillDetails skillDetails= new SkillDetails();
        skillDetails.setId(2L);
        when(skillDetailsRepo.findById(2L)).thenReturn(Optional.of(skillDetails));
        service.delete(2L);
        verify(skillDetailsRepo).delete(skillDetails);
    }
}