package com.photo.controller;

import com.photo.service.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

/**
 * AI图像分析控制器
 * 提供图像内容识别和智能标签生成功能
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AIController {

    private final AIService aiService;
    private final com.photo.config.JwtService jwtService;

    /**
     * 分析图像内容并生成智能标签
     * POST /api/ai/analyze
     */
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeImageContent(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String token) {

        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body("Only image files are supported");
        }

        File tempFile = null;
        try {
            // 创建临时文件
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                originalFilename = "image.jpg";
            }

            String tempDir = System.getProperty("java.io.tmpdir");
            String uniqueFilename = "ai_analysis_" + UUID.randomUUID() + "_" + originalFilename;
            Path tempFilePath = Path.of(tempDir, uniqueFilename);
            Files.copy(file.getInputStream(), tempFilePath);
            tempFile = tempFilePath.toFile();

            log.info("Analyzing image content: {} ({} bytes)", originalFilename, file.getSize());

            // 使用AI服务分析图像内容
            List<String> aiTags = aiService.analyzeImageContent(tempFile);

            // 准备响应数据
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("filename", originalFilename);
            response.put("tags", aiTags);
            response.put("tagCount", aiTags.size());
            response.put("message", "Image analysis completed");

            log.info("AI analysis completed for {}: {} tags", originalFilename, aiTags.size());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Failed to process image file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process image file: " + e.getMessage());
        } catch (Exception e) {
            log.error("AI analysis error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("AI analysis failed: " + e.getMessage());
        } finally {
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (!deleted) {
                    log.warn("Failed to delete temporary file: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 执行详细的图像分析并返回更多信息
     * POST /api/ai/analyze/detailed
     */
    @PostMapping("/analyze/detailed")
    public ResponseEntity<?> analyzeImageContentDetailed(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String token) {

        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        File tempFile = null;
        try {
            // 创建临时文件
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                originalFilename = "image.jpg";
            }

            String tempDir = System.getProperty("java.io.tmpdir");
            String uniqueFilename = "ai_detailed_" + UUID.randomUUID() + "_" + originalFilename;
            Path tempFilePath = Path.of(tempDir, uniqueFilename);
            Files.copy(file.getInputStream(), tempFilePath);
            tempFile = tempFilePath.toFile();

            log.info("Detailed analysis for image: {} ({} bytes)", originalFilename, file.getSize());

            // 使用AI服务执行详细分析
            AIService.AIAnalysisResult result = aiService.detailedAnalyze(tempFile);

            // 准备响应数据
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("filename", originalFilename);
            response.put("tags", result.getTags());
            response.put("confidence", result.getConfidence());
            response.put("tagCount", result.getTags().size());
            response.put("message", "Detailed analysis completed");

            log.info("Detailed analysis completed for {}: confidence = {:.2f}%, tags = {}",
                    originalFilename, result.getConfidence() * 100, result.getTags());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Failed to process image file for detailed analysis: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process image file: " + e.getMessage());
        } catch (Exception e) {
            log.error("Detailed AI analysis error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Detailed AI analysis failed: " + e.getMessage());
        } finally {
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (!deleted) {
                    log.warn("Failed to delete temporary file: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 检查AI服务状态
     * GET /api/ai/status
     */
    @GetMapping("/status")
    public ResponseEntity<?> getAIStatus() {
        try {
            Map<String, Object> modelStatus = aiService.getModelStatus();
            boolean isModelAvailable = (boolean) modelStatus.get("available");

            Map<String, Object> status = new HashMap<>(modelStatus);
            status.put("model", "ResNet50");
            status.put("library", "Deep Java Library (DJL)");
            status.put("description", "ImageNet-based image classification model");

            if (!isModelAvailable) {
                status.put("warning", "AI模型未初始化，可能正在下载或初始化中");
                status.put("recommendation", "可以调用 /api/ai/reinitialize 手动重新初始化模型");
                status.put("simulationMode", "当前使用模拟模式基于文件名生成标签");
            } else {
                status.put("mode", "实际图像内容分析模式");
            }

            return ResponseEntity.ok(status);
        } catch (Exception e) {
            Map<String, Object> errorStatus = new HashMap<>();
            errorStatus.put("available", false);
            errorStatus.put("error", e.getMessage());
            errorStatus.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorStatus);
        }
    }

    /**
     * 手动重新初始化AI模型
     * POST /api/ai/reinitialize
     */
    @PostMapping("/reinitialize")
    public ResponseEntity<?> reinitializeModel() {
        try {
            String result = aiService.reinitializeModel();
            boolean isModelAvailable = aiService.isModelAvailable();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", result);
            response.put("modelAvailable", isModelAvailable);
            response.put("timestamp", System.currentTimeMillis());

            if (!isModelAvailable) {
                response.put("warning", "模型重新初始化失败，将使用模拟模式");
                response.put("advice", "请检查网络连接和磁盘空间，模型需要从DJL仓库下载");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "重新初始化失败: " + e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 辅助方法：从JWT令牌中提取用户名
     */
    private String extractUsernameFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwtToken = token.substring(7);
            return jwtService.extractUsername(jwtToken);
        }
        return null;
    }
}