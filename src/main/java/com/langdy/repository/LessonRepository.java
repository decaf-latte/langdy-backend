package com.langdy.repository;

import com.langdy.entity.Lesson;
import com.langdy.entity.LessonStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    boolean existsByTeacherIdAndStartAtAndStatus(Long teacherId, LocalDateTime startAt, LessonStatus status);

    boolean existsByStudentIdAndStartAtAndStatus(Long studentId, LocalDateTime startAt, LessonStatus status);
}
