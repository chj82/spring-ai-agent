package com.example.agent.controller.app;

import com.example.agent.common.context.UserContext;
import com.example.agent.common.response.ApiResponse;
import com.example.agent.model.dto.ChatSendRequest;
import com.example.agent.model.vo.ChatReplyVO;
import com.example.agent.service.chat.ChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/send")
    public ApiResponse<ChatReplyVO> send(@Valid @RequestBody ChatSendRequest request) {
        Long userId = UserContext.getRequiredAppUser().getUserId();
        return ApiResponse.success(chatService.send(userId, request));
    }
}
