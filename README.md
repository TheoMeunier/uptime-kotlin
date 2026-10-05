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

Uptime Kotlin ships as a single Docker image: the web interface, the API and the background jobs (monitoring
checks, notifications, maintenance windows, purges) run in one process. Only PostgreSQL runs next to it. The image
is published for amd64 and arm64, and Docker pulls the one that matches your host.

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

> [!TIP]
> Keep a memory limit (`mem_limit`). The image is a native executable whose heap is sized from the memory it can
> see: around 50 MB with a 512 MB limit, but it can grow to several hundred MB on a host without limit.

2. Configure the `variable environnement` file

   2.1 Encrypted variables:

    - `ENCRYPTION_MASTER_KEY` : The master key used to encrypt sensitive data. Required, at least 32 bytes
      (`openssl rand -base64 32`). The application refuses to start without it.

   2.2 PostgreSQL Configuration:

    - `QUARKUS_DATASOURCE_USERNAME` : The username of your PostgreSQL database
    - `QUARKUS_DATASOURCE_PASSWORD` : The password of your PostgreSQL database
    - `QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://[host][:port][/database]` : The URL of your PostgreSQL database

   2.3 JWT Configuration:

    - `MP_JWT_VERIFY_ISSUER` : The issuer of the JWT token

   The application signs its tokens with an RSA key pair that it generates on first start and stores in the
   database, the private key encrypted with `ENCRYPTION_MASTER_KEY`. Every instance reads the same pair, so nothing
   has to be shared between them. Changing `ENCRYPTION_MASTER_KEY` therefore makes the stored key unreadable: the
   application refuses to start until the previous key is restored.

   To use your own keys instead (or keep the ones of an existing installation), set both variables and mount the files:

    - `MP_JWT_VERIFY_PUBLICKEY_LOCATION` : The location of the public key used to verify the JWT token
    - `SMALLRYE_JWT_SIGN_KEY_LOCATION` : The location of the private key (PKCS#8) used to sign the JWT token

   2.4 Schedulers Configuration:

    - `SCHEDULER_STRATEGY` (default `database`) : `database` to run the monitoring checks and the other background
      jobs in the same process, or `none` when dedicated workers run them (see [Cluster mode](#cluster-mode)).
    - `SCHEDULER_WORKER_CONCURRENCY` (default `4`) : checks run in parallel, and at most this many checks start per
      second. Keep it under the datasource pool size.

   `QUARKUS_SCHEDULER_STRATEGY=db-lock` from previous versions is still understood as `SCHEDULER_STRATEGY=database`
   and logs a deprecation warning: replace it.

   2.5 Maintenance windows (set the same values on the application and the workers):

    - `MAINTENANCE_HORIZON_DAYS` (default `90`) : how far ahead recurring windows are unrolled into occurrences.
    - `MAINTENANCE_MAX_DURATION_HOURS` (default `24`) : longest window the application accepts.


3. Start the application with docker-compose

```bash
   docker compose up -d
```

4. Access the application

```bash
   http://localhost:8080
```

> [!NOTE]
> Behind your own reverse proxy (Traefik, Caddy, Nginx, ...), forward every path to port `8080` of the container:
> the web interface and the API (`/api/...`) answer on the same origin. Probe exports and log purges can take several
> minutes on a large history, so allow a read timeout of about 300 seconds.

### Cluster mode

By default the application runs everything. The cluster mode moves the background jobs (monitoring checks,
notifications, maintenance windows, purges) to one or more workers. Both modes run the same engine: a job queue stored
in PostgreSQL, which the workers share without running a check twice. No extra service is required.

The cluster uses the same `uptime-kotlin` image as above, with `SCHEDULER_STRATEGY` set to `none`: it then only
serves the web interface and the API. You can run several instances of it behind a load balancer, since the JWT keys
are shared through the database.

#### Add Workers

```yaml
uptime-kotlin-worker:
  image: ghcr.io/theomeunier/uptime-kotlin/worker:latest
  container_name: uptime_kotlin_worker
  restart: unless-stopped
  mem_limit: 512m
  environment:
    TZ: Europe/Paris
    SCHEDULER_STRATEGY: database
    SCHEDULER_WORKER_NAME: worker-primary
    SCHEDULER_WORKER_CONCURRENCY: "4"
    MAINTENANCE_HORIZON_DAYS: "90"
    MAINTENANCE_MAX_DURATION_HOURS: "24"
    QUARKUS_DATASOURCE_USERNAME: uptime-kotlin
    QUARKUS_DATASOURCE_PASSWORD: change-me
    QUARKUS_DATASOURCE_JDBC_URL: jdbc:postgresql://postgres:5432/uptime-kotlin
    ENCRYPTION_MASTER_KEY: change-me-32-characters-minimum-0
  depends_on:
    - uptime-kotlin
```

The worker has no web interface and no API: it only needs to reach PostgreSQL and the services it monitors.

#### Configure the `variable environnement` file

1. Cluster mode

   On the **`uptime-kotlin`** service: `SCHEDULER_STRATEGY` set to `none`, so that only the workers run the
   background jobs.

   On each **worker** (these are `SCHEDULER_*` variables, not `QUARKUS_SCHEDULER_*`):

    - `SCHEDULER_STRATEGY`: `database` to run checks from the queue, `none` to stay idle.
    - `SCHEDULER_WORKER_NAME`: name of the worker, unique for each instance. Checks queued under a name that no
      running instance uses any more are picked up by the others after 30 seconds.
    - `SCHEDULER_WORKER_CONCURRENCY` (default `4`): checks run in parallel by this worker. Keep it under the datasource
      pool size.

### Images and platforms

| Image                                      | Role                                                 |
|--------------------------------------------|------------------------------------------------------|
| `ghcr.io/theomeunier/uptime-kotlin`        | Web interface, API and background jobs (or API only) |
| `ghcr.io/theomeunier/uptime-kotlin/worker` | Background jobs for the cluster mode                 |

Both are native executables published for `linux/amd64` and `linux/arm64` (Raspberry Pi, AWS Graviton, Ampere
servers, Apple Silicon, ...) under the same name: there is no image to swap on an ARM64 host.

> [!IMPORTANT]
> The host must run a 64-bit OS: `uname -m` must print `x86_64`, `aarch64` or `arm64`. 32-bit ARM (`armv7l`) is not
> supported, e.g. a Raspberry Pi running a 32-bit OS.

<details>
<summary>Building the images yourself</summary>

From the root of the repository, with Docker (at least 8 GB of RAM for Docker, native compilation is memory hungry).
The images are built for the architecture of the machine that builds them:

```bash
docker build -f docker/all-in-one/Dockerfile.native -t uptime-kotlin:local .
docker build -f docker/worker/Dockerfile.native -t uptime-kotlin-worker:local .
```

`docker compose -f docker/all-in-one/compose.yaml up --build` builds the image and starts it with PostgreSQL on
`http://localhost:8090`.

</details>

### Migrating from the `app` + `api` + Nginx setup

The `app`, `api` and `api-arm64` images are deprecated: they are still published for a few releases, then they will
be removed. `worker-arm64` is no longer published, `worker` now covers both architectures.

1. In your `compose.yaml`, replace the `uptime-kotlin-app`, `uptime-kotlin-api` and `nginx` services with the
   `uptime-kotlin` service from [Getting Started](#getting-started). Keep the environment variables of your former
   `api` service, including `SCHEDULER_STRATEGY`.
2. Publish port `8080` of the `uptime-kotlin` container (or point your reverse proxy to it) instead of the Nginx port.
   The `docker/nginx.conf` file is no longer needed.
3. On an ARM64 host, replace `worker-arm64` with `worker`.
4. Run `docker compose up -d`. The database is unchanged and the JWT keys are read from it (or from your key files if
   you set `MP_JWT_VERIFY_PUBLICKEY_LOCATION` and `SMALLRYE_JWT_SIGN_KEY_LOCATION`), so users stay signed in.

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
