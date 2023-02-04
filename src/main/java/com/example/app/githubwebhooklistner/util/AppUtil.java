package com.example.app.githubwebhooklistner.util;

import com.example.app.githubwebhooklistner.config.GitHubConfig;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Component
public class AppUtil {
  Logger LOGGER = LoggerFactory.getLogger(AppUtil.class.getName());

  @Autowired
  Environment environment;

  @Autowired
  GitHubConfig gitHubConfig;

  @SneakyThrows
  public boolean isValidWebHookRequest(String message, String requestHeader) {
    if(StringUtils.hasText(requestHeader)) {
      String computedHash = "sha256=".concat(HMAC_MD5_encode(gitHubConfig.getWebhookSecret(), message));
      LOGGER.info("Calculated hash  :{}", computedHash);
      return requestHeader.equals(computedHash);
    }
    return false;
  }

  public String HMAC_MD5_encode(String key, String message) throws Exception {
    SecretKeySpec keySpec = new SecretKeySpec(
       key.getBytes(),
       "HmacSHA256");
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(keySpec);
    byte[] rawHmac = mac.doFinal(message.getBytes());
    return Hex.encodeHexString(rawHmac);
  }

  public ExchangeFilterFunction logWebClientRequest() {
    return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
      StringBuilder sb = new StringBuilder("Request: \n");
      sb.append(clientRequest.method());
      sb.append(clientRequest.url());
      //append clientRequest method and url
      clientRequest
         .headers()
         .forEach((name, values) -> values.forEach(value -> sb.append(name.concat(" - ").concat(value))));
      LOGGER.info(sb.toString());
      return Mono.just(clientRequest);
    });
  }

  public ExchangeFilterFunction logWebClientResponse() {
    return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
      StringBuilder sb = new StringBuilder("Response: \n");
      sb.append(clientResponse.statusCode());
      clientResponse
         .headers().asHttpHeaders()
         .forEach((name, values) -> values.forEach(value -> sb.append(name.concat(" - ").concat(value))));
      LOGGER.info(sb.toString());
      return Mono.just(clientResponse);
    });
  }
}
