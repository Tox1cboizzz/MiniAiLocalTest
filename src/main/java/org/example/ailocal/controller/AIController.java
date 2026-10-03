package org.example.ailocal.controller;

import org.example.ailocal.service.ProjectReaderService;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AIController {

    private final OllamaChatModel chatModel;
    private final ProjectReaderService projectReaderService;

    public AIController(
            OllamaChatModel chatModel,
            ProjectReaderService projectReaderService
    ) {
        this.chatModel = chatModel;
        this.projectReaderService = projectReaderService;
    }

    @GetMapping("/chat")
    public Map<String, String> chat(
            @RequestParam String message
    ) {
        String response = chatModel.call(message);

        return Map.of(
                "question", message,
                "answer", response
        );
    }

    @GetMapping("/project-chat")
    public Map<String, String> projectChat(
            @RequestParam String message
    ) {

        try {

            String projectCode = projectReaderService.readProject();

            String prompt = """
                    Bạn là AI Developer Assistant.

                    Dưới đây là source code của một project Java Spring Boot.

                    Hãy chỉ sử dụng thông tin trong source code được cung cấp
                    để trả lời câu hỏi của developer.

                    Nếu source code không đủ thông tin thì nói rõ
                    "Không tìm thấy thông tin trong source code".

                    SOURCE CODE PROJECT:
                    
                    %s

                    ================================
                    CÂU HỎI:
                    %s
                    """.formatted(projectCode, message);

            String response = chatModel.call(prompt);

            return Map.of(
                    "question", message,
                    "answer", response
            );

        } catch (Exception e) {

            return Map.of(
                    "question", message,
                    "answer", "Lỗi đọc project: " + e.getMessage()
            );
        }
    }
}