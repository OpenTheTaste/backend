package com.ott.transcoder.queue.rabbit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.SetInstanceProtectionRequest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Component
@ConditionalOnProperty(name = "transcoder.worker.scale-in-protection.enabled", havingValue = "true")
public class AsgScaleInProtectionManager {

    private static final String IMDS_ENDPOINT = "http://169.254.169.254";

    private final AutoScalingClient autoScalingClient = AutoScalingClient.create();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    @Value("${transcoder.worker.scale-in-protection.asg-name:}")
    private String asgName;

    @Value("${transcoder.worker.scale-in-protection.instance-id:}")
    private String configuredInstanceId;

    private volatile String cachedInstanceId;

    public void markBusy() {
        setProtection(true);
    }

    public void markIdle() {
        setProtection(false);
    }

    private void setProtection(boolean enabled) {
        String instanceId = resolveInstanceId();
        if (instanceId == null || instanceId.isBlank()) {
            log.warn("skip ASG instance protection update: instance-id is unavailable (enabled={})", enabled);
            return;
        }
        if (asgName == null || asgName.isBlank()) {
            log.warn("skip ASG instance protection update: asg-name is empty (enabled={}, instanceId={})",
                    enabled, instanceId);
            return;
        }

        try {
            autoScalingClient.setInstanceProtection(
                    SetInstanceProtectionRequest.builder()
                            .autoScalingGroupName(asgName)
                            .instanceIds(instanceId)
                            .protectedFromScaleIn(enabled)
                            .build()
            );
            log.info("ASG scale-in protection updated: instanceId={}, asgName={}, protectedFromScaleIn={}",
                    instanceId, asgName, enabled);
        } catch (Exception e) {
            log.error("failed to update ASG scale-in protection: instanceId={}, asgName={}, protectedFromScaleIn={}",
                    instanceId, asgName, enabled, e);
        }
    }

    private String resolveInstanceId() {
        if (configuredInstanceId != null && !configuredInstanceId.isBlank()) {
            return configuredInstanceId;
        }
        if (cachedInstanceId != null && !cachedInstanceId.isBlank()) {
            return cachedInstanceId;
        }

        synchronized (this) {
            if (cachedInstanceId != null && !cachedInstanceId.isBlank()) {
                return cachedInstanceId;
            }
            cachedInstanceId = fetchInstanceIdFromImds();
            return cachedInstanceId;
        }
    }

    private String fetchInstanceIdFromImds() {
        try {
            String token = fetchImdsV2Token();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(IMDS_ENDPOINT + "/latest/meta-data/instance-id"))
                    .timeout(Duration.ofSeconds(2))
                    .header("X-aws-ec2-metadata-token", token)
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body().trim();
            }
            log.warn("failed to fetch instance-id from IMDS: status={}", response.statusCode());
        } catch (Exception e) {
            log.warn("failed to fetch instance-id from IMDS", e);
        }
        return null;
    }

    private String fetchImdsV2Token() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(IMDS_ENDPOINT + "/latest/api/token"))
                .timeout(Duration.ofSeconds(2))
                .header("X-aws-ec2-metadata-token-ttl-seconds", "21600")
                .method("PUT", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("IMDSv2 token request failed with status " + response.statusCode());
        }
        return response.body().trim();
    }
}
