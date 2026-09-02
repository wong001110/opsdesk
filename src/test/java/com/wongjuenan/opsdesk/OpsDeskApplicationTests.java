package com.wongjuenan.opsdesk;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;

@SpringBootTest
class OpsDeskApplicationTests {

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void contextStartsAndFlywayAppliesBaselineMigration() {
        String schemaVersion = jdbcClient.sql("""
                SELECT metadata_value
                FROM system_metadata
                WHERE metadata_key = 'schema_version'
                """)
                .query(String.class)
                .single();

        assertThat(schemaVersion).isEqualTo("phase-0");
    }
}
