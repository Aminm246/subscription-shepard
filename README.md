<div align="center">
📊 Subscription Shepard

A full-stack subscription tracker with secure authentication, containerized and deployed to the cloud with a keyless, automated CI/CD pipeline.

Show Image Show Image Show Image Show Image Show Image Show Image

🔗 Live Demo  ·  📘 Deployment Docs  ·  🐛 Troubleshooting Log

</div>

[!TIP] Live demo: add your .run.app URL here once deployed

Show Image Add a screenshot of the dashboard here — drag an image into this file in GitHub's web editor and it inserts automatically.

🧭 Overview

Users register an account, log in, and track recurring subscriptions — Netflix, Spotify, rent, and more — with a live dashboard showing total monthly spend and a breakdown chart.

Originally built as a team capstone project at Metropolitan State University. Independently containerized, secured, and deployed to production cloud infrastructure afterward.

🛠️ Tech Stack
Layer	Technology
Backend	Java 17 · Spring Boot 3 · Spring Security · Spring Data JPA
Frontend	Thymeleaf · Bootstrap · Chart.js
Database	H2 (in-memory, stateless container design)
Containerization	Docker (multi-stage build)
Cloud Infrastructure	Google Cloud Run · Artifact Registry
CI/CD	GitHub Actions + GCP Workload Identity Federation
🔐 Key Engineering Decisions

[!IMPORTANT] Keyless CI/CD. Deployment authentication uses Workload Identity Federation instead of downloaded service account keys — GCP's current recommended security practice. GitHub's own OIDC token is exchanged for a short-lived GCP credential at deploy time, scoped to this exact repository. No key file is ever stored anywhere.

Stateless database design — migrated from file-based to in-memory H2 to match Cloud Run's ephemeral container filesystem, with trade-offs explicitly documented rather than hidden. See docs/DEPLOYMENT.md.
Multi-architecture Docker build — base images support both local ARM development (Apple Silicon) and Cloud Run's amd64 production runtime.
Cost-conscious infrastructure — runs entirely within GCP's always-free tier, backed by budget alerts and spend caps.

📄 Full architecture and reasoning → docs/DEPLOYMENT.md 🐛 Real debugging log with root causes → docs/TROUBLESHOOTING.md

🚀 Running Locally

Option 1 — Maven

bash
./mvnw spring-boot:run

Visit http://localhost:8080

Option 2 — Docker (identical to how Cloud Run runs it)

bash
docker build -t subscription-shepard .
docker run -p 8080:8080 -e PORT=8080 subscription-shepard
📁 Project Structure
src/main/java/edu/metro/subscriptionshepard/   → application code
src/main/resources/templates/                   → Thymeleaf views
docs/                                            → deployment & troubleshooting documentation
Dockerfile                                       → multi-stage container build
👥 Credits

Built by Amin Mohamed, Nicole Golden, Jaileia Yang, and Abdishakur Abdi as a capstone project for Metropolitan State University.

Cloud deployment, containerization, and CI/CD automation independently implemented by Amin Mohamed.

<div align="center">

License: MIT

</div>
