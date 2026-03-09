package com.langdy.dto;

import com.langdy.entity.Lesson;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CreateLessonResponse {

    private final Long lessonId;
    private final Long courseId;
    private final Long teacherId;
    private final Long studentId;
    private final String status;
    private final LocalDateTime startAt;
    private final LocalDateTime endAt;

    public static CreateLessonResponse from(Lesson lesson) {
        return new CreateLessonResponse(
                lesson.getId(),
                lesson.getCourseId(),
                lesson.getTeacherId(),
                lesson.getStudentId(),
                lesson.getStatus().name(),
                lesson.getStartAt(),
                lesson.getEndAt()
        );
    }
}
