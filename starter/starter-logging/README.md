## 一、本模块用途  
1. 从配置中心读取log4j2配置文件，可以实现配置共享，不用在每个应用的 resources 文件夹下都建一个log4j2.xml配置文件  
2. 可实现日志的热更新，在配置中心修改日志配置后可以同步到各应用，令配置立即生效，不用重启所有应用  
3. 自定义PoolDataSourceConnectionSource，让 JdbcAppender 能够使用 DruidDataSource，还可以灵活配置最大连接数/初始连接数等参数  
4. 可以在 log4j2.xml 中配置 SMTP/JDBC/HTTP/KAFKA 等等 Appender，用以实现日志更多的用途，比如：邮件预警、特殊日志持久化到数据库、容器部署时日志直接送到ELK 等  

## 二、配置中心  
1. 使用 nacos 作为配置中心  

## 三、可选配置参数  
1. logging.config-name 可在 bootstrap.properties 中指定 log4j2 的配置文件名，默认为 log4j2.xml  
2. logging.log-home 可在 bootstrap.properties 中指定本地日志文件的所在目录，默认为 ./logs  
3. logging.log-name 可在 bootstrap.properties 中指定本地日志文件的名称，默认等于 spring.application.name 指定的值  

## 四、系统参数  
1. 启动的时候会往系统参数中(通过System.setProperty())设置 logHome、logName 两个参数，分别代表日志的目录和日志文件名，
可在log4j2.xml配置文件中使用 ${sys:logHome} 和 ${sys:logName} 来获取到  