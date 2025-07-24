package com.saas.saas_foundry_api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class SendEmailDto {
  
  private String recipient;
  private String body;
}
