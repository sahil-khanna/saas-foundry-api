package com.vonage.saas_foundry_api.dto.response;

import java.util.List;
import com.vonage.saas_foundry_api.dto.request.UserDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(name = "Users")
public class UsersDto {
  private List<UserDto> users;
  private long total;
}
