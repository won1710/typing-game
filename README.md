# Multi-App Spring Boot Server

## 개요
이 프로젝트는 Java 기반 Spring Boot를 사용하여 여러 애플리케이션을 하나의 WAS 환경에서 서비스할 수 있도록 설계된 멀티 모듈 구조입니다.

## 모듈 구성
- `portal` - 웹 루트 메인 페이지로 여러 애플리케이션 목록을 보여줍니다.
- `slack-scheduler` - 매일 오전 7시 30분에 Slack API를 호출하여 메시지를 보내는 샘플 애플리케이션입니다.
- `typing-game` - 닉네임 입력 후 타자 게임을 제공하는 샘플 애플리케이션입니다.

## 디자인 포인트
- 각 애플리케이션은 독립적인 WAR로 패키징되어 WAS에 각각 배포할 수 있습니다.
- `portal`은 루트 애플리케이션으로 작동하며, 다른 앱의 컨텍스트 경로로 이동할 수 있는 링크를 제공합니다.
- 모듈별로 `server.servlet.context-path`를 설정하여 컨텍스트 경로를 분리했습니다.

## 실행 방법
1. 전체 빌드
   ```bash
   mvn clean package
   ```
2. 각 모듈을 WAS에 배포
   - `portal/target/portal.war`
   - `slack-scheduler/target/slack-scheduler.war`
   - `typing-game/target/typing-game.war`

## 참고
- `slack-scheduler`에서 Slack Webhook URL은 `slack-scheduler/src/main/resources/application.properties`에 설정해야 합니다.
- 실제 서비스 환경에서는 슬랙 Webhook을 `Vault`나 `환경 변수`로 관리하는 것이 안전합니다.
