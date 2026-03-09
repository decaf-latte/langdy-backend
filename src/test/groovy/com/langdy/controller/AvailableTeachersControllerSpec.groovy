package com.langdy.controller

import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.annotation.Transactional
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AvailableTeachersControllerSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    @Autowired
    EntityManager em

    Long courseId
    Long teacherId1
    Long studentId

    def setup() {
        em.createNativeQuery("INSERT INTO course (id, name) VALUES (1, 'English')").executeUpdate()
        em.createNativeQuery("INSERT INTO teacher (id, name) VALUES (1, 'John')").executeUpdate()
        em.createNativeQuery("INSERT INTO teacher (id, name) VALUES (2, 'Sarah')").executeUpdate()
        em.createNativeQuery("INSERT INTO teacher (id, name) VALUES (3, 'Mike')").executeUpdate()
        em.createNativeQuery("INSERT INTO student (id, name, os) VALUES (1, '김민수', 'IOS')").executeUpdate()
        em.flush()
        em.clear()
    }

    def "수업 가능한 선생님 목록 조회 - 전체 선생님 반환"() {
        expect:
        mockMvc.perform(get("/api/v1/courses/1/available-teachers")
                .param("startAt", "2026-03-10T09:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.success').value(true))
                .andExpect(jsonPath('$.data.teachers').isArray())
                .andExpect(jsonPath('$.data.teachers.length()').value(3))
    }

    def "수업 가능한 선생님 목록 조회 - 예약된 선생님 제외"() {
        given:
        em.createNativeQuery(
                "INSERT INTO lesson (course_id, teacher_id, student_id, status, start_at, end_at) " +
                "VALUES (1, 1, 1, 'BOOKED', '2026-03-10T09:00:00', '2026-03-10T09:20:00')"
        ).executeUpdate()
        em.flush()
        em.clear()

        expect:
        mockMvc.perform(get("/api/v1/courses/1/available-teachers")
                .param("startAt", "2026-03-10T09:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.success').value(true))
                .andExpect(jsonPath('$.data.teachers.length()').value(2))
    }

    def "수업 가능한 선생님 목록 조회 - 존재하지 않는 코스"() {
        expect:
        mockMvc.perform(get("/api/v1/courses/999/available-teachers")
                .param("startAt", "2026-03-10T09:00:00"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath('$.code').value("COURSE_NOT_FOUND"))
    }

    def "수업 가능한 선생님 목록 조회 - 유효하지 않은 시작 시각"() {
        expect:
        mockMvc.perform(get("/api/v1/courses/1/available-teachers")
                .param("startAt", "2026-03-10T09:15:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.code').value("INVALID_START_TIME"))
    }

    def "수업 가능한 선생님 목록 조회 - startAt 파라미터 누락"() {
        expect:
        mockMvc.perform(get("/api/v1/courses/1/available-teachers"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.code').value("MISSING_PARAMETER"))
    }

    def "수업 가능한 선생님 목록 조회 - 잘못된 날짜 형식"() {
        expect:
        mockMvc.perform(get("/api/v1/courses/1/available-teachers")
                .param("startAt", "invalid-date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath('$.code').value("INVALID_PARAMETER"))
    }
}
