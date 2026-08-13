# 📘 Deployment Guide
 
How Subscription Shepard is containerized, secured, and deployed to Google Cloud Run.
 
---
 
## 🗺️ Architecture Overview
 
```
GitHub (push to main)
   │
   ▼
GitHub Actions  ──►  Workload Identity Federation (no stored keys)
   │
   ▼
Cloud Build     ──►  Artifact Registry (Docker image)
   │
   ▼
Cloud Run       ──►  containerized Spring Boot app (in-memory H2)
```
 
---
 
## 🗄️ Database Strategy
 
> [!NOTE]
> The app uses H2 in **in-memory mode** rather than file-based storage:
> ```
> jdbc:h2:mem:subscriptiondb;DB_CLOSE_DELAY=-1
> ```
 
Cloud Run containers have an **ephemeral, stateless filesystem**, anything written to disk is lost on restart or when scaling to a new instance. In-memory H2 is a deliberate trade-off for this portfolio deployment: it avoids provisioning a persistent database (no free tier exists for Cloud SQL) while still demonstrating a fully working, containerized full-stack application.
 
> [!WARNING]
> **Trade-off:** data resets on cold start, and concurrent traffic across multiple Cloud Run instances does not share state, each instance holds its own isolated in-memory database. A production version would use Cloud SQL (Postgres/MySQL) instead.
 
---
 
## 🐳 Dockerfile
 
A **multi-stage build**:
 
| Stage | Base Image | Purpose |
|---|---|---|
| **Build** | `eclipse-temurin:17-jdk-jammy` | Compiles the app via the Maven Wrapper |
| **Run** | `eclipse-temurin:17-jre-jammy` | Copies only the built JAR into a minimal runtime image |
 
**Jammy** (Ubuntu-based) tags were chosen over Alpine-based tags for multi-architecture support, see [`TROUBLESHOOTING.md`](TROUBLESHOOTING.md).
 
The app respects Cloud Run's dynamically assigned `$PORT` via:
```properties
server.port=${PORT:8080}
```
 
---
 
## 🔑 CI/CD Security: Workload Identity Federation
 
> [!IMPORTANT]
> Instead of generating and storing a GCP service account **key** (a long-lived credential) as a GitHub secret, this project uses **Workload Identity Federation (WIF)**, Google's current recommended approach for keyless CI/CD.
 
1. A dedicated, **least-privilege** service account (`github-deployer`) is created with only the roles needed to deploy:
   - `roles/run.admin`
   - `roles/artifactregistry.writer`
   - `roles/iam.serviceAccountUser`
2. A Workload Identity Pool and OIDC provider trust GitHub's own token issuer.
3. The trust binding is scoped to **this exact repository** (`Aminm246/subscription-shepard`), no other GitHub repo can impersonate this service account.
4. At deploy time, GitHub Actions exchanges its OIDC token for a short-lived GCP access token. **No key file ever exists** on disk or in GitHub Secrets.
---
 
## 💰 Cost Controls
 
| Control | Purpose |
|---|---|
| `--max-instances=1` | Prevents traffic spikes from spinning up multiple billed instances; avoids multi-instance database inconsistency |
| No `--min-instances` set | Service scales to zero when idle |
| Cloud Run always-free tier | 2M requests/month, 360,000 GiB-seconds, covers expected traffic at **$0/month** |
| GCP Spend Cap (Cloud Run) | Hard limit as a safety net |
| Project-wide budget alert | Email notification at defined spend thresholds |
 
---
 
## 🧪 Local Testing
 
```bash
# Build and run the container locally — identical to how Cloud Run runs it
Step 1: docker build -t subscription-shepard-test .
Step 2: docker run -p 8080:8080 -e PORT=8080 subscription-shepard-test
```
 
Visit `http://localhost:8080`
