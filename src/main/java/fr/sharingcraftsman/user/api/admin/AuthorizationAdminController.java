package fr.sharingcraftsman.user.api.admin;

import fr.sharingcraftsman.user.api.models.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/roles")
@Api(description = "Endpoints to for admin")
public class AuthorizationAdminController {
  private AdminService adminService;

  @Autowired
  public AuthorizationAdminController(AdminService adminService) {
    this.adminService = adminService;
  }

  @ApiOperation(value = "Endpoint to get groups", response = GroupDTO.class)
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = ""),
          @ApiResponse(code = 401, message = "Unauthorized")
  })
  @RequestMapping(method = RequestMethod.GET, value = "/groups")
  public ResponseEntity getGroups(@RequestHeader("client") String client,
                                @RequestHeader("secret") String secret,
                                @RequestHeader("username") String username,
                                @RequestHeader("access-token") String accessToken) {
    ClientDTO clientDTO = new ClientDTO(client, secret);
    TokenDTO tokenDTO = new TokenDTO(username, accessToken);
    return adminService.getGroups(clientDTO, tokenDTO);
  }

  @ApiOperation(value = "Endpoint to get groups", response = GroupDTO.class)
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = ""),
          @ApiResponse(code = 401, message = "Unauthorized")
  })
  @RequestMapping(method = RequestMethod.POST, value = "/groups")
  public ResponseEntity removeGroup(@RequestHeader("client") String client,
                                    @RequestHeader("secret") String secret,
                                    @RequestHeader("username") String username,
                                    @RequestHeader("access-token") String accessToken,
                                    @RequestBody GroupDTO groupDTO) {
    ClientDTO clientDTO = new ClientDTO(client, secret);
    TokenDTO tokenDTO = new TokenDTO(username, accessToken);
    return adminService.createNewGroupWithRoles(clientDTO, tokenDTO, groupDTO);
  }
}
