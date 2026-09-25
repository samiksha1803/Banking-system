# Deploy InBank on Render

**Use Render for this project.** [Vercel](https://vercel.com) does not run Java WAR / Tomcat apps — it targets static sites, Next.js, and short-lived serverless functions. InBank needs a long-running servlet container and PostgreSQL.

---

## Step-by-step with commands (Windows PowerShell)

All paths assume your project folder:

`C:\Users\HP\NewBankProject\InBank`

### Step 1 — Open PowerShell in the project

```powershell
cd C:\Users\HP\NewBankProject\InBank
```

Check you are in the right place (you should see `pom.xml`, `Dockerfile`, `render.yaml`):

```powershell
dir pom.xml, Dockerfile, render.yaml
```

### Step 2 — Push deployment files to GitHub

Render deploys from GitHub. Commit and push:

```powershell
git status
git add Dockerfile docker-entrypoint.sh docker-compose.yml .dockerignore render.yaml DEPLOYMENT.md docs README.md src/main/java/util/JpaUtil.java
git commit -m "Add Render Docker deployment and deployment docs"
git push origin main
```

If `git push` asks you to sign in, use **Git Credential Manager** / browser login, or a [GitHub Personal Access Token](https://github.com/settings/tokens) as the password.

Verify on the web:  
https://github.com/samiksha1803/Banking-system — files `Dockerfile` and `render.yaml` should appear at the **root** of the repo.

### Step 3 (optional) — Test with Docker on your PC

Requires [Docker Desktop](https://www.docker.com/products/docker-desktop/) running.

```powershell
cd C:\Users\HP\NewBankProject\InBank
docker compose up --build
```

Wait until logs show Tomcat started. In the browser:

http://localhost:8080/InBank/

Stop containers:

```powershell
docker compose down
```

### Step 4 — Create a Render account and link GitHub

No CLI required. In the browser:

1. Go to https://render.com and **Sign Up** (GitHub login is easiest).
2. **Account Settings** → **GitHub** → **Connect** and authorize Render.
3. Grant access to the **Banking-system** repository (or “All repos”).

### Step 5 — Deploy with Blueprint (recommended)

1. https://dashboard.render.com/ → **New** → **Blueprint**.
2. Select repository **samiksha1803/Banking-system**.
3. Render shows `render.yaml` (database + web service). Click **Apply**.
4. Wait until **inbank** web service status is **Live** (first build often **10–20 minutes**).

Watch build output: **inbank** → **Logs**.

### Step 6 — Open your live app

Copy the web service URL from the Render dashboard, then add the context path:

```text
https://YOUR-SERVICE-NAME.onrender.com/InBank/
```

Example login (seeded on first startup):

- Username: `admin`
- Password: `admin123`

### Step 7 — Redeploy after code changes

Every time you change code locally:

```powershell
cd C:\Users\HP\NewBankProject\InBank
git add -A
git commit -m "Describe your change"
git push origin main
```

Render auto-redeploys on push to `main` (if auto-deploy is enabled on the service — default for Blueprint).

Manual redeploy without a new commit: Dashboard → **inbank** → **Manual Deploy** → **Deploy latest commit**.

---

## Manual Render setup (commands + dashboard)

Use this if Blueprint fails or you prefer separate DB + web service.

### A. Create database (dashboard)

**New** → **PostgreSQL** → name `inbank-db`, database `inbank`, region **Singapore**, plan **Free** → **Create**.

### B. Create web service (dashboard)

**New** → **Web Service** → repo **Banking-system**:

| Setting | Value |
|---------|--------|
| Name | `inbank` |
| Region | Singapore |
| Branch | `main` |
| Root Directory | *(empty — repo root is InBank)* |
| Runtime | **Docker** |
| Instance type | Free |
| Health Check Path | `/InBank/` |

**Environment** → add variable:

| Key | Value |
|-----|--------|
| `DATABASE_URL` | From **inbank-db** → **Info** → **Internal Database URL** |

Click **Create Web Service**.

### C. Useful checks (local, after clone)

```powershell
git clone https://github.com/samiksha1803/Banking-system.git
cd Banking-system
git log -1 --oneline
```

---

## Prerequisites

1. Code on GitHub: [Banking-system](https://github.com/samiksha1803/Banking-system) (repo root should contain `Dockerfile`, `pom.xml`, `render.yaml`).
2. Free [Render](https://render.com) account.

---

## Method A — Blueprint (fastest)

1. Open [Render Dashboard](https://dashboard.render.com/) → **New** → **Blueprint**.
2. Connect GitHub and select **Banking-system**.
3. Render reads `render.yaml` and creates:
   - PostgreSQL database `inbank-db`
   - Web service **inbank** (Docker build)
4. Click **Apply**. First deploy builds Maven inside Docker (often **10–20 minutes** on free tier).
5. When status is **Live**, open the service URL, e.g.  
   `https://inbank-xxxx.onrender.com/InBank/`  
   (context path **`/InBank`** is required.)

Default admin (created on first startup): **admin** / **admin123** — change after login.

---

## Method B — Manual (dashboard)

### 1. PostgreSQL

1. **New** → **PostgreSQL** → name `inbank-db`, database `inbank`, plan **Free**.
2. After create, copy **Internal Database URL** (starts with `postgresql://`).

### 2. Web service

1. **New** → **Web Service** → connect repo.
2. **Language:** Docker  
3. **Root directory:** leave blank if `Dockerfile` is at repo root; otherwise `InBank`.
4. **Plan:** Free  
5. **Environment variables:**

   | Key | Value |
   |-----|--------|
   | `DATABASE_URL` | Paste **Internal Database URL** from the database |

   Optional overrides (instead of `DATABASE_URL`):

   | Key | Example |
   |-----|---------|
   | `INBANK_JDBC_URL` | `jdbc:postgresql://HOST:5432/inbank` |
   | `INBANK_DB_USER` | from Render DB dashboard |
   | `INBANK_DB_PASSWORD` | from Render DB dashboard |

6. **Health check path:** `/InBank/`
7. **Create Web Service** and wait for deploy.

---

## After deploy

| Item | Detail |
|------|--------|
| App URL | `https://<your-service>.onrender.com/InBank/` |
| Login | Register a user or use admin (see above) |
| Logs | Render → your web service → **Logs** (Tomcat / Hibernate errors appear here) |
| Cold start | Free web services **spin down** after ~15 min idle; first request may take 30–60 s |

---

## Troubleshooting

**502 / deploy failed**

- Check **Logs** for Maven or Tomcat errors.
- Confirm health check is `/InBank/` (not `/`).

**Database connection errors**

- Web service and DB must be in the **same Render account/region**.
- Prefer **Internal** database URL on the web service, not the external URL.
- Or set `INBANK_JDBC_URL` manually with `?sslmode=require` if Render requires SSL.

**404 on root URL**

- Tomcat serves the app at **`/InBank`**, not `/`. Always use `/InBank/` in links and demos.

**Build timeout**

- Free tier builds can be slow. Retry deploy or upgrade build minutes if needed.

---

## Resume line (example)

> Deployed InBank (Java Servlets, Hibernate, PostgreSQL) on **Render** using **Docker**, with managed Postgres and GitHub-backed CI deploys.
