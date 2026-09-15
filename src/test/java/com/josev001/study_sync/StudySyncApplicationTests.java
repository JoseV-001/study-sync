package com.josev001.study_sync;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Classe de teste que valida se o contexto da aplicação sobe corretamente.
@SpringBootTest(properties = {
		"clockify.api-key=test",
		"notion.api-key=test",
		"study-sync.sync-on-startup=false",
		"spring.datasource.url=jdbc:h2:mem:study_sync;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.flyway.locations=classpath:db/migration"
})
class StudySyncApplicationTests {

	@Test
	void contextLoads() {
	}

}
