package com.example.skillswap.service;

import com.example.skillswap.dto.request.SwapRequestRequestDto;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SwapRequestService {
    private final SwapRequestRepo repo;
    private final SwapRequestMapper mapper;
    private final UserRepo userRepo;
    private  final SkillRepo skillRepo;
    private final SkillDetailsRepo detailsRepo;
    @Transactional
    public SwapRequestResponseDto createSwapRequest(Long senderId, SwapRequestRequestDto requestDto){
        User sender= userRepo.findById(senderId).orElseThrow(()->new RuntimeException("Sender not found"));
        User receiver = userRepo.findById(requestDto.getReceiverId()).orElseThrow(()->new RuntimeException("Receiver not found"));
        if(senderId.equals(requestDto.getReceiverId())){
            throw new RuntimeException("You can't send a swap request to yourself");
        }
        Skill skillOffered =skillRepo.findById(requestDto.getSkillOfferedId())
                .orElseThrow(() -> new RuntimeException("Offered skill not found"));
        Skill skillWanted = skillRepo.findById(requestDto.getSkillWantedId())
                .orElseThrow(() -> new RuntimeException("Wanted skill not found"));

        if(!detailsRepo.existsByUserIdAndSkillId(receiver.getId(),requestDto.getSkillWantedId())){
            throw new RuntimeException(
                    "Receiver does not have the wanted skill");
        }
        SwapRequest swapRequest= new SwapRequest();
        swapRequest.setSender(sender);
        swapRequest.setReceiver(receiver);
        swapRequest.setSkillOffered(skillOffered);
        swapRequest.setSkillWanted(skillWanted);
        swapRequest.setMessage(requestDto.getMessage());
        SwapRequest savedSwap =repo.save(swapRequest);
        return mapper.toResponse(savedSwap);
    }

    @Transactional
    public SwapRequestResponseDto getSwapRequestById(Long swapId){
        SwapRequest swapRequest =repo.findById(swapId).orElseThrow(()->new RuntimeException("swap request not found"));
        return mapper.toResponse(swapRequest);
    }

    @Transactional
    public List<SwapRequestResponseDto> getAllSwapRequests(){
        return repo.findAll().stream()
                .map(mapper::toResponse).toList();
    }

    @Transactional
    public List<SwapRequestResponseDto>getReceivedRequests(Long userId){
        List<SwapRequest> listOfRequestReceived =repo.findByReceiverId(userId);
        return listOfRequestReceived.stream()
                .map(mapper::toResponse).toList();

    }

    @Transactional
    public List<SwapRequestResponseDto>getSentRequests(Long userId){
        List<SwapRequest> listOfRequestSender = repo.findBySenderId(userId);
        return listOfRequestSender.stream()
                .map(mapper::toResponse).toList();
    }

    @Transactional
    public SwapRequestResponseDto acceptSwapRequest(Long swapId,Long userId ){
        SwapRequest swapRequest= repo.findById(swapId)
                .orElseThrow(() -> new RuntimeException("Swap request not found"));
        if(!swapRequest.getReceiver().getId().equals(userId)){
            throw new RuntimeException("You are not allowed to accept this request");
        }

        if(swapRequest.getSwapStatus()!= SwapStatus.PENDING){
            throw  new RuntimeException("swap is not pending");
        }
        swapRequest.setSwapStatus(SwapStatus.ACCEPTED);
        SwapRequest savedRequest = repo.save(swapRequest);
        return mapper.toResponse(savedRequest);
    }
    @Transactional
    public SwapRequestResponseDto rejectSwapRequest(Long swapId,Long userI){
        SwapRequest swapRequest= repo.findById(swapId)
                .orElseThrow(() -> new RuntimeException("Swap request not found"));
        if(!swapRequest.getReceiver().getId().equals(userI)){
            throw new RuntimeException("You are not allowed to reject this request");
        }
        if(swapRequest.getSwapStatus()!=SwapStatus.PENDING){
            throw  new RuntimeException("swap is not pending");
        }
        swapRequest.setSwapStatus(SwapStatus.REJECTED);
        SwapRequest saved = repo.save(swapRequest);
        return mapper.toResponse(saved);
    }

    @Transactional
    public SwapRequestResponseDto cancelSwapRequest(Long swapId,Long userId){
        SwapRequest swapRequest=repo.findById(swapId).orElseThrow(()->new RuntimeException("Swap request not found"));
        if(!swapRequest.getSender().getId().equals(userId)){
            throw new RuntimeException("You are not allowed to cancel this request");
        }
        if(swapRequest.getSwapStatus()!=SwapStatus.PENDING){
            throw new RuntimeException("Swap is not pending");
        }
        swapRequest.setSwapStatus(SwapStatus.CANCELLED);
        SwapRequest savedSwap=repo.save(swapRequest);
        return mapper.toResponse(savedSwap);
    }

    @Transactional
    public SwapRequestResponseDto completeSwapRequest(Long swapId,Long userId){
        SwapRequest swapRequest=repo.findById(swapId).orElseThrow(()->new RuntimeException("Swap request not found "));
        if(!swapRequest.getSender().getId().equals(userId) && !swapRequest.getReceiver().getId().equals(userId)){
            throw  new RuntimeException("You are not allowed to complete this swap");
        }
        if(swapRequest.getSwapStatus()!=SwapStatus.ACCEPTED){
            throw new RuntimeException("Swap is not accepted");
        }
        swapRequest.setSwapStatus(SwapStatus.COMPLETED);
        SwapRequest saved= repo.save(swapRequest);
        return mapper.toResponse(saved);
    }
}
