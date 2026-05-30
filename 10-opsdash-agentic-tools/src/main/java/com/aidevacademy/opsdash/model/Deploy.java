package com.aidevacademy.opsdash.model;

/**
 * A deployment event for a microservice.
 * In production: populated from GitHub Actions, Argo CD, or your CI/CD platform.
 */
public record Deploy(
    String service,
    String version,
    String deployedAt,
    String commitHash,
    String deployedBy
) {
    @Override
    public String toString() {
        return String.format(
            "Deploy{service='%s', version='%s', at='%s', commit='%s', by='%s'}",
            service, version, deployedAt, commitHash, deployedBy
        );
    }
}
