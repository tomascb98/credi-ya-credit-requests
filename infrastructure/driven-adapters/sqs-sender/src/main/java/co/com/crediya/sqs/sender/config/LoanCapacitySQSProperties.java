package co.com.crediya.sqs.sender.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "adapter.sqs.loan-capacity")
public class LoanCapacitySQSProperties {
    private String region;
    private String queueUrl;
}
