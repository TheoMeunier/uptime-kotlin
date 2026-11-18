# Domaines personnalisés pour les pages de statut

Une page de statut peut être servie à la racine de son propre domaine, par exemple `https://status.example.com`, en plus de `/status/<slug>`.

Uptime Kotlin ne gère **pas** le HTTPS : il ne parle que HTTP. Le certificat est l'affaire du reverse proxy placé devant l'instance.

## Fonctionnement

1. Dans l'admin, la page reçoit un **domaine personnalisé** (réglages de la page). Une page privée ne peut pas en avoir.
2. Le DNS du domaine pointe vers le reverse proxy (CNAME vers l'hôte de l'instance, ou enregistrement A/AAAA).
3. Le reverse proxy termine le TLS et transmet la requête à Uptime Kotlin **en conservant l'en-tête `Host`** (ou en ajoutant `X-Forwarded-Host`).
4. Au chargement, le front appelle `GET /api/status-domains/resolve`. Si l'hôte correspond à une page, seule cette page est affichée, à `/`. Sinon, l'application normale démarre.

Sur un domaine personnalisé, l'API ne sert que les routes publiques de la page de statut (`/api/status/...`). Le login et l'administration restent sur le domaine principal, si bien qu'aucune session n'existe sur un domaine tiers.

## Caddy (certificats automatiques)

Avec le TLS « à la demande », Caddy demande un certificat Let's Encrypt la première fois qu'un domaine est appelé, après avoir vérifié auprès d'Uptime Kotlin qu'il est bien déclaré : `GET /api/status-domains/allowed?domain=<domaine>` répond 200 ou 404.

```caddyfile
{
	on_demand_tls {
		ask http://uptime-kotlin:80/api/status-domains/allowed
	}
}

# Domaine principal de l'instance
uptime.example.com {
	reverse_proxy uptime-kotlin:80
}

# Tous les domaines personnalisés déclarés dans l'admin
https:// {
	tls {
		on_demand
	}
	reverse_proxy uptime-kotlin:80
}
```

Ajouter un domaine dans l'admin suffit alors : aucune modification du Caddyfile n'est nécessaire.

> La syntaxe `on_demand_tls { ask ... }` est celle de Caddy 2.7+. Les versions récentes proposent aussi `permission http`. Vérifiez la documentation de votre version.

## Traefik, nginx, Cloudflare…

Tout reverse proxy convient tant qu'il conserve `Host` (ou envoie `X-Forwarded-Host`). Il faut en revanche déclarer chaque domaine et son certificat dans sa configuration (par exemple une règle `Host(`status.example.com`)` avec un `certResolver` dans Traefik).
