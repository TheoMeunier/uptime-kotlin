# Issue tracker: GitHub

Issues and PRDs for this repository live in GitHub Issues at `TheoMeunier/uptime-kotlin`. Use the `gh` CLI from the repository so it resolves the remote automatically.

## Conventions

- Create: `gh issue create --title "..." --body "..."`
- Read with comments: `gh issue view <number> --comments`
- List: `gh issue list --state open`
- Comment: `gh issue comment <number> --body "..."`
- Apply or remove labels: `gh issue edit <number> --add-label "..."` or `--remove-label "..."`
- Close: `gh issue close <number> --comment "..."`

When an engineering skill says to publish to the issue tracker, create a GitHub issue. When it says to fetch the relevant ticket, read the issue and its comments.
