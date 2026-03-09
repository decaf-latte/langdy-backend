package com.langdy.entity;

import com.langdy.exception.BusinessException;
import com.langdy.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = {
        @Index(name = "idx_lesson_teacher_start_status", columnList = "teacherId, startAt, status"),
        @Index(name = "idx_lesson_student_start_status", columnList = "studentId, startAt, status")
})
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
            throw new BusinessException(ErrorCode.INVALID_LESSON_STATUS);
        }
        this.status = LessonStatus.CANCELLED;
    }
}
