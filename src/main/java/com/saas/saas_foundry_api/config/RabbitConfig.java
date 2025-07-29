package com.saas.saas_foundry_api.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.saas.saas_foundry_api.common.QueueNames;

@Configuration
public class RabbitConfig {

  @Bean
  public Queue organizationProvisioningQueue() {
    return new Queue(QueueNames.ORGANIZATION_PROVISIONING_QUEUE, true);
  }

  @Bean
  public Queue clientProvisioningQueue() {
    return new Queue(QueueNames.CLIENT_PROVISIONING_QUEUE, true);
  }

  @Bean
  public Queue userProvisioningQueue() {
    return new Queue(QueueNames.USER_PROVISIONING_QUEUE, true);
  }
}