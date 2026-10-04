package com.yran304.incidentplatform;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

// Imported by every @SpringBootTest class. Spring's test context caching reuses an
// already-started application context for any test class with an identical configuration
// (same annotations, same imports). Since every test class here imports this exact class,
// they all share one cached context, and therefore one Postgres container, for the whole
// test run instead of each class starting its own.
@TestConfiguration(proxyBeanMethods = false) // 告诉 Spring"这是一个只在测试里用的配置类，生产代码永远不会加载它"。跟普通 @Configuration 的区别就是这个"仅测试可见"的语义。
public class TestcontainersConfiguration {

    @Bean // 定义了一个 Bean：一个 PostgreSQLContainer 对象，具体就是一个"17-alpine" 版本的 Postgres 容器实例（还没启动，只是定义了"要用这个镜像"）。
    @ServiceConnection // 这是关键的 Spring Boot 魔法注解。它告诉 Spring Boot："这个 Bean 是一个数据库容器，请你自动把应用的数据源配置（spring.datasource.url/username/password）指向这个容器实际跑起来之后的地址"。没有这个注解的话，你还得自己手写一堆 @DynamicPropertySource 代码去把容器的随机端口/账号密码塞进 Spring 的配置里——@ServiceConnection 帮你把这一整套接线工作自动做了。
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));
    }
}
