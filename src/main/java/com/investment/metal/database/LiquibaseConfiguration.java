package com.investment.metal.database;

import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LiquibaseProperties.class)
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
    liquibase.setContexts(properties.getContexts());
    liquibase.setDropFirst(properties.isDropFirst());
    liquibase.setShouldRun(properties.isEnabled());
    liquibase.setLabels(properties.getLabels());
    liquibase.setChangeLogParameters(properties.getParameters());
    liquibase.setDatabaseChangeLogTable(properties.getDatabaseChangeLogTable());
    liquibase.setDatabaseChangeLogLockTable(properties.getDatabaseChangeLogLockTable());
    liquibase.setRollbackFile(properties.getRollbackFile());
    liquibase.setTag(properties.getTag());
    liquibase.setTestRollbackOnUpdate(properties.isTestRollbackOnUpdate());
    return liquibase;
  }
}
