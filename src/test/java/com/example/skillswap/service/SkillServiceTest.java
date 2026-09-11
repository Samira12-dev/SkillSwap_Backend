package com.example.skillswap.service;

import com.example.skillswap.dto.request.SkillDetailsRequestDto;
import com.example.skillswap.dto.request.SkillRequestDto;
import com.example.skillswap.dto.request.SwapRequestRequestDto;
import com.example.skillswap.dto.response.SkillDetailsResponseDto;
import com.example.skillswap.dto.response.SkillResponseDto;
import com.example.skillswap.entity.Skill;
import com.example.skillswap.entity.SkillDetails;
import com.example.skillswap.entity.SwapRequest;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.Role;
import com.example.skillswap.mapper.SkillMapper;
import com.example.skillswap.repository.SkillDetailsRepo;
import com.example.skillswap.repository.SkillRepo;
import com.example.skillswap.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SkillServiceTest {
      @Mock
      private SkillRepo skillRepo;

      @Mock
      private SkillMapper skillMapper;
      @Mock
      private SkillDetailsRepo detailsRepo;
      @Mock
      private UserRepo userRepo;
      @InjectMocks
      private  SkillService service;

    @Test
    void createSkill() {
        SkillRequestDto request =new SkillRequestDto();
        request.setName("Java");

        Skill skill = new Skill();
        skill.setId(5L);
        skill.setName("Java");

        SkillResponseDto responseDto= new SkillResponseDto();
        responseDto.setId(5L);
        responseDto.setName("Java");

       when(skillRepo.existsByName("Java")).thenReturn(false);

        when(skillMapper.toEntity(request)).thenReturn(skill);
        when(skillRepo.save(skill)).thenReturn(skill);
        when(skillMapper.toResponse(skill)).thenReturn(responseDto);

        SkillResponseDto result= service.createSkill(request);

        assertEquals("Java",result.getName());
        verify(skillRepo).existsByName("Java");
        verify(skillRepo).save(skill);
    }

    @Test
    void createSkill_alreadyExists(){
        SkillRequestDto requestDto =new SkillRequestDto();
        requestDto.setName("Java");

        when(skillRepo.existsByName("Java")).thenReturn(true);

        assertThrows(RuntimeException.class,()->service.createSkill(requestDto));

    }

    @Test
    void addSkillToUser() {
        SkillDetailsRequestDto detailsRequestDto= new SkillDetailsRequestDto();
        detailsRequestDto.setSkillId(5L);
        User user =new User();
        user.setFirstName("Samira");
        user.setId(1L);

        Skill skill= new Skill();
        skill.setId(5L);
        skill.setName("Java");

        when(detailsRepo.existsByUserIdAndSkillIdAndType(1L,5L,detailsRequestDto.getType())).thenReturn(false);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(skillRepo.findById(5L)).thenReturn(Optional.of(skill));

        User currentUSer= new User();
        currentUSer.setId(1L);
        currentUSer.setFirstName("Samira");
        currentUSer.setRole(Role.USER);

        service.addSkillToUser(1l,detailsRequestDto,currentUSer);
            verify(detailsRepo).save(any(SkillDetails.class));
    }
    
}