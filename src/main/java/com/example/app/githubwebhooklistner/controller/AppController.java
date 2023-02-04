package com.example.app.githubwebhooklistner.controller;

import com.example.app.githubwebhooklistner.models.GitHubCommitResponse;
import com.example.app.githubwebhooklistner.models.GitHubDownloadResponse;
import com.example.app.githubwebhooklistner.models.GitHubHeadBranchResponse;
import com.example.app.githubwebhooklistner.models.WebhookPushEvent;
import com.example.app.githubwebhooklistner.service.GitHubService;
import com.example.app.githubwebhooklistner.util.AppUtil;
import com.example.app.githubwebhooklistner.util.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app")
public class AppController {

  Logger LOGGER = LoggerFactory.getLogger(AppController.class.getName());

  @Autowired
  AppUtil util;
  @Autowired
  GitHubService gitHubService;

  @GetMapping("/hello")
  public String healthEndPoint() {
    return "Running github-webhook-listner Application successfully";
  }

  @SneakyThrows
  @PostMapping("/payload")
  public ResponseEntity<String> gitHibWebHook(@RequestBody String payload, @RequestHeader(name = "x-hub-signature-256") String secretToken) {
    LOGGER.info("Header x-hub-signature-256 :{}", secretToken);

    if(!util.isValidWebHookRequest(payload, secretToken)) {
      LOGGER.error("Secret Token Miss match !!!!! ");
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not a Secure request from WebHook !!!");
    }

    LOGGER.info("Secret Token Matches !!!!! ");
    LOGGER.info("Received request from GitHub Webhook {}", payload);

    ObjectMapper objectMapper = new ObjectMapper();
    WebhookPushEvent event = objectMapper.readValue(payload, WebhookPushEvent.class);
    LOGGER.info("Payload Constructed {}", event);

    if(isEventIsFileModify(event)) {
      LOGGER.info("Files added or modified");

      // get commit ID
      GitHubCommitResponse gitHubCommitResponse = gitHubService.getGitHubCommitInfo(event.getRepository().getName(), event.getLatestCommitSha());
      GitHubHeadBranchResponse gitHubHeadBranchResponse = gitHubCommitResponse != null && gitHubCommitResponse.getFiles().size() > 0 && isNotRemoveOperation(gitHubCommitResponse) ?
         gitHubService.getBranchInfoForTheLatestCommit(event.getRepository().getName(), event.getLatestCommitSha()) : null;

      if(gitHubHeadBranchResponse != null && gitHubHeadBranchResponse.getGitHubBranchInfo().size() > 0 && isUploadBranch(gitHubHeadBranchResponse)) {
        LOGGER.info("invoking download API");
        GitHubDownloadResponse downloadResponse = gitHubService.downloadFromGitHubPathAndBranch(event.getRepository().getName(),
           gitHubCommitResponse.getFiles().get(0).getFilename(), gitHubHeadBranchResponse.getGitHubBranchInfo().get(0).getName());
        LOGGER.info("download end point Response {}", downloadResponse);
      }
    } else {
      LOGGER.info("Branch added or deleted");
    }

    return ResponseEntity.ok("WebHook Request received by github-webhook-listner application ".concat(event.toString()));
  }

  private boolean isUploadBranch(GitHubHeadBranchResponse gitHubHeadBranchResponse) {
    boolean isUploadBranch = gitHubHeadBranchResponse.getGitHubBranchInfo().get(0).getName().equals(Constants.UPLOAD_BRANCH);
    LOGGER.info(isUploadBranch ? "Pushed to Upload Branch " : "Pushed to non Upload Branch");
    return isUploadBranch;
  }

  private boolean isNotRemoveOperation(GitHubCommitResponse gitHubCommitResponse) {
    if(gitHubCommitResponse.getFiles().get(0).getStatus().equals("removed")) {
      LOGGER.info("remove Operation !!!!");
    }
    return !gitHubCommitResponse.getFiles().get(0).getStatus().equals("removed");
  }

  private boolean isEventIsFileModify(WebhookPushEvent event) {
    return !event.latestCommitSha.equals(Constants.GIT_HUB_NO_COMMIT_REF) && !event.prevCommitSha.equals(Constants.GIT_HUB_NO_COMMIT_REF);
  }
}
