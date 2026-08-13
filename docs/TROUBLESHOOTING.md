# 🐛 Troubleshooting Log
 
Real issues encountered while containerizing and deploying this app — root causes and fixes, kept as documentation of the actual debugging process.
 
---
 
<details>
<summary><strong>🔴 Docker build failed: "no match for platform in manifest"</strong></summary>
<br>
**Symptom**
```
ERROR: failed to build: failed to solve: eclipse-temurin:17-jdk-alpine:
failed to resolve source metadata: no match for platform in manifest: not found
```
 
**Root cause**
The Alpine-based `eclipse-temurin` image tags do not reliably publish builds for `arm64` (Apple Silicon / M-series Mac chips), so Docker could not find a compatible image for the local build machine.
 
**Fix**
Switched the Dockerfile's base images from `17-jdk-alpine` / `17-jre-alpine` to `17-jdk-jammy` / `17-jre-jammy` (Ubuntu-based). Jammy tags are built for both `amd64` and `arm64`, fixing local builds on Apple Silicon while remaining fully compatible with Cloud Run's `amd64` runtime.
 
</details>
---
 
<details>
<summary><strong>🔴 Maven build failed: "Resolution of annotationProcessorPath dependencies failed"</strong></summary>
<br>
**Symptom**
```
[ERROR] Resolution of annotationProcessorPath dependencies failed:
version can neither be null, empty nor blank
```
 
**Root cause**
`pom.xml`'s `maven-compiler-plugin` configuration referenced Lombok as an annotation processor without an explicit `<version>`. IntelliJ's bundled Maven inferred the version automatically; the plain Maven Wrapper used inside the Docker build environment requires it stated explicitly.
 
**Fix**
Added `<version>1.18.30</version>` to the `annotationProcessorPaths` block in `maven-compiler-plugin`'s configuration, matching the Lombok version declared elsewhere in the POM.
 
</details>
---
 
<details>
<summary><strong>🔴 Missing <code>.mvn/wrapper/maven-wrapper.properties</code></strong></summary>
<br>
**Symptom**
Docker build failed at the `./mvnw` invocation with no clear error, since the wrapper script had no version metadata to read.
 
**Root cause**
The original project was run locally via IntelliJ's bundled Maven, so `.mvn/wrapper/maven-wrapper.properties` was never generated or committed to the repository, even though `mvnw` and `mvnw.cmd` were present.
 
**Fix**
Manually created `.mvn/wrapper/maven-wrapper.properties` with the wrapper and distribution versions matching the `mvnw` script.
 
</details>
---
 
<details>
<summary><strong>🔴 500 Internal Server Error when clicking "Edit" on a subscription</strong></summary>
<br>
**Symptom**
Clicking Edit on any subscription threw an unhandled 500 error, followed by a secondary error since no custom error page was configured.
 
**Root cause**
In `SubscriptionShepardController.java`, the `/retrieve/{id}` endpoint returned `"/subscription"` (leading slash) instead of `"subscription"`. Thymeleaf's template resolver could not resolve the leading-slash path to the actual `subscription.html` template. Every other controller method returned template names without a leading slash — this one was inconsistent.
 
**Fix**
```diff
- return "/subscription";
+ return "subscription";
```
 
</details>
---
 
<details>
<summary><strong>🔴 GitHub Actions deploy failed: "Permission 'iam.serviceaccounts.actAs' denied"</strong></summary>
<br>
**Symptom**
```
ERROR: (gcloud.run.deploy) PERMISSION_DENIED: Permission 'iam.serviceaccounts.actAs'
denied on service account subscription-shepard-runtime@subscription-shepard.iam.gserviceaccount.com
(or it may not exist). This command is authenticated as
github-deployer@subscription-shepard.iam.gserviceaccount.com
```
 
**Root cause**
The GitHub Actions workflow's deploy step referenced a service account named `subscription-shepard-runtime`, which did not actually exist. The Cloud Run service's real runtime service account — the one manually created and selected while deploying through the Cloud Console's click-through flow — was named `subscription-shepard-runner`. Since `github-deployer` had no grant on an account that didn't exist, the deploy failed at the permission check.
 
**Fix**
1. Granted the `github-deployer` service account the **Service Account User** role directly on the correct, existing `subscription-shepard-runner` service account (IAM & Admin → Service Accounts → `subscription-shepard-runner` → Permissions → Grant Access).
2. Corrected the `--service-account` value in `deploy.yml` from `subscription-shepard-runtime` to `subscription-shepard-runner`, matching the account that actually exists.
Re-ran the workflow, which then deployed successfully.
 
</details>
---
 
<details>
<summary><strong>🟡 Local H2 in-memory data persisting across "restarts" in IntelliJ</strong></summary>
<br>
**Symptom**
Registering a username that should have been unique failed with "Username already taken," even on what appeared to be a fresh app run.
 
**Root cause**
Spring Boot DevTools performs a "soft restart" when re-running the app in IntelliJ — it reloads application code inside the same still-running JVM process rather than starting a new one. Since the in-memory H2 database (`DB_CLOSE_DELAY=-1`) persists for the life of the JVM process, data survived across these soft restarts.
 
**Resolution**
Not a bug — expected DevTools behavior. Confirmed by fully stopping the process (not just re-running) and observing the database correctly reset, which also confirmed the in-memory database resets on genuine Cloud Run cold starts, as intended.
 
</details>
