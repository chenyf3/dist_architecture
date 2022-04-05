package com.xpay.starter.logging.connectionSource;

import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.appender.db.jdbc.AbstractConnectionSource;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 支持连接池的DataSource，目前是使用alibaba的Druid作为DataSource
 * @author chenyf
 */
@Plugin(name = "PoolDataSource", category = Core.CATEGORY_NAME, elementType = "connectionSource", printObject = true)
public final class PoolDataSourceConnectionSource extends AbstractConnectionSource {
    private final DataSource dataSource;

    private PoolDataSourceConnectionSource(final String jdbcUrl, final String username, final String password,
                                          final String driverClassName, Property[] properties) {
        Map<String, String> propertyMap = new HashMap<>();
        propertyMap.put("url", jdbcUrl);
        propertyMap.put("username", username);
        propertyMap.put("password", password);
        propertyMap.put("driver-class-name", driverClassName);
        if(properties != null){
            for(Property property : properties){
                propertyMap.put(property.getName(), property.getValue());
            }
        }

        try {
            this.dataSource = com.alibaba.druid.pool.DruidDataSourceFactory.createDataSource(propertyMap);
        } catch (Exception e) {
            throw new RuntimeException("PoolDataSourceConnectionSource 初始化异常", e);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    @PluginFactory
    public static PoolDataSourceConnectionSource createConnectionSource(@PluginAttribute("jdbcUrl") final String jdbcUrl,
                                                                        @PluginAttribute("username") final String username,
                                                                        @PluginAttribute("password") final String password,
                                                                        @PluginAttribute("driverClass") final String driverClass,
                                                                        @PluginElement("Properties") final Property[] properties) {
        return new PoolDataSourceConnectionSource(jdbcUrl, username, password, driverClass, properties);
    }
}
