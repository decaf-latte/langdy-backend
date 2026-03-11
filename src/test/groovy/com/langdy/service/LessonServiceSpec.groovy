package com.langdy.service

import com.langdy.dto.CreateLessonCommand
import com.langdy.entity.Course
import com.langdy.entity.Lesson
import com.langdy.entity.LessonStatus
import com.langdy.entity.Student
import com.langdy.entity.Teacher
import com.langdy.exception.BusinessException
import com.langdy.exception.ErrorCode
import com.langdy.repository.*
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneOffset

class LessonServiceSpec extends Specification {

    def courseRepository = Mock(CourseRepository)
    def teacherRepository = Mock(TeacherRepository)
    def studentRepository = Mock(StudentRepository)
    def lessonRepository = Mock(LessonRepository)
    def lessonQueryRepository = Mock(LessonQueryRepository)
    def clock = Clock.fixed(
            LocalDateTime.of(2026, 3, 1, 0, 0).toInstant(ZoneOffset.UTC),
            ZoneOffset.UTC
    )

    def lessonService = new LessonService(clock, courseRepository, teacherRepository, studentRepository, lessonRepository, lessonQueryRepository)

    def startAt = LocalDateTime.of(2026, 3, 10, 9, 0, 0)

    @Shared def validCourse = Optional.of(new Course())
    @Shared def validTeacher = Optional.of(new Teacher())
    @Shared def validStudent = Optional.of(new Student())
    @Shared def empty = Optional.empty()

    // === TASK#1 ===

    def "수업 가능한 선생님 조회 - 정상"() {
        given:
        courseRepository.findById(1L) >> validCourse
        lessonQueryRepository.findAvailableTeachers(startAt) >> []

        when:
        def result = lessonService.getAvailableTeachers(1L, startAt)

        then:
        result != null
    }

    def "수업 가능한 선생님 조회 - 코스가 없으면 COURSE_NOT_FOUND"() {
        given:
        courseRepository.findById(999L) >> empty

        when:
        lessonService.getAvailableTeachers(999L, startAt)

        then:
        def e = thrown(BusinessException)
        e.errorCode == ErrorCode.COURSE_NOT_FOUND
    }

    def "수업 가능한 선생님 조회 - 유효하지 않은 시작 시각이면 INVALID_START_TIME"() {
        when:
        lessonService.getAvailableTeachers(1L, LocalDateTime.of(2026, 3, 10, 9, 15, 0))

        then:
        def e = thrown(BusinessException)
        e.errorCode == ErrorCode.INVALID_START_TIME
    }

    def "수업 가능한 선생님 조회 - 과거 시각이면 PAST_START_TIME"() {
        when:
        lessonService.getAvailableTeachers(1L, LocalDateTime.of(2026, 2, 28, 9, 0, 0))

        then:
        def e = thrown(BusinessException)
        e.errorCode == ErrorCode.PAST_START_TIME
    }

    // === TASK#2 ===

    def "수업 신청 - 정상"() {
        given:
        def command = new CreateLessonCommand(startAt, 1L, 1L, 1L)
        courseRepository.findById(1L) >> validCourse
        teacherRepository.findByIdWithLock(1L) >> validTeacher
        studentRepository.findByIdWithLock(1L) >> validStudent
        lessonRepository.existsByTeacherIdAndStartAtAndStatus(1L, startAt, LessonStatus.BOOKED) >> false
        lessonRepository.existsByStudentIdAndStartAtAndStatus(1L, startAt, LessonStatus.BOOKED) >> false
        lessonRepository.save(_) >> { Lesson lesson -> lesson }

        when:
        def result = lessonService.createLesson(command)

        then:
        result.status == LessonStatus.BOOKED
        result.endAt == startAt.plusMinutes(20)
    }

    @Unroll
    def "수업 신청 - #description"() {
        given:
        def command = new CreateLessonCommand(startAt, 1L, 1L, 1L)
        courseRepository.findById(1L) >> courseResult
        teacherRepository.findByIdWithLock(1L) >> teacherResult
        studentRepository.findByIdWithLock(1L) >> studentResult
        lessonRepository.existsByTeacherIdAndStartAtAndStatus(1L, startAt, LessonStatus.BOOKED) >> teacherConflict
        lessonRepository.existsByStudentIdAndStartAtAndStatus(1L, startAt, LessonStatus.BOOKED) >> studentConflict

        when:
        lessonService.createLesson(command)

        then:
        def e = thrown(BusinessException)
        e.errorCode == expectedError

        where:
        description       | courseResult | teacherResult | studentResult | teacherConflict | studentConflict || expectedError
        "코스가 없으면"    | empty       | empty         | empty         | false           | false           || ErrorCode.COURSE_NOT_FOUND
        "선생님이 없으면"  | validCourse | empty         | empty         | false           | false           || ErrorCode.TEACHER_NOT_FOUND
        "학습자가 없으면"  | validCourse | validTeacher  | empty         | false           | false           || ErrorCode.STUDENT_NOT_FOUND
        "선생님 시간 충돌" | validCourse | validTeacher  | validStudent  | true            | false           || ErrorCode.TEACHER_SCHEDULE_CONFLICT
        "학습자 시간 충돌" | validCourse | validTeacher  | validStudent  | false           | true            || ErrorCode.STUDENT_SCHEDULE_CONFLICT
    }
}
