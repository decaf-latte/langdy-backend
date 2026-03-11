package com.langdy.service

import com.langdy.dto.CreateLessonCommand
import com.langdy.entity.LessonStatus
import com.langdy.exception.BusinessException
import com.langdy.repository.LessonRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Specification

import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
class LessonConcurrencySpec extends Specification {

    @Autowired
    LessonService lessonService

    @Autowired
    LessonRepository lessonRepository

    @Autowired
    JdbcTemplate jdbcTemplate

    def setup() {
        jdbcTemplate.execute("DELETE FROM lesson")
        jdbcTemplate.execute("DELETE FROM student")
        jdbcTemplate.execute("DELETE FROM teacher")
        jdbcTemplate.execute("DELETE FROM course")

        jdbcTemplate.execute("INSERT INTO course (id, name) VALUES (1, 'English')")
        jdbcTemplate.execute("INSERT INTO teacher (id, name) VALUES (1, 'John')")
        (1..10).each { i ->
            jdbcTemplate.execute("INSERT INTO student (id, name, os) VALUES (${i}, '학생${i}', 'IOS')")
        }
    }

    def cleanup() {
        jdbcTemplate.execute("DELETE FROM lesson")
        jdbcTemplate.execute("DELETE FROM student")
        jdbcTemplate.execute("DELETE FROM teacher")
        jdbcTemplate.execute("DELETE FROM course")
    }

    def "동시에 같은 선생님 같은 시간에 예약하면 하나만 성공한다"() {
        given:
        def threadCount = 10
        def executor = Executors.newFixedThreadPool(threadCount)
        def latch = new CountDownLatch(threadCount)
        def successCount = new AtomicInteger(0)
        def startAt = LocalDateTime.of(2026, 6, 10, 9, 0, 0)

        when:
        (1..threadCount).each { i ->
            executor.submit({
                try {
                    lessonService.createLesson(new CreateLessonCommand(startAt, 1L, 1L, (long) i))
                    successCount.incrementAndGet()
                } catch (BusinessException e) {
                    // TEACHER_SCHEDULE_CONFLICT 예상
                } finally {
                    latch.countDown()
                }
            })
        }
        latch.await()
        executor.shutdown()

        then:
        successCount.get() == 1
        lessonRepository.findAll().count { it.status == LessonStatus.BOOKED } == 1
    }
}
