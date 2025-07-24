package com.saas.saas_foundry_api.dto.response;

import java.util.List;

import com.saas.saas_foundry_api.dto.request.ClientDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(name = "Clients")
public class ClientsDto {
  private List<ClientDto> clients;
  private long total;
}
