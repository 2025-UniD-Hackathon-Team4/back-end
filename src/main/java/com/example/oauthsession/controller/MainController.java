package com.example.oauthsession.controller;

import com.example.oauthsession.amazon.s3.AmazonS3Manager;
import com.example.oauthsession.service.UpstageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.UUID;

@CrossOrigin(origins = "")
@RestController
@RequiredArgsConstructor
public class MainController {

    private final UpstageService upstageService;
    private final AmazonS3Manager amazonS3Manager;

    @GetMapping("/")
    public String mainPage(){
        return "main";
    }

    @GetMapping("/chat")
    public Mono<String> getResponse(@RequestParam String prompt, HttpSession session){
        String model = "solar-pro2";
        return upstageService.getChatResponseText(model, prompt, session);
    }

    @PostMapping(
            value = "/upload",
            consumes = {"multipart/form-data"}
    )
    @Operation(summary = "파일 업로드", description = "S3에 파일 업로드")
    public String uploadFile(
            @Parameter(description = "업로드할 파일", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart("file") MultipartFile file)  {

        String keyName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        return amazonS3Manager.uploadFile(keyName, file);
    }
}
