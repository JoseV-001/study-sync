package com.josev001.study_sync;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.datasource.url=jdbc:sqlite:file:study_sync_test?mode=memory&cache=shared",
                "spring.datasource.driver-class-name=org.sqlite.JDBC",
                "spring.jpa.hibernate.ddl-auto=none",
                "spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect",
                "spring.flyway.locations=classpath:db/migration/sqlite",
                "study-sync.sync-on-startup=false"
        }
)
class SqliteApplicationTests {

    @Test
    void contextLoadsWithThePortableSqliteDatabase() {
    }
}
