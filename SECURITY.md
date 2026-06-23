# Security Policy

Security reports are taken seriously. If you find a vulnerability in Command Framework,
please report it privately instead of opening a public issue.

## Reporting a Vulnerability

Please send security reports to:

```text
contact@despical.dev
```

When possible, include the following details:

* A clear description of the vulnerability.
* Steps to reproduce the issue.
* The affected version, commit, branch, or server environment.
* Any relevant logs, screenshots, configuration examples, or proof of concept details.
* Whether the issue appears to affect public players, administrators, stored data,
  arena configuration, or server configuration.

Please do not include destructive payloads, real user data, private credentials,
or anything that could damage a running server.

## Scope

The following areas are considered security-sensitive:

* Command permissions, administrator actions, and bypass permissions.
* Player data storage, MySQL credentials, and flat-file persistence.
* Arena configuration, setup tools, signs, holograms, and teleport handling.
* External plugin integrations and placeholder output.
* Packaged resources, configuration reloads, and server startup behavior.

Reports about spam, abuse, or non-security bugs should use the normal GitHub
issue tracker instead.

## Supported Versions

Only the latest public version of Command Framework is currently supported. If you
are running an older version, please update before reporting unless the same
issue also exists on the latest version.

## Response

After a valid report is received, the issue will be reviewed as soon as possible.
If the report is confirmed, a fix will be prepared privately and released with
credit where appropriate.

Please avoid public disclosure until a fix is available.
