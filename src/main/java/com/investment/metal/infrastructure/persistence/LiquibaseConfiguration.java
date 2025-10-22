package com.investment.metal.infrastructure.persistence;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LiquibaseProperties.class)
@Slf4j
public class LiquibaseConfiguration {

  private final DataSource dataSource;
  private final LiquibaseProperties properties;

  public LiquibaseConfiguration(DataSource dataSource, LiquibaseProperties properties) {
    this.dataSource = dataSource;
    this.properties = properties;
  }

  @Bean(name = "liquibaseChangelog")
  public SpringLiquibase liquibase() {
    SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setDataSource(dataSource);
    liquibase.setChangeLog(properties.getChangeLog());
    liquibase.setDefaultSchema(properties.getDefaultSchema());
    liquibase.setLiquibaseSchema(properties.getLiquibaseSchema());
    liquibase.setLiquibaseTablespace(properties.getLiquibaseTablespace());
    //liquibase.setContexts(properties.getContexts());
    liquibase.setDropFirst(properties.isDropFirst());
    liquibase.setShouldRun(shouldRunLiquibase());
    //liquibase.setLabels(properties.getLabels());
    liquibase.setChangeLogParameters(properties.getParameters());
    liquibase.setDatabaseChangeLogTable(properties.getDatabaseChangeLogTable());
    liquibase.setDatabaseChangeLogLockTable(properties.getDatabaseChangeLogLockTable());
    liquibase.setRollbackFile(properties.getRollbackFile());
    liquibase.setTag(properties.getTag());
    liquibase.setTestRollbackOnUpdate(properties.isTestRollbackOnUpdate());
    return liquibase;
  }

  private boolean shouldRunLiquibase() {
    if (!properties.isEnabled()) {
      log.info("Liquibase execution disabled via configuration");
      return false;
    }

    if (isDatabaseAvailable()) {
      return true;
    }

    log.warn("Database is not available during startup. Liquibase migrations will be skipped.");
    return false;
  }

  private boolean isDatabaseAvailable() {
    try (Connection connection = dataSource.getConnection()) {
      return connection != null && connection.isValid(1);
    } catch (SQLException ex) {
      log.debug("Database availability check failed", ex);
      return false;
    }
  }
}
