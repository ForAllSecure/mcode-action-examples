# Mayhem for Code: Example CI Integration

[![Mayhem for Code](https://drive.google.com/uc?export=view&id=1JXEbfCDMMwwnDaOgs5-XlPWQwZR93fv4)](http://mayhem.forallsecure.com/)

A GitHub Action for using Mayhem for Code to check for reliability, performance and security issues in your application binary (packaged as a containerized [Docker](https://docs.docker.com/get-started/overview/) image).

## About Mayhem for Code

🧪 Modern App Testing: Mayhem for Code is an application security testing tool that catches reliability, performance and security bugs before they hit production.

🧑‍💻 For Developers, by developers: The engineers building software are the best equipped to fix bugs, including security bugs. As engineers ourselves, we're building tools that we wish existed to make our job easier!

🤖 Simple to Automate in CI: Tests belong in CI, running on every commit and PRs. We make it easy, and provide results right in your PRs where you want them. Adding Mayhem for Code to a DevOps pipeline is easy.

Want to try it? [Get started for free](https://forallsecure.com/mayhem-free) today!

## Getting Started

To use the Mayhem for Code GitHub Action, perform the following steps:

1. Create a Mayhem account and copy and paste your account token to GitHub Secrets.

    a. Navigate to [mayhem.forallsecure.com](https://mayhem.forallsecure.com/) to register an account.

    b. Click your profile drop-down and go to *Settings* > *API Tokens* to access your account API token.

    c. Copy and paste your Mayhem token to your [GitHub Secrets](https://docs.github.com/en/actions/security-guides/encrypted-secrets#creating-encrypted-secrets-for-an-organization).

2. Create a `mayhem.yml` file in your GitHub repository located at:

    ```sh
    .github/workflows/mayhem.yml
    ```

## Example GitHub Actions Integration

Modify the GitHub Action workflow file at `.github/workflows/mayhem.yml` and adjust the `context` parameter of the Docker build and push step to point to the corresponding target directory.

```yaml
- name: Build and push Docker image
uses: docker/build-push-action@ad44023a93711e3deb337508980b4b5e9bcdc5dc
with:
    context: lighttpd
    push: true
    tags: ${{ steps.meta.outputs.tags }}
    labels: ${{ steps.meta.outputs.labels }}
```