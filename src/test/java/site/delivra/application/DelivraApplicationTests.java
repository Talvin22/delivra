package site.delivra.application;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "here.api.key=test-api-key",
        "jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLW9ubHktbXVzdC1iZS1sb25nLWVub3VnaA==",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=",
        "spring.flyway.enabled=false",
        "spring.cloud.consul.enabled=false",
        "spring.cloud.consul.config.enabled=false",
        "spring.cloud.consul.discovery.enabled=false",
        "spring.cloud.consul.discovery.register=false",
        "spring.kafka.bootstrap-servers=localhost:9092",
        "swagger.servers.first=http://localhost:8189",
        "swagger.servers.second=http://localhost:8189",
        "websocket.allowed-origins=http://localhost:3000",
        "here.api.geocoding.base-url=https://geocode.search.hereapi.com/v1/geocode",
        "here.api.routing.base-url=https://router.hereapi.com/v8/routes",
        "here.api.truck.default-gross-weight=12000",
        "here.api.truck.default-height=400",
        "here.api.truck.default-width=250",
        "here.api.truck.default-length=1200",
        "spring.autoconfigure.exclude=" +
                "org.springframework.cloud.consul.ConsulAutoConfiguration," +
                "org.springframework.cloud.consul.serviceregistry.ConsulAutoServiceRegistrationAutoConfiguration," +
                "org.springframework.cloud.consul.discovery.ConsulDiscoveryClientConfiguration," +
                "org.springframework.cloud.consul.config.ConsulConfigAutoConfiguration," +
                "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
class DelivraApplicationTests {

    @Test
    void contextLoads() {
    }

}
