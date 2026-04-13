/*
 * Production-Ready CI/CD Pipeline — graphql-backend
 *
 * Required Jenkins plugins:
 *   - Docker Pipeline
 *   - Pipeline Utility Steps
 *   - JUnit
 *   - HTML Publisher
 *   - SonarQube Scanner (optional — skip if not used)
 *   - OWASP Dependency-Check (optional — skip if not used)
 *   - Slack Notification (optional)
 *
 * Required Jenkins credentials:
 *   - docker-registry-credentials : Username/Password  (container registry login)
 *   - sonar-token                 : Secret Text        (SonarQube token, optional)
 *   - k8s-staging-kubeconfig      : Secret File        (staging cluster kubeconfig)
 *   - k8s-prod-kubeconfig         : Secret File        (production cluster kubeconfig)
 *   - slack-token                 : Secret Text        (Slack Bot token, optional)
 *
 * Required Jenkins global config:
 *   - SonarQube server named "SonarQube" in Manage Jenkins → Configure System (optional)
 *   - Slack workspace configured via Slack Notification plugin (optional)
 *
 * Required Gradle plugins (add to build.gradle.kts if using SonarQube / OWASP):
 *   - id("org.sonarqube") version "..."
 *   - id("org.owasp.dependencycheck") version "..."
 */

