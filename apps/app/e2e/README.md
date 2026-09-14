# End-to-end tests

One Playwright parcours (`probe-lifecycle.spec.ts`): setup → login → monitor
creation → the monitor appears. It exercises the front against a real backend,
which is the only way the routing, auth guard, `/api` calls and query
invalidation are checked at all.

## Running it

The app calls `/api` with no Vite proxy, so the suite drives the stack through
the nginx reverse proxy from `compose.yaml`, not through `localhost:5173`.

```bash
docker compose up -d          # repo root: nginx (8888), postgres, redis
./gradlew :backend:quarkusDev # repo root: backend on 8080
npm run dev                   # apps/app: vite on 5173

npm run test:e2e              # apps/app
npm run test:e2e:ui           # same, in Playwright's UI mode
```

First run only: `npx playwright install chromium` downloads the browser.

Point it elsewhere with `E2E_BASE_URL`. The account is created on the setup
screen the first time; against a database that already has a user, set
`E2E_EMAIL` and `E2E_PASSWORD` to credentials it accepts.
