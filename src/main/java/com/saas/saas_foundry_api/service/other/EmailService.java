package com.saas.saas_foundry_api.service.other;

import org.springframework.stereotype.Service;

import com.saas.saas_foundry_api.dto.request.SendEmailDto;

@Service
public class EmailService {
  
  public boolean sendEmail(SendEmailDto sendEmailDto) {
    // TODO: Send email;
    return true;
  }
}
