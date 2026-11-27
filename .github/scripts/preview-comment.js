// Creates or updates the single preview comment of a pull request.
const MARKER = '<!-- uptime-kotlin-preview -->';

module.exports = async ({ github, context, status }) => {
  const pr = context.payload.pull_request;
  const sha = pr.head.sha.substring(0, 7);
  const url = `https://pr-${pr.number}.staging.uptime-kotlin.com`;
  const run = `${context.serverUrl}/${context.repo.owner}/${context.repo.repo}/actions/runs/${context.runId}`;

  const states = {
    building: `⏳ Building \`${sha}\` ([logs](${run}))`,
    ready: `✅ Image \`${sha}\` ready, Argo CD rolls it out within a few minutes`,
    failed: `❌ Build of \`${sha}\` failed ([logs](${run}))`,
    removed: '🗑️ Environment removed',
  };

  const body = [
    MARKER,
    `**Preview** of this pull request in the \`uptime-kotlin-staging\` environment.`,
    '',
    '| Application | URL | Last deployment |',
    '|---|---|---|',
    `| uptime-kotlin | ${status === 'removed' ? '—' : url} | ${states[status]} |`,
  ].join('\n');

  const comments = await github.paginate(github.rest.issues.listComments, {
    ...context.repo,
    issue_number: pr.number,
    per_page: 100,
  });
  const existing = comments.find((comment) => comment.body?.startsWith(MARKER));

  if (existing) {
    await github.rest.issues.updateComment({ ...context.repo, comment_id: existing.id, body });
  } else if (status !== 'removed') {
    await github.rest.issues.createComment({ ...context.repo, issue_number: pr.number, body });
  }
};
