package com.langdy.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
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

    public static Lesson create(Long courseId, Long teacherId, Long studentId, LocalDateTime startAt) {
        Lesson lesson = new Lesson();
        lesson.courseId = courseId;
        lesson.teacherId = teacherId;
        lesson.studentId = studentId;
        lesson.status = LessonStatus.BOOKED;
        lesson.startAt = startAt;
        lesson.endAt = startAt.plusMinutes(LESSON_DURATION_MINUTES);
        return lesson;
    }

    public void cancel() {
        if (this.status != LessonStatus.BOOKED) {
            throw new IllegalStateException("BOOKED 상태에서만 취소할 수 있습니다.");
        }
        this.status = LessonStatus.CANCELLED;
    }
}
