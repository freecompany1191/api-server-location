/**
 *
 */
package com.o2osys.location.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author stunstun(minhyuck.jung@nhnent.com)
 *
 */
@ConfigurationProperties(prefix = MasterDatabaseProperties.PREFIX)
public class MasterDatabaseProperties implements DatabaseProperties {

    public static final String PREFIX = "spring.datasource.master";

    private String driverClassName;

    private String url;

    private String userName;

    private String password;

    private int initialSize;

    private int maxActive;

    private int maxIdle;

    private int minIdle;

    private int maxWait;

    private String validationQuery;

    @Override
    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    @Override
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public int getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(int initialSize) {
        this.initialSize = initialSize;
    }

    @Override
    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    @Override
    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    @Override
    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    @Override
    public int getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(int maxWait) {
        this.maxWait = maxWait;
    }

    @Override
    public String getValidationQuery() {
        return validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    @Override
    public String toString() {
        return "MasterDatabaseProperties[" + this.driverClassName + "]";
    }
}