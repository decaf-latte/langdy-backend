package com.langdy.controller

import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.annotation.Transactional
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CreateLessonControllerSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    @Autowired
    EntityManager em

    def setup() {
        em.createNativeQuery("INSERT INTO course (id, name) VALUES (1, 'English')").executeUpdate()
        em.createNativeQuery("INSERT INTO teacher (id, name) VALUES (1, 'John')").executeUpdate()
        em.createNativeQuery("INSERT INTO teacher (id, name) VALUES (2, 'Sarah')").executeUpdate()
        em.createNativeQuery("INSERT INTO student (id, name, os) VALUES (1, '김민수', 'IOS')").executeUpdate()
        em.createNativeQuery("INSERT INTO student (id, name, os) VALUES (2, '이영희', 'ANDROID')").executeUpdate()
        em.flush()
        em.clear()
    }

    def "수업 신청 - 정상"() {
        expect:
        mockMvc.perform(post("/api/v1/lessons")
                .header("X-Student-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"startAt":"2026-06-10T09:00:00","courseId":1,"teacherId":1}'))
                .andExpect(status().isCreated())
                .andExpect(jsonPath('$.success').value(true))
                .andExpect(jsonPath('$.data.lessonId').exists())
                .andExpect(jsonPath('$.data.courseId').value(1))
                .andExpect(jsonPath('$.data.teacherId').value(1))
                .andExpect(jsonPath('$.data.studentId').value(1))
                .andExpect(jsonPath('$.data.status').value("BOOKED"))
                .andExpect(jsonPath('$.data.startAt').value("2026-06-10T09:00:00"))
                .andExpect(jsonPath('$.data.endAt').value("2026-06-10T09:20:00"))
    }

    def "수업 신청 - X-Student-Id 헤더 누락"() {
        expect:
        mockMvc.perform(post("/api/v1/lessons")
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"startAt":"2026-06-10T09:00:00","courseId":1,"teacherId":1}'))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.code').value("MISSING_STUDENT_ID"))
    }

    def "수업 신청 - 존재하지 않는 코스"() {
        expect:
        mockMvc.perform(post("/api/v1/lessons")
                .header("X-Student-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"startAt":"2026-06-10T09:00:00","courseId":999,"teacherId":1}'))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath('$.code').value("COURSE_NOT_FOUND"))
    }

    def "수업 신청 - 존재하지 않는 선생님"() {
        expect:
        mockMvc.perform(post("/api/v1/lessons")
                .header("X-Student-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"startAt":"2026-06-10T09:00:00","courseId":1,"teacherId":999}'))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath('$.code').value("TEACHER_NOT_FOUND"))
    }

    def "수업 신청 - 존재하지 않는 학습자"() {
        expect:
        mockMvc.perform(post("/api/v1/lessons")
                .header("X-Student-Id", "999")
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"startAt":"2026-06-10T09:00:00","courseId":1,"teacherId":1}'))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath('$.code').value("STUDENT_NOT_FOUND"))
    }

    def "수업 신청 - 유효하지 않은 시작 시각"() {
        expect:
        mockMvc.perform(post("/api/v1/lessons")
                .header("X-Student-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"startAt":"2026-06-10T09:15:00","courseId":1,"teacherId":1}'))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.code').value("INVALID_START_TIME"))
    }

    def "수업 신청 - 선생님 시간 충돌"() {
        given:
        em.createNativeQuery(
                "INSERT INTO lesson (course_id, teacher_id, student_id, status, start_at, end_at) " +
                "VALUES (1, 1, 1, 'BOOKED', '2026-06-10T09:00:00', '2026-06-10T09:20:00')"
        ).executeUpdate()
        em.flush()
        em.clear()

        expect:
        mockMvc.perform(post("/api/v1/lessons")
                .header("X-Student-Id", "2")
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"startAt":"2026-06-10T09:00:00","courseId":1,"teacherId":1}'))
                .andExpect(status().isConflict())
                .andExpect(jsonPath('$.code').value("TEACHER_SCHEDULE_CONFLICT"))
    }

    def "수업 신청 - 학습자 시간 충돌"() {
        given:
        em.createNativeQuery(
                "INSERT INTO lesson (course_id, teacher_id, student_id, status, start_at, end_at) " +
                "VALUES (1, 1, 1, 'BOOKED', '2026-06-10T09:00:00', '2026-06-10T09:20:00')"
        ).executeUpdate()
        em.flush()
        em.clear()

        expect:
        mockMvc.perform(post("/api/v1/lessons")
                .header("X-Student-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"startAt":"2026-06-10T09:00:00","courseId":1,"teacherId":2}'))
                .andExpect(status().isConflict())
                .andExpect(jsonPath('$.code').value("STUDENT_SCHEDULE_CONFLICT"))
    }

    def "수업 신청 - @Valid 필드 누락"() {
        expect:
        mockMvc.perform(post("/api/v1/lessons")
                .header("X-Student-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content('{"courseId":1,"teacherId":1}'))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.code').value("VALIDATION_ERROR"))
    }
}
