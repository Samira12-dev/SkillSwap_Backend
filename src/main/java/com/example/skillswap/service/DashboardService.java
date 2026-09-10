package com.example.skillswap.service;

import com.example.skillswap.dto.response.AdminDashboardResponseDto;
import com.example.skillswap.dto.response.DashboardResponseDto;
import com.example.skillswap.entity.*;
import com.example.skillswap.enums.SessionStatus;
import com.example.skillswap.enums.SwapStatus;
import com.example.skillswap.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor

public class DashboardService {
    private  final SwapRequestRepo swapRequestRepo;
    private final SessionRepo sessionRepo;
    private final MessageRepo messageRepo;
    private  final NotificationRepo notificationRepo;
    private final UserRepo userRepo;
    private final SkillRepo skillRepo;

    public DashboardResponseDto getUserDashboard(String email){

        User user =userRepo.findByEmail(email).orElseThrow(()->new RuntimeException("User not found"));
        Long userId =user.getId();
        int pendingRequest = swapRequestRepo.findByReceiverIdAndStatus(userId, SwapStatus.PENDING).size();


        List<SwapRequest>sentRequest=swapRequestRepo.findBySenderId(userId);
        List<SwapRequest> receivedRequest= swapRequestRepo.findByReceiverId(userId);
        int upComingSession =0;
        for(SwapRequest swap :sentRequest){
            if(swap.getConversation()!= null){
               List<Session>sessions =sessionRepo.findByConversationSwapRequestId(swap.getId());
               for (Session session:sessions){
                   if(session.getDate().isAfter(LocalDateTime.now())){
                       upComingSession ++;
                   }
               }
            }
        }
        for (SwapRequest request :receivedRequest){
            if(request.getConversation()!= null){
                List<Session> sessions= sessionRepo.findByConversationSwapRequestId(request.getId());
                for (Session session:sessions){
                    if (session.getDate().isAfter(LocalDateTime.now())){
                        upComingSession ++;
                    }
                }
            }
        }


        int unReadMessage =0;
        for(SwapRequest request :receivedRequest){
            if(request.getConversation() != null){
                List<Message> messages =messageRepo.findByConversationId(request.getConversation().getId());

                for (Message message:messages){
                    if(!message.isRead()&& !message.getSender().getId().equals(userId)){
                        unReadMessage ++;
                    }
                }
            }
        }
        int notificationIsRead= 0;
        List<Notification> notifications= notificationRepo.findByUserId(userId);
        for(Notification notification1 :notifications){
            if(!notification1.isRead()){
                notificationIsRead ++;
            }
        }



        return new DashboardResponseDto(
                pendingRequest,
                upComingSession,
                unReadMessage,
                notificationIsRead
        );
    }
    public AdminDashboardResponseDto getAdminDashboard(){
        int totalUsers= userRepo.findAll().size();
        int totalSkills= skillRepo.findAll().size();
        int totalSwapRequest= swapRequestRepo.findAll().size();

        int activeSession =0;
        int completeSession =0;

        List<Session> sessionList =sessionRepo.findAll();
        for (Session session :sessionList){
            if(session.getStatus() == SessionStatus.CONFIRMED){
                activeSession ++;
            }
            if(session.getStatus() == SessionStatus.COMPLETED){
                completeSession ++;
            }


        }
        return new AdminDashboardResponseDto(
                totalUsers,
                totalSkills,
                totalSwapRequest,
                activeSession,
                completeSession
        );
    }
}
