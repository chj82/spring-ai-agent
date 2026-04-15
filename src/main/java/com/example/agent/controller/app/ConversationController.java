package com.example.agent.controller.app;

import com.example.agent.common.context.UserContext;
import com.example.agent.common.response.ApiResponse;
import com.example.agent.model.dto.ConversationIdRequest;
import com.example.agent.model.dto.CreateConversationRequest;
import com.example.agent.model.dto.UpdateConversationTitleRequest;
import com.example.agent.model.vo.ConversationMessageVO;
import com.example.agent.model.vo.ConversationVO;
import com.example.agent.service.app.ConversationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping("/list")
    public ApiResponse<List<ConversationVO>> list() {
        Long userId = UserContext.getRequiredAppUser().getUserId();
        return ApiResponse.success(conversationService.list(userId));
    }

    @PostMapping("/create")
    public ApiResponse<ConversationVO> create(@Valid @RequestBody CreateConversationRequest request) {
        Long userId = UserContext.getRequiredAppUser().getUserId();
        return ApiResponse.success(conversationService.create(userId, request));
    }

    @GetMapping("/get")
    public ApiResponse<ConversationVO> get(@Valid @ModelAttribute ConversationIdRequest request) {
        Long userId = UserContext.getRequiredAppUser().getUserId();
        return ApiResponse.success(conversationService.get(userId, request));
    }

    @PostMapping("/update-title")
    public ApiResponse<Void> updateTitle(@Valid @RequestBody UpdateConversationTitleRequest request) {
        Long userId = UserContext.getRequiredAppUser().getUserId();
        conversationService.updateTitle(userId, request);
        return ApiResponse.success();
    }

    @PostMapping("/delete")
    public ApiResponse<Void> delete(@Valid @RequestBody ConversationIdRequest request) {
        Long userId = UserContext.getRequiredAppUser().getUserId();
        conversationService.delete(userId, request);
        return ApiResponse.success();
    }

    @GetMapping("/messages")
    public ApiResponse<List<ConversationMessageVO>> messages(@Valid @ModelAttribute ConversationIdRequest request) {
        Long userId = UserContext.getRequiredAppUser().getUserId();
        return ApiResponse.success(conversationService.messages(userId, request));
    }
}
