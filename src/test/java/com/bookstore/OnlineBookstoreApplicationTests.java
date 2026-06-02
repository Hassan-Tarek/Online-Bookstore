package com.bookstore;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class OnlineBookstoreApplicationTests {

	@MockitoBean
	private JavaMailSender mailSender;

	@Test
	void contextLoads() {
	}

}
