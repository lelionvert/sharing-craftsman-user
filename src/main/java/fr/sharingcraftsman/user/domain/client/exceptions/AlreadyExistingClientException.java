package fr.sharingcraftsman.user.domain.client.exceptions;

public class AlreadyExistingClientException extends ClientException {
  public AlreadyExistingClientException(String message) {
    super(message);
  }
}
