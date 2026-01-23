<div align="center">
<a href="https://github.com/TheoMeunier/uptime-kotlin">
<img src="docs/images/logo.svg" alt="Logo" width="300" height="300">
</a>

<h2 align="center">Uptime Kotlin</h3>
  <p align="center">
    <a href="https://github.com/TheoMeunier/uptime-kotlin/issues/new?labels=bug&template=bug-report---.md">Report Bug</a>
    ·
    <a href="https://github.com/TheoMeunier/uptime-kotlin/issues/new?labels=enhancement&template=feature-request---.md">Request Feature</a>
  </p>
</div>

## About The Project

A simple, lightweight, and self-hostable uptime monitoring tool, built and optimized for cloud deployment.
Ideal for monitoring service availability without relying on external solutions.

<table>
  <tr>
    <td><img src="docs/images/dashboard.png" alt="dashboard" width="300"></td>
    <td><img src="docs/images/status_page.png" alt="status page" width="300"></td>
    <td><img src="docs/images/monitor_page.png" alt="monitor" width="300"></td>
  </tr>
</table>

## Getting Started

### Prerequisites

* nodejs lts
* java 21
* postgres 17.7

### Installation for development

1. Clone the repo
   ```sh
   git clone https://github.com/TheoMeunier/uptime-kotlin.git
   ```
2. Configuring the `variable environnement` file

3. Build front-end
   ```sh 
    cd front
    npm install
    npm run dev
   ```
4. Build back-end
   ```sh
    ./gradlew quarkusDev
   ```
5. Run docker compose
   ```sh
    docker compose up -d
   ```

#### Docker

### 1. Create keys for JWT token with `openssl`:

```bash
mkdir certs/ && cd certs

openssl genrsa -out rsaPrivateKey.pem 2048
openssl rsa -pubout -in rsaPrivateKey.pem -out publicKey.pem
openssl pkcs8 -topk8 -nocrypt -inform pem -in rsaPrivateKey.pem -outform pem -out privateKey.pem

chmod 644 privateKey.pem publicKey.pem
```

2. Create a `compose.yaml` file

```yml
services:
  uptime-kotlin:
    image: ghcr.io/theomeunier/uptime/api-native:latest
    container_name: uptime_api
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      QUARKUS_DATASOURCE_USERNAME: uptime
      QUARKUS_DATASOURCE_PASSWORD: uptime
      QUARKUS_DATASOURCE_JDBC_URL: jdbc:postgresql://uptime_database/uptime
      MP_JWT_VERIFY_PUBLICKEY_LOCATION: /certs/publicKey.pem
      SMALLRYE_JWT_SIGN_KEY_LOCATION: /certs/privateKey.pem
    volumes:
      - ./certs:/certs
    networks:
      - app_network

networks:
  app_network:
    driver: bridge

```

### 3. Configure the `variable environnement` file

#### 3.1 PostgreSQL Configuration:

- `QUARKUS_DATASOURCE_USERNAME` : The username of your PostgreSQL database
- `QUARKUS_DATASOURCE_PASSWORD` : The password of your PostgreSQL database
- `QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://[host][:port][/database]` : The URL of your PostgreSQL database

#### 3.2 JWT Configuration:

- `MP_JWT_VERIFY_PUBLICKEY_LOCATION` : The location of the public key used to verify the JWT token
- `SMALLRYE_JWT_SIGN_KEY_LOCATION` : The location of the private key used to sign the JWT token

### 4. Start the application with docker-compose

```bash
docker compose up -d
```

### 5. Access the application

```bash
http://localhost:8888
```

#### Connect to the application

Your default admin credentials are:

- Email: `admin@uptime-kotlin.com`
- Password: `adminadmin`

It is strongly recommended that you change this password immediately after your first login for security reasons.

## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any
contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also
simply open an issue with the tag "enhancement".
Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

Distributed under the MIT License. See `LICENSE` for more information.