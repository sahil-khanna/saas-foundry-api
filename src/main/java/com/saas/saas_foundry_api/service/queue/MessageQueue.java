package com.saas.saas_foundry_api.service.queue;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class MessageQueue {

  private final AmqpTemplate amqpTemplate;

  public void sendMessage(String queueName, QueueEvent event) {
    amqpTemplate.convertAndSend(queueName, event.toString());
  }
}