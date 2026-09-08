package com.example.skillswap.controller;

import com.example.skillswap.dto.request.SwapRequestRequestDto;
import com.example.skillswap.dto.response.SwapRequestResponseDto;
import com.example.skillswap.service.SwapRequestService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/swaprequests")
@RequiredArgsConstructor
public class SwapRequestController {
    private final SwapRequestService service;

    @Operation(summary = "Create a swap request")
    @PostMapping("/{senderId}")
    public SwapRequestResponseDto createSwap(@PathVariable Long senderId, @Valid @RequestBody SwapRequestRequestDto requestDto){
        return  service.createSwapRequest(senderId,requestDto);
    }

    @Operation(summary = "Get swap request by ID")
    @GetMapping("/{swapId}")
    public SwapRequestResponseDto getSwapRequestById(@PathVariable Long swapId){
        return service.getSwapRequestById(swapId);
    }

    @GetMapping
    public ResponseEntity<List<SwapRequestResponseDto>> getAllSwapRequests(){
        return  ResponseEntity.ok(service.getAllSwapRequests());
    }

    @Operation(summary = "Get received swap requests")
    @GetMapping("/received/{userId}")
    public ResponseEntity<List<SwapRequestResponseDto>>getReceivedRequests(@PathVariable Long userId){
        return ResponseEntity.ok(service.getReceivedRequests(userId));
    }

    @Operation(summary = "Get sent swap requests")
    @GetMapping("/sent/{userId}")
    public ResponseEntity<List<SwapRequestResponseDto>>getSentRequests(@PathVariable Long userId){
        return ResponseEntity.ok(service.getSentRequests(userId));
    }

    @Operation(summary = "Accept a swap request")
    @PutMapping("/{swapId}/accept/{userId}")
    public SwapRequestResponseDto acceptSwapRequest(@PathVariable Long swapId,@PathVariable Long userId){
        return service.acceptSwapRequest(swapId,userId);
    }

    @Operation(summary = "Reject a swap request")
    @PutMapping("/{swapId}/reject/{userId}")
    public SwapRequestResponseDto rejectSwapRequest(@PathVariable Long swapId,@PathVariable Long userId){
        return service.rejectSwapRequest(swapId,userId);
    }

    @Operation(summary = "Cancel a swap request")
    @PutMapping("/{swapId}/cancel/{userId}")
    public SwapRequestResponseDto cancelSwapRequest(@PathVariable Long swapId,@PathVariable Long userId){
        return service.cancelSwapRequest(swapId,userId);
    }

    @Operation(summary = "Complete a swap request")
    @PutMapping("/{swapId}/complete")
    public  SwapRequestResponseDto completeSwapRequest(@PathVariable Long swapId){
        return service.completeSwapRequest(swapId);
    }
}
