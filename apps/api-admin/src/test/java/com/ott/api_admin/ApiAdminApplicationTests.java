package com.ott.api_admin;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Integration context requires DB/Flight; skip during unit-focused runs")
class ApiAdminApplicationTests {

	@Test
	void contextLoads() {
	}

}
