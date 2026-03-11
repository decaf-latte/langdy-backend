package com.langdy.controller;

import com.langdy.dto.ApiResponse;
import com.langdy.dto.AvailableTeachersResponse;
import com.langdy.dto.CreateLessonRequest;
import com.langdy.dto.CreateLessonResponse;
import com.langdy.util.ResponseUtil;
import com.langdy.entity.Lesson;
import com.langdy.entity.Teacher;
import com.langdy.service.LessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @GetMapping("/courses/{courseId}/available-teachers")
    public ResponseEntity<ApiResponse<AvailableTeachersResponse>> getAvailableTeachers(
            @PathVariable Long courseId,
            @RequestParam LocalDateTime startAt) {
        List<Teacher> teachers = lessonService.getAvailableTeachers(courseId, startAt);
        return ResponseUtil.ok(AvailableTeachersResponse.from(teachers));
    }

    @PostMapping("/lessons")
    public ResponseEntity<ApiResponse<CreateLessonResponse>> createLesson(
            @RequestHeader("X-Student-Id") Long studentId,
            @Valid @RequestBody CreateLessonRequest request) {
        Lesson lesson = lessonService.createLesson(request.toCommand(studentId));
        return ResponseUtil.created(CreateLessonResponse.from(lesson));
    }
}
