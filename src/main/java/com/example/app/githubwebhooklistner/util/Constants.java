package com.example.app.githubwebhooklistner.util;

public interface Constants {
  String GIT_HUB_USER_END_POINT = "/user";
  String GIT_HUB_COMMIT_END_POINT = "/repos/{owner}/{repo}/commits/{ref}";
  String GIT_HUB_COMMMIT_BRANCH_HEAD_END_POINT = "/repos/{owner}/{repo}/commits/{commit_sha}/branches-where-head";
  String GIT_HUB_DOWNLOAD_END_POINT = "/repos/{owner}/{repo}/contents/{path}";
  String GIT_HUB_NO_COMMIT_REF = "0000000000000000000000000000000000000000";
  String UPLOAD_BRANCH = "upload";
}
