// TeamCity Kotlin DSL pipeline for Mayhem.

import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.triggers.finishBuildTrigger
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot

version = "2024.03"

project {
    description = "Mayhem example pipeline"

    vcsRoot(MainVcs)

    params {
        param("env.REGISTRY", "ghcr.io")
        param("env.IMAGE", "ghcr.io/forallsecure/mcode-action-examples")
        param("env.MAYHEM_URL", "https://app.mayhem.security")
        password("env.MAYHEM_TOKEN", "credentialsJSON:replace-with-mayhem-token-id")
        param("env.GITHUB_USERNAME", "forallsecure-demo")
        password("env.GITHUB_TOKEN", "credentialsJSON:replace-with-github-token-id")
    }

    buildType(Build)
    buildType(MayhemLighttpd)
    buildType(MayhemMayhemIt)
}

object MainVcs : GitVcsRoot({
    name = "mcode-action-examples"
    url = "https://github.com/ForAllSecure/mcode-action-examples.git"
    branch = "refs/heads/main"
})

object Build : BuildType({
    name = "Build"
    description = "Build and push the Docker image used by Mayhem"

    vcs {
        root(MainVcs)
    }

    steps {
        script {
            name = "Build and push Docker image"
            scriptContent = """
                #!/usr/bin/env bash
                set -euo pipefail
                echo "%env.GITHUB_TOKEN%" | docker login %env.REGISTRY% -u %env.GITHUB_USERNAME% --password-stdin
                docker build --platform=linux/amd64 -f mayhem/Dockerfile -t %env.IMAGE%:%teamcity.build.branch% .
                docker push %env.IMAGE%:%teamcity.build.branch%
            """.trimIndent()
        }
    }

    triggers {
        vcs {}
    }
})

object MayhemLighttpd : BuildType({
    name = "Mayhem: lighttpd"

    vcs {
        root(MainVcs)
    }

    steps {
        script {
            name = "Install Mayhem CLI and log in"
            scriptContent = """
                #!/usr/bin/env bash
                set -euo pipefail
                mkdir -p ~/bin
                curl --no-progress-meter -Lo ~/bin/mayhem "%env.MAYHEM_URL%/cli/Linux/mayhem"
                chmod +x ~/bin/mayhem
                export PATH="${'$'}PATH:~/bin"
                mayhem login --url "%env.MAYHEM_URL%" --token "%env.MAYHEM_TOKEN%"
            """.trimIndent()
        }

        script {
            name = "Run Mayhem"
            scriptContent = """
                #!/usr/bin/env bash
                set -euo pipefail
                export PATH="${'$'}PATH:~/bin"

                run=${'$'}(mayhem --verbosity info run . --project mcode-action-examples --owner forallsecure-demo --image %env.IMAGE%:%teamcity.build.branch% --file mayhem/lighttpd.mayhemfile --duration 60 --branch-name "%teamcity.build.branch%" --revision "%build.vcs.number%" --ci-url "%teamcity.serverUrl%/build/%teamcity.build.id%" 2>/dev/null)
                if [ -z "${'$'}run" ]; then
                  echo "Mayhem run failed to start."
                  exit 1
                fi

                runName=${'$'}(echo "${'$'}run" | awk -F / '{ print ${'$'}(NF-1) }')
                mayhem --verbosity info wait "${'$'}run" --fail-on-defects --owner forallsecure-demo --sarif "sarif-${'$'}{runName}.sarif" --junit "junit-${'$'}{runName}.xml"

                status=${'$'}(mayhem --verbosity info show --owner forallsecure-demo --format json "${'$'}run" | jq -r '.[0].status')
                if [[ "${'$'}status" == *"stopped"* || "${'$'}status" == *"failed"* ]]; then
                  echo "Mayhem run finished with status: ${'$'}status"
                  exit 2
                fi
            """.trimIndent()
        }
    }

    triggers {
        finishBuildTrigger {
            buildType = "${Build.id}"
            successfulOnly = true
        }
    }

    dependencies {
        snapshot(Build) {}
    }

    artifactRules = "sarif-*.sarif => sarif\njunit-*.xml => junit"
})