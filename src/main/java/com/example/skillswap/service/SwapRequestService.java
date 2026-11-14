package com.example.skillswap.service;
import com.example.skillswap.exception.BadRequestException;

import com.example.skillswap.dto.request.SwapRequestRequestDto;
import com.example.skillswap.dto.response.ConversationResponseDto;
import com.example.skillswap.dto.response.SwapRequestResponseDto;
import com.example.skillswap.entity.Skill;
import com.example.skillswap.entity.SwapRequest;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.NotificationType;
import com.example.skillswap.enums.SwapStatus;
import com.example.skillswap.mapper.SwapRequestMapper;
import com.example.skillswap.repository.SkillDetailsRepo;
import com.example.skillswap.repository.SkillRepo;
import com.example.skillswap.repository.SwapRequestRepo;
import com.example.skillswap.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SwapRequestService {
    private final SwapRequestRepo repo;
    private final SwapRequestMapper mapper;
    private final UserRepo userRepo;
    private  final SkillRepo skillRepo;
    private final SkillDetailsRepo detailsRepo;
    private  final NotificationService notificationService;
    private final ConversationService conversationService;
    @Transactional
    public SwapRequestResponseDto createSwapRequest(Long senderId, SwapRequestRequestDto requestDto){
        User sender= userRepo.findById(senderId).orElseThrow(()->new BadRequestException("Sender not found"));
        User receiver = userRepo.findById(requestDto.getReceiverId()).orElseThrow(()->new BadRequestException("Receiver not found"));
        if(senderId.equals(requestDto.getReceiverId())){
            throw new BadRequestException("You can't send a swap request to yourself");
        }
        Skill skillOffered =skillRepo.findById(requestDto.getSkillOfferedId())
                .orElseThrow(() -> new BadRequestException("Offered skill not found"));
        Skill skillWanted = skillRepo.findById(requestDto.getSkillWantedId())
                .orElseThrow(() -> new BadRequestException("Wanted skill not found"));

        if(!detailsRepo.existsByUserIdAndSkillId(senderId,requestDto.getSkillOfferedId())){
            throw new BadRequestException(
                    "You can only offer a skill that belongs to your profile");
        }

        if(!detailsRepo.existsByUserIdAndSkillId(receiver.getId(),requestDto.getSkillWantedId())){
            throw new BadRequestException(
                    "Receiver does not have the wanted skill");
        }
        SwapRequest swapRequest= new SwapRequest();
        swapRequest.setSender(sender);
        swapRequest.setReceiver(receiver);
        swapRequest.setSkillOffered(skillOffered);
        swapRequest.setSkillWanted(skillWanted);
        swapRequest.setMessage(requestDto.getMessage());
        SwapRequest savedSwap =repo.save(swapRequest);
        notificationService.createNotification(
                swapRequest.getReceiver().getId(),
                swapRequest.getSender().getFirstName() + " sent you a new swap request",
                NotificationType.NEW_SWAP_REQUEST
        );
        return mapper.toResponse(savedSwap);
    }

    @Transactional
    public SwapRequestResponseDto getSwapRequestById(Long swapId, Long userId){
        SwapRequest swapRequest =repo.findById(swapId).orElseThrow(()->new BadRequestException("swap request not found"));
        if (!swapRequest.getSender().getId().equals(userId)
                && !swapRequest.getReceiver().getId().equals(userId)) {
            throw new BadRequestException("You are not part of this swap request");
        }
        return mapper.toResponse(swapRequest);
    }

    @Transactional
    public Page<SwapRequestResponseDto> getAllSwapRequests(Pageable pageable){
        return repo.findAll(pageable).map(mapper::toResponse);
    }

    @Transactional
    public Page<SwapRequestResponseDto>getReceivedRequests(Long userId, Pageable pageable){
        return repo.findByReceiverId(userId, pageable).map(mapper::toResponse);
    }

    @Transactional
    public Page<SwapRequestResponseDto>getSentRequests(Long userId, Pageable pageable){
        return repo.findBySenderId(userId, pageable).map(mapper::toResponse);
    }

    @Transactional
    public SwapRequestResponseDto acceptSwapRequest(Long swapId, Long userId) {
        SwapRequest swapRequest = repo.findById(swapId)
                .orElseThrow(() -> new BadRequestException("Swap request not found"));

        if (!swapRequest.getReceiver().getId().equals(userId)) {
            throw new BadRequestException("You are not allowed to accept this request");
        }

        if (swapRequest.getSwapStatus() != SwapStatus.PENDING) {
            throw new BadRequestException("swap is not pending");
        }

        swapRequest.setSwapStatus(SwapStatus.ACCEPTED);
        SwapRequest savedRequest = repo.save(swapRequest);

        ConversationResponseDto conversation = conversationService.createCoersation(savedRequest.getId(), userId);

        notificationService.createNotification(
                swapRequest.getSender().getId(),
                swapRequest.getReceiver().getFirstName() + " accepted your swap request",
                NotificationType.REQUEST_ACCEPTED
        );
        SwapRequestResponseDto response = mapper.toResponse(savedRequest);
        response.setConversationId(conversation.getId());

        return response;
    }
    @Transactional
    public SwapRequestResponseDto rejectSwapRequest(Long swapId,Long userI){
        SwapRequest swapRequest= repo.findById(swapId)
                .orElseThrow(() -> new BadRequestException("Swap request not found"));
        if(!swapRequest.getReceiver().getId().equals(userI)){
            throw new BadRequestException("You are not allowed to reject this request");
        }
        if(swapRequest.getSwapStatus()!=SwapStatus.PENDING){
            throw  new BadRequestException("swap is not pending");
        }
        swapRequest.setSwapStatus(SwapStatus.REJECTED);
        SwapRequest saved = repo.save(swapRequest);
        notificationService.createNotification( swapRequest.getSender().getId(),
                swapRequest.getReceiver().getFirstName() + " rejected your swap request",
                NotificationType.REQUEST_REJECTED );
        return mapper.toResponse(saved);
    }

    @Transactional
    public SwapRequestResponseDto cancelSwapRequest(Long swapId,Long userId){
        SwapRequest swapRequest=repo.findById(swapId).orElseThrow(()->new BadRequestException("Swap request not found"));
        if(!swapRequest.getSender().getId().equals(userId)){
            throw new BadRequestException("You are not allowed to cancel this request");
        }
        if(swapRequest.getSwapStatus()!=SwapStatus.PENDING){
            throw new BadRequestException("Swap is not pending");
        }
        swapRequest.setSwapStatus(SwapStatus.CANCELLED);
        SwapRequest savedSwap=repo.save(swapRequest);
        return mapper.toResponse(savedSwap);
    }

    @Transactional
    public SwapRequestResponseDto completeSwapRequest(Long swapId,Long userId){
        SwapRequest swapRequest=repo.findById(swapId)
                .orElseThrow(()->new BadRequestException("Swap request not found"));

        if(!swapRequest.getSender().getId().equals(userId)
                && !swapRequest.getReceiver().getId().equals(userId)){
            throw new BadRequestException("You are not allowed to complete this swap");
        }

        if (swapRequest.getSwapStatus() == SwapStatus.COMPLETED) {
            throw new BadRequestException("Swap is already completed");
        }

        if (swapRequest.getSwapStatus() == SwapStatus.COMPLETION_REQUESTED) {
            if (swapRequest.getCompletionRequestedBy() != null
                    && swapRequest.getCompletionRequestedBy().getId().equals(userId)) {
                throw new BadRequestException(
                        "The participant who requested completion cannot confirm it"
                );
            }

            swapRequest.setSwapStatus(SwapStatus.COMPLETED);
            SwapRequest saved = repo.save(swapRequest);

            notificationService.createNotification(
                    otherParticipantId(swapRequest, userId),
                    "The swap was marked as completed.",
                    NotificationType.SESSION_COMPLETED
            );

            return mapper.toResponse(saved);
        }

        if (swapRequest.getSwapStatus() != SwapStatus.ACCEPTED) {
            throw new BadRequestException("Swap is not accepted");
        }

        User requester = participantById(swapRequest, userId);

        swapRequest.setSwapStatus(SwapStatus.COMPLETION_REQUESTED);
        swapRequest.setCompletionRequestedAt(LocalDateTime.now());
        swapRequest.setCompletionRequestedBy(requester);

        SwapRequest saved = repo.save(swapRequest);

        notificationService.createNotification(
                otherParticipantId(swapRequest, userId),
                requester.getFirstName()
                        + " requested completion of your swap. Please confirm it.",
                NotificationType.SESSION_COMPLETION_REQUEST
        );

        return mapper.toResponse(saved);
    }

    private User participantById(SwapRequest swapRequest, Long userId) {
        if (swapRequest.getSender().getId().equals(userId)) {
            return swapRequest.getSender();
        }

        if (swapRequest.getReceiver().getId().equals(userId)) {
            return swapRequest.getReceiver();
        }

        throw new BadRequestException("You are not part of this swap request");
    }

    private Long otherParticipantId(SwapRequest swapRequest, Long userId) {
        if (swapRequest.getSender().getId().equals(userId)) {
            return swapRequest.getReceiver().getId();
        }

        return swapRequest.getSender().getId();
    }
}