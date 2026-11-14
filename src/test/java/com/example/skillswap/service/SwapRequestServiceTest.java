package com.example.skillswap.service;

import com.example.skillswap.dto.request.SwapRequestRequestDto;
import com.example.skillswap.dto.response.ConversationResponseDto;
import com.example.skillswap.dto.response.SwapRequestResponseDto;
import com.example.skillswap.entity.Skill;
import com.example.skillswap.entity.SwapRequest;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.SwapStatus;
import com.example.skillswap.mapper.SwapRequestMapper;
import com.example.skillswap.repository.SkillDetailsRepo;
import com.example.skillswap.repository.SkillRepo;
import com.example.skillswap.repository.SwapRequestRepo;
import com.example.skillswap.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SwapRequestServiceTest {

    @Mock
    private SwapRequestRepo swapRequestRepo;
    @Mock
    private SkillRepo skillRepo;
    @Mock
    private SwapRequestMapper mapper;
    @Mock
    private UserRepo userRepo;
    @Mock
    private SkillDetailsRepo detailsRepo;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ConversationService conversationService;
    @InjectMocks
    private  SwapRequestService service;


    @Test
    void createSwapRequest() {
        User sender= new User();
        sender.setId(1L);
        User receiver =new User();
        receiver.setId(2L);

        Skill offer=new Skill();
        offer.setId(10L);
        Skill wanted= new Skill();
        wanted.setId(20L);


        SwapRequestRequestDto requestDto= new SwapRequestRequestDto();
        requestDto.setReceiverId(2L);
        requestDto.setSkillOfferedId(10L);
        requestDto.setSkillWantedId(20L);
        requestDto.setMessage("let's message");

        SwapRequest saved =new SwapRequest();
        SwapRequestResponseDto responseDto =new SwapRequestResponseDto();

        when(userRepo.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepo.findById(2L)).thenReturn(Optional.of(receiver));
        when(skillRepo.findById(10L)).thenReturn(Optional.of(offer));
        when(skillRepo.findById(20L)).thenReturn(Optional.of(wanted));
        when(detailsRepo.existsByUserIdAndSkillId(1L,10L)).thenReturn(true);
        when(detailsRepo.existsByUserIdAndSkillId(2L,20L)).thenReturn(true);

        when(swapRequestRepo.save(any(SwapRequest.class))).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(responseDto);

        SwapRequestResponseDto result= service.createSwapRequest(1L,requestDto);

        assertEquals(responseDto,result);


    }

    @Test
    void acceptSwapRequest() {

        User sender =new User();
        sender.setId(1L);

        User receiver= new User();
        receiver.setId(2L);

        SwapRequest swapRequest1 =new SwapRequest();
        swapRequest1.setId(100L);
        swapRequest1.setSender(sender);
        swapRequest1.setReceiver(receiver);
        swapRequest1.setSwapStatus(SwapStatus.PENDING);

        SwapRequestResponseDto responseDto = new SwapRequestResponseDto();

        ConversationResponseDto conversationResponseDto = new ConversationResponseDto();
        conversationResponseDto.setId(500L);

        when(swapRequestRepo.findById(100L)).thenReturn(Optional.of(swapRequest1));

        when(swapRequestRepo.save(swapRequest1)).thenReturn(swapRequest1);
        when(conversationService.createCoersation(100L, 2L)).thenReturn(conversationResponseDto);
        when(mapper.toResponse(swapRequest1)).thenReturn(responseDto);

        SwapRequestResponseDto result= service.acceptSwapRequest(100L,2L);

        assertEquals(SwapStatus.ACCEPTED,swapRequest1.getSwapStatus());
        assertEquals(responseDto,result);

    }

    @Test
    void senderCannotOfferSkillThatIsNotOwnedByThem() {
        User sender = new User();
        sender.setId(1L);
        User receiver = new User();
        receiver.setId(2L);

        SwapRequestRequestDto requestDto = new SwapRequestRequestDto();
        requestDto.setReceiverId(2L);
        requestDto.setSkillOfferedId(10L);
        requestDto.setSkillWantedId(20L);
        requestDto.setMessage("let's message");

        when(userRepo.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepo.findById(2L)).thenReturn(Optional.of(receiver));
        when(skillRepo.findById(10L)).thenReturn(Optional.of(new Skill()));
        when(skillRepo.findById(20L)).thenReturn(Optional.of(new Skill()));
        when(detailsRepo.existsByUserIdAndSkillId(1L, 10L)).thenReturn(false);

        assertThrows(
                RuntimeException.class,
                () -> service.createSwapRequest(1L, requestDto)
        );
    }

    @Test
    void swapCompletionRequiresTheOtherParticipantToConfirm() {
        User sender = new User();
        sender.setId(1L);
        sender.setFirstName("Sender");
        User receiver = new User();
        receiver.setId(2L);
        receiver.setFirstName("Receiver");

        SwapRequest swapRequest = new SwapRequest();
        swapRequest.setId(100L);
        swapRequest.setSender(sender);
        swapRequest.setReceiver(receiver);
        swapRequest.setSwapStatus(SwapStatus.ACCEPTED);

        when(swapRequestRepo.findById(100L)).thenReturn(Optional.of(swapRequest));
        when(swapRequestRepo.save(any(SwapRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toResponse(any(SwapRequest.class)))
                .thenReturn(new SwapRequestResponseDto());

        service.completeSwapRequest(100L, 1L);

        assertEquals(SwapStatus.COMPLETION_REQUESTED, swapRequest.getSwapStatus());
        assertEquals(sender, swapRequest.getCompletionRequestedBy());
        assertNotNull(swapRequest.getCompletionRequestedAt());

        assertThrows(
                RuntimeException.class,
                () -> service.completeSwapRequest(100L, 1L)
        );

        service.completeSwapRequest(100L, 2L);

        assertEquals(SwapStatus.COMPLETED, swapRequest.getSwapStatus());
    }
}