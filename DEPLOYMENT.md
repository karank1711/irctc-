# Deploy the IRCTC Admin Dashboard

The application is a Java 17 WAR. Local development uses embedded Tomcat through `mvn compile exec:java`. The Docker image builds that same WAR and runs it on Tomcat `11.0.15` with Java 17. Docker Compose in this repository starts only MySQL. It does not start the web application, and Render does not deploy that Compose file as the web service.

The generated WAR file is `target/irctc-admin.war`. Inside the image it is deployed as `/usr/local/tomcat/webapps/ROOT.war`, so the site stays at `/login` rather than a nested context path.

## 1. Local build

From the project root, with JDK 17 and Maven 3.9+:

```text
mvn -B package
```

This writes `target/irctc-admin.war`. Tests alone:

```text
mvn -B test
```

Local server, using the `.env` file and embedded Tomcat:

```text
mvn -B compile exec:java
```

Open `http://localhost:8080/login`. XAMPP or the Compose MySQL service can stay as they are. Point `DB_URL` at the database you are actually running. The Compose file publishes MySQL on host port `3307`. A default XAMPP MySQL install listens on `3306`.

## 2. Local Docker test

Build from the repository root. The build context is this directory, and the Dockerfile is `./Dockerfile`.

```text
docker build -t irctc-admin:local .
```

Start only the existing database container, then run the web image against it. Do not commit the password into a file that is tracked by Git.

```text
docker compose up -d
docker run --rm -p 8080:8080 -e PORT=8080 -e DB_URL="jdbc:mysql://host.docker.internal:3307/irctc_admin?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&serverTimezone=Asia/Kolkata" -e DB_USERNAME=root -e DB_PASSWORD=your-local-db-password -e DB_AUTO_INIT=true -e ADMIN_EMAIL=admin@example.com -e ADMIN_PASSWORD=replace-with-a-long-password irctc-admin:local
```

`docker compose up -d` is still database-only. `scripts/dev.mjs` depends on that behavior.

## 3. Render settings

Create a **Web Service** from the GitHub repository. Do not use the Compose file as the web service.

| Setting | Value |
|---|---|
| Root directory | repository root (the directory that contains `Dockerfile` and `pom.xml`) |
| Runtime | Docker |
| Dockerfile path | `Dockerfile` |
| Docker build context | `.` |
| Health check path | `/login` |

Render sets `PORT`. The image entrypoint `docker/render-entrypoint.sh` makes Tomcat listen on that port.

Host MySQL separately. Render does not turn `docker-compose.yml` into the application plus the database. Use a managed MySQL 8 service, or another host you control, with a private connection string. Do not publish that database to the open internet.

The container filesystem is ephemeral. Banner uploads are stored in `data/uploads` under the Tomcat working directory. Those files disappear when the service restarts unless you attach external storage later. Database rows are not stored in the container.

## 4. Production environment variables

Set these on the Render web service. Do not put the real values in Git.

| Variable | Purpose |
|---|---|
| `PORT` | Set by Render. Do not hardcode it. |
| `DB_URL` | Full JDBC URL, including host, port, and database name. |
| `DB_USERNAME` | Database user. |
| `DB_PASSWORD` | Database password. |
| `DB_AUTO_INIT` | `true` applies `sql/schema.sql` with `CREATE TABLE IF NOT EXISTS`. It does not delete rows. |
| `ADMIN_EMAIL` | Creates this administrator only when that email is not already in `admins`. |
| `ADMIN_PASSWORD` | Used only for that first create. It is stored as a BCrypt hash. Changing it later does not reset an existing account. |
| `SEED_SAMPLE_DATA` | Leave `false`. Startup does not load `sql/seed.sql`. |

Example `DB_URL` for a remote MySQL 8 server. Replace the host. Keep `useSSL` aligned with what that host requires.

```text
jdbc:mysql://your-mysql-host:3306/irctc_admin?useSSL=true&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&serverTimezone=Asia/Kolkata
```

## 5. Production MySQL

Create an empty database named `irctc_admin` with `utf8mb4` / `utf8mb4_unicode_ci`. Create a user that can use only that database. Give Render the host, port, database name, username, and password through the variables above.

The Compose service `db` (`mysql:8.4`, container `irctc-admin-mysql`, volume `irctc-mysql-data`) is for local use. Its data stays in that Docker volume. It is not the Render database.

## 6. Import an existing database

This copies data. It does not drop the local XAMPP or Docker database.

1. From the machine that has the current data, dump that database:

```text
mysqldump --host=127.0.0.1 --port=3306 --user=root --routines --single-transaction --databases irctc_admin > irctc_admin.sql
```

Use port `3307` if the source is the Compose container instead of XAMPP.

2. Import into the empty production database:

```text
mysql --host=your-mysql-host --port=3306 --user=your-user --password irctc_admin < irctc_admin.sql
```

3. Deploy the web service with `DB_AUTO_INIT=true`. Startup creates any missing tables and does not remove imported rows. If the dump already contains the administrator, leave `ADMIN_EMAIL` set to that same address so startup does not try to insert a second one. An existing email is left unchanged.

4. Keep the dump file out of Git.

## 7. Verify and troubleshoot

After deploy, open `https://<your-service>.onrender.com/login`. A signed-out visit to `/` redirects to `/login`.

| Symptom | What to check |
|---|---|
| Service never becomes healthy | The process must listen on Render's `PORT`. The entrypoint reads that variable. Health check path is `/login`. |
| Startup says the database could not be initialized | `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` on the web service. The database host must allow Render's outbound connection. |
| Login page loads but sign-in fails | The imported `admins` row, or `ADMIN_EMAIL` / `ADMIN_PASSWORD` when the email does not exist yet. `.env` on your PC is not uploaded. |
| Pages 404 under `/irctc-admin/` | The image must deploy `ROOT.war`, which the Dockerfile already does. |
| Uploaded banner missing after restart | Uploads are on the container disk, not in MySQL. |

Local XAMPP development is unchanged: keep using `.env` and `mvn compile exec:java`.
