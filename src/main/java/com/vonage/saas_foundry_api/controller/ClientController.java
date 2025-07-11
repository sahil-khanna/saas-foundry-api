package com.vonage.saas_foundry_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.vonage.saas_foundry_api.dto.request.ClientDto;
import com.vonage.saas_foundry_api.service.domain.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/organizations/{orgUid}/clients")
@AllArgsConstructor
public class ClientController {

  private final ClientService clientService;

  @PostMapping
  @Operation(description = "Create a client")
  public ResponseEntity<String> create(@PathVariable String orgUid, @RequestBody ClientDto clientDto) {
    clientService.create(orgUid, clientDto);
    return ResponseEntity.accepted().body("You will get an email when the setup is complete.");
  }
}