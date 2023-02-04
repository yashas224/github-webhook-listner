package com.example.app.githubwebhooklistner.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "github")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GitHubConfig {
  public String userName;
  public String personalToken;
  public String webhookSecret;
  public String apiHost;
}
