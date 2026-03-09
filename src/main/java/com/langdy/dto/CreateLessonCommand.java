package com.langdy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CreateLessonCommand {

    private final LocalDateTime startAt;
    private final Long courseId;
    private final Long teacherId;
    private final Long studentId;
}
