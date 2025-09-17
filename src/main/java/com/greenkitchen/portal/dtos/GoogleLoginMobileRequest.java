package com.greenkitchen.portal.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GoogleLoginMobileRequest {
  @NotNull(message = "User info is required")
  private Map<String, Object> userInfo;

  // Helper methods to extract user info fields
  public String getId() {
    return userInfo != null ? (String) userInfo.get("id") : null;
  }

  public String getEmail() {
    return userInfo != null ? (String) userInfo.get("email") : null;
  }

  public String getName() {
    return userInfo != null ? (String) userInfo.get("name") : null;
  }

  public String getGivenName() {
    return userInfo != null ? (String) userInfo.get("givenName") : null;
  }

  public String getFamilyName() {
    return userInfo != null ? (String) userInfo.get("familyName") : null;
  }

  public String getPicture() {
    return userInfo != null ? (String) userInfo.get("picture") : null;
  }

  public Boolean getEmailVerified() {
    return userInfo != null ? (Boolean) userInfo.get("emailVerified") : null;
  }

  public String getLocale() {
    return userInfo != null ? (String) userInfo.get("locale") : null;
  }
}