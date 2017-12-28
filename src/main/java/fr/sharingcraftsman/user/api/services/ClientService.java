package fr.sharingcraftsman.user.api.services;

import fr.sharingcraftsman.user.api.models.ClientRegistration;
import fr.sharingcraftsman.user.api.pivots.ClientPivot;
import fr.sharingcraftsman.user.domain.client.ClientAdministrator;
import fr.sharingcraftsman.user.domain.client.ClientException;
import fr.sharingcraftsman.user.domain.client.ClientStock;
import fr.sharingcraftsman.user.domain.ports.client.ClientManager;
import fr.sharingcraftsman.user.domain.utils.SimpleSecretGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ClientService {
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private ClientManager clientManager;

  @Autowired
  public ClientService(ClientStock clientStock) {
    clientManager = new ClientAdministrator(clientStock, new SimpleSecretGenerator());
  }

  public ResponseEntity register(ClientRegistration clientRegistration) {
    try {
      log.info("Registering new client: " + clientRegistration.getName());
      clientManager.createNewClient(ClientPivot.fromApiToDomain(clientRegistration));
    } catch (ClientException e) {
      log.warn("Client already exists: " + clientRegistration.getName());
      return ResponseEntity
              .badRequest()
              .body(e.getMessage());
    }
    return ResponseEntity.ok().build();
  }
}
