package com.langdy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CreateLessonRequest {

    @NotNull(message = "수업 시작 시각은 필수입니다.")
    private LocalDateTime startAt;

    @NotNull(message = "코스 ID는 필수입니다.")
    private Long courseId;

    @NotNull(message = "선생님 ID는 필수입니다.")
    private Long teacherId;

    public CreateLessonCommand toCommand(Long studentId) {
        return new CreateLessonCommand(startAt, courseId, teacherId, studentId);
    }
}
