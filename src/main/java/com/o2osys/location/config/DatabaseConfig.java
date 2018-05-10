
/**
 *
 */
package com.o2osys.location.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author stunstun(minhyuck.jung@nhnent.com)
 *
 */
public abstract class DatabaseConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    public abstract DataSource dataSource();

    protected void configureDataSource(org.apache.tomcat.jdbc.pool.DataSource dataSource,
            DatabaseProperties databaseProperties) {
        LOGGER.info("configureDataSource = {}", databaseProperties);
        dataSource.setDriverClassName(databaseProperties.getDriverClassName());
        dataSource.setUrl(databaseProperties.getUrl());
        dataSource.setUsername(databaseProperties.getUserName());
        dataSource.setPassword(databaseProperties.getPassword());
        dataSource.setMaxActive(databaseProperties.getMaxActive());
        dataSource.setMaxIdle(databaseProperties.getMaxIdle());
        dataSource.setMaxWait(databaseProperties.getMaxWait());
        dataSource.setMinIdle(databaseProperties.getMinIdle());
        dataSource.setInitialSize(databaseProperties.getInitialSize());
        // dataSource.setTestOnBorrow(true);
        dataSource.setValidationQuery(databaseProperties.getValidationQuery());
        // dataSource.setRemoveAbandoned(true);
        // dataSource.setTimeBetweenEvictionRunsMillis(7200000);
        dataSource.setTestWhileIdle(true);
    }
}

@Configuration
// @EnableTransactionManagement
@EnableConfigurationProperties(MasterDatabaseProperties.class)
class MasterDatabaseConfig extends DatabaseConfig {

    @Autowired
    private MasterDatabaseProperties masterDatabaseProperties;

    @Override
    @Primary
    @Bean(name = "masterDataSource", destroyMethod = "close")
    public DataSource dataSource() {
        org.apache.tomcat.jdbc.pool.DataSource masterDataSource = new org.apache.tomcat.jdbc.pool.DataSource();
        configureDataSource(masterDataSource, masterDatabaseProperties);
        return masterDataSource;
    }

    @Bean(name = "Masterjdbc")
    @Autowired
    public JdbcTemplate masterJdbcTemplate(@Qualifier("masterDataSource") DataSource masterDataSource) {
        return new JdbcTemplate(masterDataSource);
    }

    // @Bean
    // public PlatformTransactionManager
    // transactionManager(@Qualifier("masterDataSource") DataSource
    // masterDataSource) {
    // DataSourceTransactionManager transactionManager = new
    // DataSourceTransactionManager(masterDataSource);
    // transactionManager.setGlobalRollbackOnParticipationFailure(false);
    // return transactionManager;
    // }
}

@Configuration
@EnableConfigurationProperties(SlaveDatabaseProperties.class)
class SlaveDatabaseConfig extends DatabaseConfig {

    @Autowired
    private SlaveDatabaseProperties slaveDatabaseProperties;

    @Override
    @Bean(name = "slaveDataSource", destroyMethod = "close")
    public DataSource dataSource() {
        org.apache.tomcat.jdbc.pool.DataSource slaveDataSource = new org.apache.tomcat.jdbc.pool.DataSource();
        configureDataSource(slaveDataSource, slaveDatabaseProperties);
        return slaveDataSource;
    }

    @Bean(name = "Slavejdbc")
    @Autowired
    public JdbcTemplate slaveJdbcTemplate(@Qualifier("slaveDataSource") DataSource slaveDataSource) {
        return new JdbcTemplate(slaveDataSource);
    }

}
