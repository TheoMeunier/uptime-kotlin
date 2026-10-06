<div align="center">
<a href="https://github.com/TheoMeunier/uptime-kotlin">
<img src="docs/images/logo-ui.png" alt="Logo" width="150" height="150">
</a>

<h2 align="center">Uptime Kotlin</h2>

<p align="center">
  <a href="https://github.com/TheoMeunier/uptime-kotlin/releases">
    <img src="https://img.shields.io/github/v/release/TheoMeunier/uptime-kotlin" alt="Release">
  </a>
  <a href="LICENSE">
    <img src="https://img.shields.io/github/license/TheoMeunier/uptime-kotlin" alt="License">
  </a>
<a href="https://github.com/TheoMeunier/uptime-kotlin/actions/workflows/back-ci.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/TheoMeunier/uptime-kotlin/back-ci.yml?branch=main&label=backend" alt="Backend CI">
  </a>
  <a href="https://github.com/TheoMeunier/uptime-kotlin/actions/workflows/app-ci.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/TheoMeunier/uptime-kotlin/app-ci.yml?branch=main&label=frontend" alt="Frontend CI">
  </a>
</p>

<p align="center">
<a href="https://github.com/TheoMeunier/uptime-kotlin/issues/new?labels=bug&template=bug-report---.md">Report Bug</a>
·
<a href="https://github.com/TheoMeunier/uptime-kotlin/issues/new?labels=enhancement&template=feature-request---.md">Request Feature</a>
</p>
</div>

## About The Project

Self-hosted uptime monitoring for HTTP, TCP, DNS, databases, Kafka, RabbitMQ and more. Public status pages,
multi-channel notifications, and a worker-based cluster mode to scale your checks.

<table>
  <tr>
    <td><img src="docs/images/dashboard.webp" alt="dashboard" width="300"></td>
    <td><img src="docs/images/status-page.webp" alt="status page" width="300"></td>
    <td><img src="docs/images/monitor-page.webp" alt="monitor" width="300"></td>
  </tr>
</table>

### Key Features

- HTTP/HTTPS, TCP, DNS, ping, PostgreSQL, Microsoft SQL Server, MySQL/MariaDB, Redis, SMTP, Kafka and RabbitMQ
  monitoring
- Real-time dashboard with historical data
- Multi-channel notifications (Email, Slack, Discord, Teams, Webhook), with periodic resend while a monitor
  stays down (per-monitor interval, disabled by default)
- Public status pages for your users
- JWT authentication with encrypted data
- Single Docker image for amd64 and ARM64, a few tens of MB of RAM

### Built With