pipeline {

    // ─── Agent ──────────────────────────────────────────────────────────────
    // Eclipse Temurin JDK 25 inside Docker.
    // Docker socket is mounted so Testcontainers and bootBuildImage can reach
    // the host Docker daemon.  The Gradle cache is persisted between builds.
    agent {
        docker {
            image 'eclipse-temurin:25-jdk-jammy'
            args  '''-v /var/run/docker.sock:/var/run/docker.sock
                     -v $HOME/.gradle:/root/.gradle
                     -e TESTCONTAINERS_RYUK_DISABLED=true
                     -e DOCKER_HOST=unix:///var/run/docker.sock'''
        }
    }

    // ─── Global environment ──────────────────────────────────────────────────
    environment {
        APP_NAME       = 'graphql-backend'
        DOCKER_REGISTRY = 'registry.example.com'          // ← replace with your registry
        IMAGE_NAME      = "${DOCKER_REGISTRY}/${APP_NAME}"

        // Gradle: disable daemon inside CI, enable local build cache
        GRADLE_OPTS    = '-Dorg.gradle.daemon=false -Dorg.gradle.caching=true'

        // SonarQube server name (matches Jenkins global config)
        SONAR_HOST_URL = 'http://sonarqube:9000'          // ← replace with your SonarQube URL

        // Slack channel for build notifications
        SLACK_CHANNEL  = '#ci-notifications'              // ← replace with your channel
    }

    // ─── Triggers ────────────────────────────────────────────────────────────
    // Run integration tests automatically every weekday at 9:00 AM EST (14:00 UTC).
    // On a scheduled run the manual confirmation stage is skipped automatically.
    triggers {
        cron('TZ=America/New_York\n0 9 * * 1-5')
    }

    // ─── Pipeline options ────────────────────────────────────────────────────
    options {
        buildDiscarder(logRotator(numToKeepStr: '15', artifactNumToKeepStr: '5'))
        timeout(time: 60, unit: 'MINUTES')
        disableConcurrentBuilds(abortPrevious: true)
        timestamps()
    }

    // ─── Stages ─────────────────────────────────────────────────────────────
    stages {

        // ── 1. Prepare ───────────────────────────────────────────────────────
        stage('Prepare') {
            steps {
                script {
                    env.GIT_COMMIT_SHORT = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    env.IMAGE_TAG        = "${env.GIT_COMMIT_SHORT}-${env.BUILD_NUMBER}"
                    env.FULL_IMAGE       = "${env.IMAGE_NAME}:${env.IMAGE_TAG}"
                    env.BRANCH_SLUG      = env.BRANCH_NAME.replaceAll('[^a-zA-Z0-9._-]', '-').toLowerCase()
                }
                sh './gradlew --version'
                echo "Branch : ${env.BRANCH_NAME}"
                echo "Commit : ${env.GIT_COMMIT_SHORT}"
                echo "Image  : ${env.FULL_IMAGE}"
            }
        }

        // ── 2. Build ─────────────────────────────────────────────────────────
        stage('Build') {
            steps {
                sh './gradlew classes testClasses --no-daemon'
            }
            post {
                failure {
                    archiveArtifacts artifacts: 'build/reports/**', allowEmptyArchive: true
                }
            }
        }

        // ── 3a. Unit Tests ───────────────────────────────────────────────────
        // Runs on every branch — no Docker, no Testcontainers required.
        // Only classes NOT annotated with @Tag("integration") are executed.
        stage('Unit Tests') {
            steps {
                sh './gradlew test --no-daemon'
            }
            post {
                always {
                    junit testResults: 'build/test-results/test/**/*.xml',
                          allowEmptyResults: true
                    publishHTML([
                        allowMissing         : true,
                        alwaysLinkToLastBuild: true,
                        keepAll              : true,
                        reportDir            : 'build/reports/tests/test',
                        reportFiles          : 'index.html',
                        reportName           : 'Unit Test Report'
                    ])
                }
            }
        }

        // ── 3b. Confirm: Integration Tests ───────────────────────────────────
        // Skipped automatically when triggered by the 9am EST cron schedule.
        // On push-triggered builds a human must approve before Testcontainers
        // spins up a real PostgreSQL instance.
        stage('Confirm: Run Integration Tests') {
            when {
                allOf {
                    not { triggeredBy 'TimerTrigger' }
                    anyOf {
                        branch 'main'
                        branch 'develop'
                        branch pattern: 'release/.+', comparator: 'REGEXP'
                    }
                }
            }
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    input(
                        message  : 'Run integration tests against a live database?',
                        ok       : 'Run Tests',
                        submitter: 'developers,release-managers'
                    )
                }
            }
        }

        // ── 3c. Integration Tests ─────────────────────────────────────────────
        // Testcontainers spins up a real PostgreSQL container.
        // Only classes annotated with @Tag("integration") are executed.
        // Skipped entirely on feature branches.
        stage('Integration Tests') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    branch pattern: 'release/.+', comparator: 'REGEXP'
                    branch pattern: 'hotfix/.+',  comparator: 'REGEXP'
                }
            }
            steps {
                sh './gradlew integrationTest --no-daemon'
            }
            post {
                always {
                    junit testResults: 'build/test-results/integrationTest/**/*.xml',
                          allowEmptyResults: true
                    publishHTML([
                        allowMissing         : true,
                        alwaysLinkToLastBuild: true,
                        keepAll              : true,
                        reportDir            : 'build/reports/tests/integrationTest',
                        reportFiles          : 'index.html',
                        reportName           : 'Integration Test Report'
                    ])
                }
            }
        }

        // ── 4. Code Quality (parallel) ───────────────────────────────────────
        stage('Code Quality') {
            parallel {
                // OWASP Dependency Check — requires 'org.owasp.dependencycheck' plugin
                stage('Dependency Vulnerability Scan') {
                    steps {
                        sh './gradlew dependencyCheckAnalyze --no-daemon'
                    }
                    post {
                        always {
                            dependencyCheckPublisher(
                                pattern             : 'build/reports/dependency-check-report.xml',
                                failedTotalCritical : 0,
                                failedTotalHigh     : 0,
                                unstableTotalMedium : 5
                            )
                        }
                    }
                }
            }
        }

        // ── 6. Build Container Image ─────────────────────────────────────────
        // Uses Spring Boot's Cloud Native Buildpacks integration (no Dockerfile needed).
        // Runs only for branches that should be deployed.
        stage('Build Image') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    branch pattern: 'release/.+', comparator: 'REGEXP'
                    branch pattern: 'hotfix/.+',  comparator: 'REGEXP'
                }
            }
            steps {
                sh """
                    ./gradlew bootBuildImage \
                        --imageName=${FULL_IMAGE} \
                        --no-daemon
                """
                // Tag with branch slug for human-readable reference
                sh "docker tag ${FULL_IMAGE} ${IMAGE_NAME}:${BRANCH_SLUG}"
            }
        }

        // ── 7. Push Image ────────────────────────────────────────────────────
        stage('Push Image') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    branch pattern: 'release/.+', comparator: 'REGEXP'
                    branch pattern: 'hotfix/.+',  comparator: 'REGEXP'
                }
            }
            steps {
                withCredentials([usernamePassword(
                    credentialsId  : 'docker-registry-credentials',
                    usernameVariable: 'REGISTRY_USER',
                    passwordVariable: 'REGISTRY_PASS'
                )]) {
                    sh 'echo "$REGISTRY_PASS" | docker login -u "$REGISTRY_USER" --password-stdin $DOCKER_REGISTRY'
                    sh 'docker push $FULL_IMAGE'
                    sh 'docker push $IMAGE_NAME:$BRANCH_SLUG'
                    script {
                        // Tag :latest only from main
                        if (env.BRANCH_NAME == 'main') {
                            sh 'docker tag $FULL_IMAGE $IMAGE_NAME:latest'
                            sh 'docker push $IMAGE_NAME:latest'
                        }
                    }
                }
            }
            post {
                always {
                    sh 'docker logout $DOCKER_REGISTRY || true'
                }
            }
        }
    }

    // ─── Post actions ────────────────────────────────────────────────────────
    post {
        always {
            // Remove images built during this run to free disk space
            sh "docker rmi ${env.FULL_IMAGE} ${env.IMAGE_NAME}:${env.BRANCH_SLUG} || true"
            sh 'docker image prune -f || true'
            cleanWs()
        }
    }
}
