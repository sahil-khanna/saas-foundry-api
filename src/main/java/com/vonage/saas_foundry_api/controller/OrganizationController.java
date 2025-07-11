package com.vonage.saas_foundry_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.vonage.saas_foundry_api.dto.request.OrganizationDto;
import com.vonage.saas_foundry_api.service.domain.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/organizations")
@AllArgsConstructor
public class OrganizationController {

  private final OrganizationService organizationService;

  @PostMapping
  @Operation(description = "Create an organization")
  public ResponseEntity<String> create(@RequestBody OrganizationDto organizationDto) {
    organizationService.create(organizationDto);
    return ResponseEntity.accepted().body("You will get an email when the setup is complete.");
  }
}