- [Kotlin](https://kotlinlang.org/)
- [Quarkus](https://quarkus.io/)
- [React](https://reactjs.org/)
- [PostgreSQL](https://www.postgresql.org/)
- [Docker](https://www.docker.com/)

## Getting Started

One image runs the web interface, the API and the monitoring checks. It is published for amd64 and arm64.

1. Create a `compose.yaml` file

```yml
services:
  uptime-kotlin:
    image: ghcr.io/theomeunier/uptime-kotlin:latest
    container_name: uptime_kotlin
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      TZ: Europe/Paris
      QUARKUS_DATASOURCE_USERNAME: uptime-kotlin
      QUARKUS_DATASOURCE_PASSWORD: change-me
      QUARKUS_DATASOURCE_JDBC_URL: jdbc:postgresql://postgres:5432/uptime-kotlin
      ENCRYPTION_MASTER_KEY: change-me-32-characters-minimum-0
      MP_JWT_VERIFY_ISSUER: https://issuer.uptime-kotlin.com

  postgres:
    image: postgres:17.4-alpine
    container_name: uptime_kotlin_database
    restart: unless-stopped
    environment:
      POSTGRES_DB: uptime-kotlin
      POSTGRES_USER: uptime-kotlin
      POSTGRES_PASSWORD: change-me
      PGDATA: /var/lib/postgresql/data/pgdata
    volumes:
      - ./storage-db:/var/lib/postgresql/data
```

2. Configure the environment variables

    - `ENCRYPTION_MASTER_KEY` (required): at least 32 bytes, `openssl rand -base64 32`. Do not change it afterwards,
      it also encrypts the JWT signing key stored in the database.
    - `QUARKUS_DATASOURCE_USERNAME`, `QUARKUS_DATASOURCE_PASSWORD`, `QUARKUS_DATASOURCE_JDBC_URL`: PostgreSQL access.
    - `MP_JWT_VERIFY_ISSUER`: issuer of the JWT tokens.
    - `SCHEDULER_STRATEGY` (default `database`): `none` when workers run the checks (see [Cluster mode](#cluster-mode)).
    - `SCHEDULER_WORKER_CONCURRENCY` (default `4`): checks run in parallel.
    - `MAINTENANCE_HORIZON_DAYS` (default `90`) and `MAINTENANCE_MAX_DURATION_HOURS` (default `24`): maintenance
      windows, same values on the application and the workers.
    - `NOTIFICATIONS_DISPLAY_TIMEZONE` (default `UTC`): zone of the dates shown in Slack, Teams and e-mail
      notifications (IANA name, e.g. `Europe/Paris`), same value on the application and the workers. Webhook
      payloads stay in UTC (ISO-8601).

   <details>
   <summary>Use your own JWT keys</summary>

   Set both variables and mount the files: `MP_JWT_VERIFY_PUBLICKEY_LOCATION` (public key) and
   `SMALLRYE_JWT_SIGN_KEY_LOCATION` (private key, PKCS#8).

   </details>

3. Start the application

```bash
docker compose up -d
```

4. Open `http://localhost:8080`

> [!TIP]
> Set a memory limit (`mem_limit: 512m`): the native heap is sized from the memory the container sees, ~50 MB with a
> limit, several hundred MB without.
>
> Behind a reverse proxy, forward every path to port `8080` and allow a 300 s read timeout for exports.

### Cluster mode

Run the `uptime-kotlin` image with `SCHEDULER_STRATEGY: none` (it can be replicated behind a load balancer) and add
workers. The workers share a job queue stored in PostgreSQL, no extra service is needed.

```yaml
uptime-kotlin-worker:
  image: ghcr.io/theomeunier/uptime-kotlin/worker:latest
  container_name: uptime_kotlin_worker
  restart: unless-stopped
  environment:
    TZ: Europe/Paris
    SCHEDULER_STRATEGY: database
    SCHEDULER_WORKER_NAME: worker-primary
    SCHEDULER_WORKER_CONCURRENCY: "4"
    QUARKUS_DATASOURCE_USERNAME: uptime-kotlin
    QUARKUS_DATASOURCE_PASSWORD: change-me
    QUARKUS_DATASOURCE_JDBC_URL: jdbc:postgresql://postgres:5432/uptime-kotlin
    ENCRYPTION_MASTER_KEY: change-me-32-characters-minimum-0
  depends_on:
    - uptime-kotlin
```

`SCHEDULER_WORKER_NAME` must be unique per worker. Checks left by a stopped worker are picked up by the others after
30 seconds.

### Images

| Image                                      | Role                                     |
|--------------------------------------------|------------------------------------------|
| `ghcr.io/theomeunier/uptime-kotlin`        | Web interface, API and monitoring checks |
| `ghcr.io/theomeunier/uptime-kotlin/worker` | Monitoring checks only (cluster mode)    |

Both run on `linux/amd64` and `linux/arm64` (64-bit OS only).

### Build the images yourself

Native compilation needs about 8 GB of RAM for Docker.

```bash
docker build -f docker/all-in-one/Dockerfile.native -t uptime-kotlin:local .
docker build -f docker/worker/Dockerfile.native -t uptime-kotlin-worker:local .
```

## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any
contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also
simply open an issue with the tag "enhancement". Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

Distributed under the MIT License. See `LICENSE` for more information.
