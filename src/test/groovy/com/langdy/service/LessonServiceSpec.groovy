package com.langdy.service

import com.langdy.entity.Teacher
import com.langdy.exception.BusinessException
import com.langdy.exception.ErrorCode
import com.langdy.repository.*
import spock.lang.Specification

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

    def "수업 가능한 선생님 조회 - 정상"() {
        given:
        courseRepository.existsById(1L) >> true
        lessonQueryRepository.findAvailableTeachers(startAt) >> []

        when:
        def result = lessonService.getAvailableTeachers(1L, startAt)

        then:
        result != null
    }

    def "수업 가능한 선생님 조회 - 코스가 없으면 COURSE_NOT_FOUND"() {
        given:
        courseRepository.existsById(999L) >> false

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
}
