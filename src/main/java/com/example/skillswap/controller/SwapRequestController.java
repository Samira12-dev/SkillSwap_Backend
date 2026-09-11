package com.example.skillswap.controller;

import com.example.skillswap.dto.request.SwapRequestRequestDto;
import com.example.skillswap.dto.response.SwapRequestResponseDto;
import com.example.skillswap.service.SwapRequestService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/swaprequests")
@RequiredArgsConstructor
public class SwapRequestController {
    private final SwapRequestService service;

    @Operation(summary = "Create a swap request")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{senderId}")
    public SwapRequestResponseDto createSwap(@PathVariable Long senderId, @Valid @RequestBody SwapRequestRequestDto requestDto){
        return  service.createSwapRequest(senderId,requestDto);
    }

    @Operation(summary = "Get swap request by ID")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{swapId}")
    public SwapRequestResponseDto getSwapRequestById(@PathVariable Long swapId){
        return service.getSwapRequestById(swapId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<SwapRequestResponseDto>> getAllSwapRequests(@PageableDefault(page = 0, size = 10) Pageable pageable){
        return  ResponseEntity.ok(service.getAllSwapRequests(pageable));
    }

    @Operation(summary = "Get received swap requests")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/received/{userId}")
    public ResponseEntity<Page<SwapRequestResponseDto>>getReceivedRequests(@PathVariable Long userId, @PageableDefault(page = 0, size = 10) Pageable pageable){
        return ResponseEntity.ok(service.getReceivedRequests(userId, pageable));
    }

    @Operation(summary = "Get sent swap requests")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/sent/{userId}")
    public ResponseEntity<Page<SwapRequestResponseDto>>getSentRequests(@PathVariable Long userId, @PageableDefault(page = 0, size = 10) Pageable pageable){
        return ResponseEntity.ok(service.getSentRequests(userId, pageable));
    }

    @Operation(summary = "Accept a swap request")
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{swapId}/accept/{userId}")
    public SwapRequestResponseDto acceptSwapRequest(@PathVariable Long swapId,@PathVariable Long userId){
        return service.acceptSwapRequest(swapId,userId);
    }

    @Operation(summary = "Reject a swap request")
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{swapId}/reject/{userId}")
    public SwapRequestResponseDto rejectSwapRequest(@PathVariable Long swapId,@PathVariable Long userId){
        return service.rejectSwapRequest(swapId,userId);
    }

    @Operation(summary = "Cancel a swap request")
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{swapId}/cancel/{userId}")
    public SwapRequestResponseDto cancelSwapRequest(@PathVariable Long swapId,@PathVariable Long userId){
        return service.cancelSwapRequest(swapId,userId);
    }

    @Operation(summary = "Complete a swap request")
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{swapId}/complete/{userId}")
    public SwapRequestResponseDto completeSwapRequest(@PathVariable Long swapId, @PathVariable Long userId) {
        return service.completeSwapRequest(swapId, userId);
    }
}