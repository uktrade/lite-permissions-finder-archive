package models.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class RegisterLicence {
  private @Getter @Setter String sessionId;
  private @Getter @Setter String requestId;
  private @Getter @Setter String registrationReference;
  private @Getter @Setter String userId;
  private @Getter @Setter String customerId;
  private @Getter @Setter String siteId;
  private @Getter @Setter String ogelId;
  private @Getter @Setter String callbackUrl;
  private @Getter @Setter String userEmailAddress;
  private @Getter @Setter String userFullName;
}
