package com.saas.saas_foundry_api.controller;

import org.hibernate.validator.constraints.Range;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.saas.saas_foundry_api.context.RequestContext;
import com.saas.saas_foundry_api.dto.request.ClientDto;
import com.saas.saas_foundry_api.dto.request.UserDto;
import com.saas.saas_foundry_api.dto.response.ClientsDto;
import com.saas.saas_foundry_api.dto.response.UsersDto;
import com.saas.saas_foundry_api.service.domain.ClientService;
import com.saas.saas_foundry_api.utils.TenantUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/organizations/{orgUid}/clients")
@RequiredArgsConstructor
public class ClientController {

  private final ClientService clientService;
  private final RequestContext requestContext;

  @PostMapping
  @Operation(description = "Create a client")
  public ResponseEntity<String> createClient(@PathVariable String orgUid, @RequestBody ClientDto clientDto) {
    TenantUtils.isOrganizationOrAboveAuth(requestContext);
    clientService.createClient(orgUid, clientDto);
    return ResponseEntity.accepted().body("You will get an email when the setup is complete.");
  }

  @Operation(summary = "Get clients", parameters = {
      @Parameter(name = "page", required = true, example = "0"),
      @Parameter(name = "size", required = true, example = "20")
  })
  @GetMapping
  public ResponseEntity<ClientsDto> listClients(@PathVariable String orgUid, @RequestParam @Min(0) int page,
      @RequestParam @Range(min = 1, max = 100) int size) {
    TenantUtils.isOrganizationOrAboveAuth(requestContext);
    return ResponseEntity.ok().body(clientService.listClients(orgUid, page, size));
  }

  @PostMapping("/{clientUid}/users")
  @Operation(description = "Create a user")
  public ResponseEntity<String> createUser(@PathVariable String orgUid, @PathVariable String clientUid,
      @RequestBody UserDto userDto) {
    clientService.createUser(clientUid, userDto);
    TenantUtils.isClientOrAboveAuth(requestContext);
    return ResponseEntity.accepted().body("You will get an email when the setup is complete.");
  }

  @GetMapping("/{clientUid}/users")
  @Operation(summary = "Get users", parameters = {
      @Parameter(name = "page", required = true, example = "0"),
      @Parameter(name = "size", required = true, example = "20")
  })
  public ResponseEntity<UsersDto> listUsers(@PathVariable String orgUid, @PathVariable String clientUid,
      @RequestParam @Min(0) int page,
      @RequestParam @Range(min = 1, max = 100) int size) {
    TenantUtils.isClientOrAboveAuth(requestContext);
    return ResponseEntity.ok().body(clientService.listUsers(clientUid, page, size));
  }
}