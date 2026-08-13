# 📘 Deployment Guide
 
How Subscription Shepard is containerized, secured, and deployed to Google Cloud Run.
 
---
 
## 🗺️ Deployment Approach: Manual First, Then Automated
 
Infrastructure was set up in two deliberate phases:
 
1. **Manual, console driven deploy** the initial Cloud Run service, its runtime service account, and the connected repository were all configured by hand through the Google Cloud Console's UI, to build a real working understanding of each moving piece (IAM roles, service identities, container configuration, networking) rather than running commands without knowing what they do.
2. **Automated, keyless CI/CD** once the manual deploy was confirmed working end-to-end, a second, dedicated deploy identity and a GitHub Actions pipeline were layered on top, so every push to `main` builds and redeploys the same service automatically.
```
Phase 1 — Manual (Cloud Console)
  Cloud Run → Create Service → Connect Repository → Configure & Deploy
  → live service + runtime service account (subscription-shepard-runner) created
 
Phase 2 — Automated (GitHub Actions)
  GitHub (push to main)
     │
     ▼
  GitHub Actions ──► Workload Identity Federation (no stored keys)
     │
     ▼
  Cloud Build    ──► Artifact Registry (Docker image)
     │
     ▼
  Cloud Run.     ──► same service, running as subscription-shepard-runner
```
 
---
 
## 🗄️ Database Strategy
 
> [!NOTE]
> The app uses H2 in **in-memory mode** rather than file-based storage:
> ```
> jdbc:h2:mem:subscriptiondb;DB_CLOSE_DELAY=-1
> ```
 
Cloud Run containers have an **ephemeral, stateless filesystem** anything written to disk is lost on restart or when scaling to a new instance. In-memory H2 is a deliberate trade-off for this portfolio deployment: it avoids provisioning a persistent database (no free tier exists for Cloud SQL) while still demonstrating a fully working, containerized full-stack application.
 
> [!WARNING]
> **Trade-off:** data resets on cold start, and concurrent traffic across multiple Cloud Run instances does not share state. Each instance holds its own isolated in-memory database. A production version would use Cloud SQL (Postgres/MySQL) instead.
 
---
 
## 🐳 Dockerfile
 
A **multi-stage build**:
 
| Stage | Base Image | Purpose |
|---|---|---|
| **Build** | `eclipse-temurin:17-jdk-jammy` | Compiles the app via the Maven Wrapper |
| **Run** | `eclipse-temurin:17-jre-jammy` | Copies only the built JAR into a minimal runtime image |
 
**Jammy** (Ubuntu-based) tags were chosen over Alpine-based tags for multi-architecture support. See [`TROUBLESHOOTING.md`](TROUBLESHOOTING.md).
 
The app respects Cloud Run's dynamically assigned `$PORT` via:
```properties
server.port=${PORT:8080}
```
 
---
 
## ☁️ Phase 1: Manual Cloud Run Setup (via Console)
 
The initial deployment was configured entirely by hand in the Cloud Console, to understand each setting rather than accept CLI defaults:
 
- **Service creation** Cloud Run → Create Service → connected directly to the GitHub repository, with the build type explicitly set to **Dockerfile** (not buildpacks)
- **Runtime identity** manually created a dedicated runtime service account, `subscription-shepard-runner`, rather than using an implicit default identity, so the running application has its own scoped, least-privilege identity separate from any deploy tooling
- **Authentication** set to allow public/unauthenticated access, since this is a public web app demo
- **Billing** request-based, so cost only accrues while actively serving traffic
- **Scaling** minimum instances `0` (scales to zero when idle, no cost while unused), maximum instances `1` (prevents concurrent instances from splitting the in-memory database, and caps worst case cost)
- **Ingress** set to allow traffic from the internet. Once confirmed working, the Cloud Build trigger the Console had auto-created for continuous deployment was **disabled**, to avoid it competing with the GitHub Actions pipeline built in Phase 2.
 
---
 
## 🔑 Phase 2: Automated, Keyless CI/CD
 
> [!IMPORTANT]
> Instead of generating and storing a GCP service account **key** (a long-lived credential) as a GitHub secret, this project uses **Workload Identity Federation (WIF)** Google's current recommended approach for keyless CI/CD.
 
1. A dedicated, **least-privilege** deploy service account (`github-deployer`) separate from the app's runtime identity was created with only the roles needed to deploy:
   - `roles/run.admin`
   - `roles/artifactregistry.writer`
   - `roles/iam.serviceAccountUser`
2. A Workload Identity Pool and OIDC provider were configured, trusting GitHub's own token issuer (`https://token.actions.githubusercontent.com`).
3. The trust binding is scoped to **this exact repository** (`Aminm246/subscription-shepard`) no other GitHub repo can impersonate this service account.
4. `github-deployer` was explicitly granted **Service Account User** on the `subscription-shepard-runner` runtime identity, so it's permitted to deploy a revision that runs under that account (see [`TROUBLESHOOTING.md`](TROUBLESHOOTING.md) for the exact permission error this required fixing).
5. At deploy time, GitHub Actions exchanges its OIDC token for a short-lived GCP access token via `google-github-actions/auth`. **No key file ever exists** on disk or in GitHub Secrets.
Every push to `main` now builds a fresh image, pushes it to Artifact Registry, and deploys it to the same live Cloud Run service as a new revision fully automatically.
 
---
 
## 💰 Cost Controls
 
| Control | Purpose |
|---|---|
| `--max-instances=1` | Prevents traffic spikes from spinning up multiple billed instances; avoids multi instance database inconsistency |
| Minimum instances `0` | Service scales to zero when idle |
| Cloud Run always-free tier | 2M requests/month, 360,000 GiB-seconds covers expected traffic at **$0/month** |
| GCP Spend Cap (Cloud Run) | Hard limit as a safety net |
| Project wide budget alert | Email notification at defined spend thresholds |
 
---
 
## 🧪 Local Testing
 
```bash
# Build and run the container locally, identical to how Cloud Run runs it
docker build -t subscription-shepard-test .
docker run -p 8080:8080 -e PORT=8080 subscription-shepard-test
```
 
Visit `http://localhost:8080`.
