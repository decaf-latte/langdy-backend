# TASK#3 — 수업 신청 이후 알림 발송 인프라 설계

## 1. 개요

수업 예약이 완료되면 학습자와 선생님에게 알림을 발송한다.
알림은 수업 예약 트랜잭션과 분리하여 비동기로 처리한다.

---

## 2. 아키텍처

```
[수업 신청 API]
       │
       ▼
  Lesson INSERT (DB 트랜잭션)
       │
       ▼
  트랜잭션 커밋
       │
       ▼
  @TransactionalEventListener(AFTER_COMMIT)
       │
       ▼
  KafkaProducer → topic: lesson-notification
       │
       ▼
  KafkaConsumer (Consumer Group: notification-group)
       │
       ├── Student 알림
       │     ├── IOS → APNs
       │     └── ANDROID → FCM
       │
       └── Teacher 알림
             └── Push 또는 이메일
```

---

## 3. 왜 트랜잭션과 분리하는가

- `AFTER_COMMIT` 이벤트를 사용하면 Lesson INSERT가 성공한 후에만 알림이 발행된다.
- 만약 트랜잭션 내에서 알림을 보내면, DB 롤백이 발생해도 알림은 이미 전송된 상태가 된다.
- 알림 발송 실패가 수업 예약을 롤백시키지 않아야 한다.

---

## 4. 왜 Kafka인가

| 요구사항 | Kafka 대응 |
|---------|-----------|
| 멀티 인스턴스 환경에서 중복 발송 방지 | Consumer Group으로 파티션당 하나의 Consumer만 처리 |
| 알림 실패 시 재처리 | offset 기반 재소비 + Dead Letter Topic |
| 메시지 순서 보장 | 같은 lessonId → 같은 파티션 (Key 설정) |
| 확장성 | 파티션 수 증가로 수평 확장 가능 |

---

## 5. 메시지 구조

```json
{
    "lessonId": 1,
    "courseId": 1,
    "teacherId": 1,
    "studentId": 5,
    "studentName": "김민수",
    "studentOs": "IOS",
    "teacherName": "John",
    "status": "BOOKED",
    "startAt": "2026-03-10T09:00:00",
    "endAt": "2026-03-10T09:20:00"
}
```

- **Key**: `lessonId` → 같은 수업의 이벤트(생성, 취소)가 순서대로 처리됨
- **status**: `BOOKED`, `CANCELLED` 등 이벤트 종류를 구분

---

## 6. Consumer 처리 흐름

```
메시지 수신
    │
    ├── status == BOOKED
    │     ├── Student: "[선생님이름] 선생님과 [시작시각] 수업이 예약되었습니다"
    │     └── Teacher: "[학생이름] 학습자와 [시작시각] 수업이 예약되었습니다"
    │
    ├── status == CANCELLED
    │     ├── Student: "[시작시각] 수업이 취소되었습니다"
    │     └── Teacher: "[시작시각] 수업이 취소되었습니다"
    │
    └── Student OS 분기
          ├── IOS → APNs 발송
          └── ANDROID → FCM 발송
```

---

## 7. 실패 처리

### 재시도

- 알림 발송 실패 시 최대 3회 재시도 (1초 간격)
- `@RetryableTopic` 또는 Spring Kafka의 `DefaultErrorHandler` 활용

### Dead Letter Topic (DLT)

- 3회 재시도 후에도 실패하면 `lesson-notification-dlt` 토픽으로 이동
- DLT 메시지는 별도 배치로 수동 재처리하거나 운영팀에 알림

### 멱등성 보장

- Consumer 장애 후 재시작 시 동일 메시지를 다시 처리할 수 있음
- `lessonId` + `status` 조합으로 중복 발송 체크 테이블을 두어 방지

---

## 8. 확장성

현재는 수업 예약(BOOKED) 알림만 설계했지만, 같은 파이프라인으로 확장 가능:

- **수업 취소** → status: CANCELLED
- **수업 리마인더** → 수업 시작 10분 전, 별도 스케줄러가 메시지 발행
- **수업 완료** → status: DONE

---

## 9. 대안 비교

| 방식 | 장점 | 단점 |
|------|------|------|
| **Kafka (현재 선택)** | 순서 보장, 재처리, 수평 확장 | 인프라 운영 비용 |
| SQS | 관리 부담 적음, AWS 통합 | FIFO 큐 처리량 제한, 순서 보장 제약 |
| RabbitMQ | 라우팅 유연 | 대규모 메시지 영속성에서 Kafka보다 불리 |
| Spring Event (in-process) | 추가 인프라 불필요 | 멀티 인스턴스에서 중복 발송, 장애 시 유실 |

---

## 10. Producer 발행 실패 대응 (Outbox 패턴)

`AFTER_COMMIT` 후 Kafka 발행이 실패하면 알림이 유실될 수 있다.
이를 방지하기 위한 대안으로 Outbox 패턴이 있다:

1. Lesson INSERT와 동일 트랜잭션에서 `notification_outbox` 테이블에 메시지 저장
2. 별도 프로세스(Debezium 등)가 outbox 테이블을 polling하여 Kafka로 발행
3. DB 트랜잭션과 메시지 발행의 원자성이 보장됨

현재 규모에서는 `AFTER_COMMIT` + 재시도로 충분하며, 메시지 유실이 치명적인 경우 Outbox 패턴을 도입한다.
