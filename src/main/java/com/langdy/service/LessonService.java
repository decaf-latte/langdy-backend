package com.langdy.service;

import com.langdy.dto.CreateLessonCommand;
import com.langdy.entity.Lesson;
import com.langdy.entity.LessonStatus;
import com.langdy.entity.Teacher;
import com.langdy.exception.BusinessException;
import com.langdy.exception.ErrorCode;
import com.langdy.repository.CourseRepository;
import com.langdy.repository.LessonQueryRepository;
import com.langdy.repository.LessonRepository;
import com.langdy.repository.StudentRepository;
import com.langdy.repository.TeacherRepository;
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
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final LessonRepository lessonRepository;
    private final LessonQueryRepository lessonQueryRepository;

    public List<Teacher> getAvailableTeachers(Long courseId, LocalDateTime startAt) {
        StartTimeValidator.validate(startAt, clock);

        if (!courseRepository.existsById(courseId)) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        return lessonQueryRepository.findAvailableTeachers(startAt);
    }

    @Transactional
    public Lesson createLesson(CreateLessonCommand command) {
        StartTimeValidator.validate(command.getStartAt(), clock);

        courseRepository.findById(command.getCourseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        teacherRepository.findByIdWithLock(command.getTeacherId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TEACHER_NOT_FOUND));

        studentRepository.findByIdWithLock(command.getStudentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        if (lessonRepository.existsByTeacherIdAndStartAtAndStatus(command.getTeacherId(), command.getStartAt(), LessonStatus.BOOKED)) {
            throw new BusinessException(ErrorCode.TEACHER_SCHEDULE_CONFLICT);
        }
        if (lessonRepository.existsByStudentIdAndStartAtAndStatus(command.getStudentId(), command.getStartAt(), LessonStatus.BOOKED)) {
            throw new BusinessException(ErrorCode.STUDENT_SCHEDULE_CONFLICT);
        }

        Lesson lesson = Lesson.create(command.getCourseId(), command.getTeacherId(), command.getStudentId(), command.getStartAt());
        return lessonRepository.save(lesson);
    }
}
