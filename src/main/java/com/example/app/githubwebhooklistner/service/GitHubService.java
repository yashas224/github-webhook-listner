package com.example.app.githubwebhooklistner.service;

import com.example.app.githubwebhooklistner.config.GitHubConfig;
import com.example.app.githubwebhooklistner.models.GitHubCommitResponse;
import com.example.app.githubwebhooklistner.models.GitHubDownloadResponse;
import com.example.app.githubwebhooklistner.models.GitHubHeadBranchResponse;
import com.example.app.githubwebhooklistner.util.AppUtil;
import com.example.app.githubwebhooklistner.util.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;

@Service
public class GitHubService {

  Logger LOGGER = LoggerFactory.getLogger(GitHubService.class.getName());
  WebClient webClient;

  @Autowired
  GitHubConfig gitHubConfig;
  @Autowired
  AppUtil appUtil;

  @PostConstruct
  public void initGitHubService() {
    webClient = WebClient.builder()
       .baseUrl(gitHubConfig.getApiHost())
       .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer ".concat(gitHubConfig.getPersonalToken()))
       .filters(exchangeFilterFunctions -> {
         exchangeFilterFunctions.add(appUtil.logWebClientRequest());
         exchangeFilterFunctions.add(appUtil.logWebClientResponse());
       })
       .build();
    verifyUserDetails();
  }

  public void verifyUserDetails() {
    try {
      ResponseEntity<String> response = webClient.get()
         .uri(Constants.GIT_HUB_USER_END_POINT)
         .retrieve()
         .onStatus((httpStatus -> httpStatus.isError()), clientResponse -> {
           LOGGER.error("Status Code {} ", clientResponse.rawStatusCode());
           ResponseEntity<String> errorResponse = clientResponse.toEntity(String.class).block();
           LOGGER.error("Response Body {}", errorResponse.getBody());
           throw new RuntimeException();
         })
         .toEntity(String.class)
         .block();
      LOGGER.info("Response {} {} ", Constants.GIT_HUB_USER_END_POINT, response.getBody());
    } catch(Exception e) {
      LOGGER.error("error in verifyUserDetails Wrong User Credential ");
    }
  }

  public GitHubCommitResponse getGitHubCommitInfo(String repoName, String commitSha) {
    GitHubCommitResponse gitHubCommitResponse = null;
    try {
      gitHubCommitResponse = webClient.get()
         .uri(uriBuilder -> uriBuilder.path(Constants.GIT_HUB_COMMIT_END_POINT).build(gitHubConfig.getUserName(), repoName, commitSha))
         .retrieve()
         .bodyToMono(GitHubCommitResponse.class)
         .block();
    } catch(Exception e) {
      LOGGER.error("error in getGitHubCommitInfo  ");
    }
    return gitHubCommitResponse;
  }

  public GitHubHeadBranchResponse getBranchInfoForTheLatestCommit(String repoName, String commitSha) {
    GitHubHeadBranchResponse gitHubHeadBranchResponse = null;
    try {
      ResponseEntity<String> response = webClient.get()
         .uri(uriBuilder -> uriBuilder.path(Constants.GIT_HUB_COMMMIT_BRANCH_HEAD_END_POINT).build(gitHubConfig.getUserName(), repoName, commitSha))
         .retrieve()
         .toEntity(String.class)
         .block();

      String responseStr = response.getBody();
      responseStr = "{\"gitHubBranchInfo\":".concat(responseStr).concat("}");
      gitHubHeadBranchResponse = new ObjectMapper().readValue(responseStr, GitHubHeadBranchResponse.class);
    } catch(Exception e) {
      LOGGER.error("error in getBranchInfoForTheLatestCommit  ");
    }
    return gitHubHeadBranchResponse;
  }

  public GitHubDownloadResponse downloadFromGitHubPathAndBranch(String repoName, String path, String branch) {
    GitHubDownloadResponse gitHibDownloadResponse = null;
    try {
      gitHibDownloadResponse = webClient.get()
         .uri(uriBuilder -> uriBuilder.path(Constants.GIT_HUB_DOWNLOAD_END_POINT).queryParam("ref", branch).build(gitHubConfig.getUserName(), repoName, path))
         .retrieve()
         .bodyToMono(GitHubDownloadResponse.class)
         .block();
    } catch(Exception e) {
      LOGGER.error("error in downloadFromGitHubPathAndBranch  ");
    }
    return gitHibDownloadResponse;
  }
}


