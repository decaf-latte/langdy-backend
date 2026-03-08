package com.langdy.lesson.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Lesson {

    private static final int LESSON_DURATION_MINUTES = 20;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private Long teacherId;

    @Column(nullable = false)
    private Long studentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LessonStatus status;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Builder
    public Lesson(Long courseId, Long teacherId, Long studentId, LocalDateTime startAt) {
        this.courseId = courseId;
        this.teacherId = teacherId;
        this.studentId = studentId;
        this.status = LessonStatus.BOOKED;
        this.startAt = startAt;
        this.endAt = startAt.plusMinutes(LESSON_DURATION_MINUTES);
    }
}
