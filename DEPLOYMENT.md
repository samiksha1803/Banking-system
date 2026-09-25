# Deploying InBank

Your app is a **Java WAR** (Servlets + JSP + Hibernate) that needs:

1. **Java servlet container** — Apache Tomcat 9  
2. **PostgreSQL** database named `inbank`

You cannot host this on static hosts (GitHub Pages, Netlify static). You need a server that runs Java or Docker.

---

## Option 1 — What you already use (local / interview laptop)

**Best for:** demos, college viva, interviews on your own PC.

1. Install **JDK 21**, **PostgreSQL**, **Tomcat 9**, **Eclipse**.
2. Create database `inbank` in pgAdmin.
3. Deploy from Eclipse to Tomcat → open **http://localhost:8080/InBank/**

`persistence.xml` uses `localhost:5432` — correct for this setup.

---

## Option 2 — Docker (recommended for “deploy anywhere”)

**Best for:** one-command run on any machine with [Docker Desktop](https://www.docker.com/products/docker-desktop/).

### Steps

```powershell
cd C:\Users\HP\NewBankProject\InBank
docker compose up --build
```

Wait until Tomcat starts (first build may take several minutes).

- App: **http://localhost:8080/InBank/**  
- PostgreSQL on host port **5433** (inside Docker the app uses hostname `db`)

Stop:

```powershell
docker compose down
```

Data is kept in Docker volume `inbank_pg`. To wipe DB: `docker compose down -v`

### How it works

- `docker-compose.yml` starts PostgreSQL + Tomcat with your WAR.
- Environment variables override JDBC settings (see `JpaUtil.java`):
  - `INBANK_JDBC_URL`
  - `INBANK_DB_USER`
  - `INBANK_DB_PASSWORD`

---

## Option 3 — Manual WAR on a VPS (Linux cloud server)

**Best for:** resume line “deployed on AWS/DigitalOcean/Azure VM”.

Examples: DigitalOcean Droplet, AWS EC2, Azure VM (Ubuntu 22.04).

### On the server

1. Install Java 21, Tomcat 9, PostgreSQL.
2. Create DB `inbank` and user/password.
3. On your PC, build the WAR:

```powershell
cd C:\Users\HP\NewBankProject\InBank
mvn -DskipTests package
```

4. Copy `target\InBank.war` to the server (`scp`) into Tomcat `webapps/`.
5. Set Tomcat environment (in `setenv.sh` or systemd):

```bash
export INBANK_JDBC_URL=jdbc:postgresql://localhost:5432/inbank
export INBANK_DB_USER=postgres
export INBANK_DB_PASSWORD=your_strong_password
```

6. Open firewall port **8080** (or put Nginx in front on port 80/443).
7. Visit `http://YOUR_SERVER_IP:8080/InBank/`

**Security:** Change default admin password, use strong DB password, enable HTTPS (Let’s Encrypt + Nginx) before sharing publicly.

---

## Option 4 — Render (public URL from GitHub)

**Best for:** portfolio / resume with a live link. **Do not use Vercel** for InBank — Vercel does not run Tomcat WAR apps.

Full instructions: **[docs/RENDER-DEPLOY.md](docs/RENDER-DEPLOY.md)**

Short path:

1. Push `Dockerfile`, `render.yaml`, and latest code to GitHub.
2. Render → **New** → **Blueprint** → select repo → **Apply**.
3. Open `https://<service>.onrender.com/InBank/`

Render sets `DATABASE_URL`; the app converts it to JDBC automatically.

### Other PaaS (Docker)

| Platform | Approach |
|----------|----------|
| [Railway](https://railway.app) | Deploy from GitHub + PostgreSQL; set `DATABASE_URL` or `INBANK_JDBC_URL`. |
| [Fly.io](https://fly.io) | `fly launch` with Docker; attach Postgres. |

---

## Option 5 — Build WAR only (no Docker)

```powershell
mvn -DskipTests package
```

Output: `target\InBank.war`

Drop into Tomcat `webapps\` or Eclipse **Servers** view. Context path stays **`/InBank`**.

---

## Checklist before public deployment

- [ ] Change PostgreSQL password (not `root` in production).
- [ ] Change default **admin** password after first login.
- [ ] Turn off `hibernate.show_sql` in production `persistence.xml` (optional).
- [ ] Use HTTPS.
- [ ] Do not expose pgAdmin/PostgreSQL port 5432 to the internet.

---

## Quick comparison

| Method | Difficulty | Public URL? | Good for resume |
|--------|------------|-------------|-----------------|
| Eclipse + Tomcat (local) | Easy | No (unless ngrok) | “Developed and tested locally” |
| Docker Compose | Medium | Local only | “Containerized deployment” |
| VPS + WAR | Medium–Hard | Yes | “Deployed on cloud VM” |
| Render/Railway + Docker | Medium | Yes | “CI/CD from GitHub + cloud DB” |

---

## Optional: temporary public link (demo only)

[ngrok](https://ngrok.com): while Tomcat runs locally:

```powershell
ngrok http 8080
```

Share the HTTPS URL + path `/InBank/` — useful for remote interviews; not for production.
