package hanyang.RentalManagementSystem;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
// 테스트(컨텍스트 로드) 시 MQTT 자동 구동을 꺼서 라이브 브로커 연결/데이터 적재를 방지한다.
@TestPropertySource(properties = "drvalue.auto-start=false")
class RentalManagementSystemApplicationTests {

	@Test
	void contextLoads() {
	}

}
