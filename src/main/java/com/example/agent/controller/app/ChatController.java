package com.example.agent.controller.app;

import com.example.agent.common.context.UserContext;
import com.example.agent.model.dto.ChatSendRequest;
import com.example.agent.service.chat.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/app/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(path = "/send", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter send(@Valid @RequestBody ChatSendRequest request) {
        Long userId = UserContext.getRequiredAppUser().getUserId();
        return chatService.send(userId, request);
    }
}
