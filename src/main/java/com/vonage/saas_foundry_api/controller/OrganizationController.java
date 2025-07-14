package com.vonage.saas_foundry_api.controller;

import org.hibernate.validator.constraints.Range;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.vonage.saas_foundry_api.dto.request.OrganizationDto;
import com.vonage.saas_foundry_api.dto.response.OrganizationsDto;
import com.vonage.saas_foundry_api.service.domain.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Min;
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

  @Operation(summary = "Get organizations.", parameters = {
      @Parameter(name = "page", required = true, example = "0"),
      @Parameter(name = "size", required = true, example = "20")
  })
  @GetMapping
  public ResponseEntity<OrganizationsDto> list(@RequestParam @Min(0) int page, @RequestParam @Range(min = 1, max = 100) int size) {
    return ResponseEntity.ok().body(organizationService.list(page, size));
  }
}