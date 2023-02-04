package com.example.app.githubwebhooklistner.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookPushEvent {
  @JsonProperty(value = "before")
  public String prevCommitSha;
  @JsonProperty(value = "after")
  public String latestCommitSha;

  public Repository repository;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public class Repository {
    public String name;
    public int id;
    public String description;
    public String url;
  }
}
