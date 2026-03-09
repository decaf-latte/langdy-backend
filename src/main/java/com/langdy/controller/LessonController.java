package com.langdy.controller;

import com.langdy.dto.AvailableTeachersResponse;
import com.langdy.entity.Teacher;
import com.langdy.service.LessonService;
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
    public ResponseEntity<AvailableTeachersResponse> getAvailableTeachers(
            @PathVariable Long courseId,
            @RequestParam LocalDateTime startAt) {
        List<Teacher> teachers = lessonService.getAvailableTeachers(courseId, startAt);
        return ResponseEntity.ok(AvailableTeachersResponse.from(teachers));
    }
}
