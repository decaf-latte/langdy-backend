package com.langdy.service;

import com.langdy.entity.Teacher;
import com.langdy.exception.BusinessException;
import com.langdy.exception.ErrorCode;
import com.langdy.repository.CourseRepository;
import com.langdy.repository.LessonQueryRepository;
import com.langdy.util.StartTimeValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonService {

    private final Clock clock;
    private final CourseRepository courseRepository;
    private final LessonQueryRepository lessonQueryRepository;

    public List<Teacher> getAvailableTeachers(Long courseId, LocalDateTime startAt) {
        StartTimeValidator.validate(startAt, clock);

        if (!courseRepository.existsById(courseId)) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        return lessonQueryRepository.findAvailableTeachers(startAt);
    }
}
