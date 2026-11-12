# Security Policy

## Supported versions

Security fixes are released for the **latest minor version** only. Please upgrade to the latest
release before reporting, and check whether the problem still exists there.

| Version                    | Supported          |
|----------------------------|--------------------|
| Latest minor (e.g. `1.6.x`) | :white_check_mark: |
| Older versions             | :x:                |

The `latest` Docker images (`ghcr.io/theomeunier/uptime-kotlin`) always track the most recent release.

## Reporting a vulnerability

**Please do not report security vulnerabilities through public GitHub issues, discussions or pull requests.**

Report them privately through GitHub's private vulnerability reporting:

👉 **[Report a vulnerability](https://github.com/TheoMeunier/uptime-kotlin/security/advisories/new)**
(or go to the **Security** tab of the repository → **Report a vulnerability**).

Please include as much of the following as you can:

- the affected version (or commit) and deployment mode (all-in-one image, API + worker, native or JVM image);
- the type of issue (e.g. authentication bypass, privilege escalation between roles, injection, information disclosure);
- the steps to reproduce, or a proof of concept;
- the impact as you understand it, and who could exploit it (anonymous visitor, viewer account, admin…).

Reports in English or French are welcome.

## What happens next

- **Acknowledgement within 7 days** (14 days at most) of your report.
- An initial assessment (confirmed / not a vulnerability / needs more information) as soon as possible after that.
- For confirmed issues, a fix is developed in a temporary private fork attached to the advisory, then released as a patch version.
- The GitHub Security Advisory is published together with the release, a CVE is requested through GitHub
  when appropriate, and the release notes include a **Security** section describing the fix.
- You are credited in the advisory unless you prefer to stay anonymous.

Please give us a reasonable amount of time to release a fix before any public disclosure. We aim to
fix critical and high severity issues within 30 days, and other confirmed issues within 60 days.

## Scope and known behaviours

Some behaviours are intended and are not considered vulnerabilities on their own:

- **Administrators can probe any address.** Monitors and the "Test" button of notification channels
  make outbound requests to whatever target an admin configures, including internal networks and
  cloud metadata endpoints. Only give the admin role to people you trust with that network access.
- **Docker monitor.** Mounting `/var/run/docker.sock` into the worker gives it root-equivalent access
  to the host. Prefer a read-only Docker socket proxy.
- **Metrics port.** When `QUARKUS_MANAGEMENT_ENABLED=true`, the management port (9000) is not
  authenticated. Do not publish it outside your private network.
- **Push monitor URLs** (`/api/push/{token}`) are public by design: the token is the secret.

Issues in third-party dependencies should be reported upstream; if they affect Uptime Kotlin in a
specific way, a private report here is still welcome.
