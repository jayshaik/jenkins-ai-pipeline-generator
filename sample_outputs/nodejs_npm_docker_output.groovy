pipeline {
    agent any

    environment {
        // Set environment variables
        NODE_ENV = 'production'
        DOCKER_IMAGE = "myorg/myapp:${env.BUILD_NUMBER}"
        REGISTRY_CREDENTIALS = credentials('dockerhub-credentials') // Jenkins credentials ID
    }

    options {
        // Keep only the 10 most recent builds
        buildDiscarder(logRotator(numToKeepStr: '10'))
        // Timestamps in logs
        timestamps()
    }

    stages {
        stage('Checkout') {
            steps {
                // Clean workspace before checkout
                cleanWs()
                // Checkout source code
                checkout scm
            }
        }

        stage('Build') {
            steps {
                script {
                    // Install dependencies and build the project
                    sh 'npm ci'
                    sh 'npm run build'
                }
            }
            post {
                failure {
                    echo 'Build failed. Please check the logs.'
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    // Run tests
                    sh 'npm test'
                }
            }
            post {
                always {
                    // Archive test results if available
                    junit allowEmptyResults: true, testResults: '**/test-results.xml'
                }
                failure {
                    echo 'Tests failed. Please review the test reports.'
                }
            }
        }

        stage('Docker Build & Push') {
            when {
                // Only build Docker image on main/master branch
                branch pattern: "main|master", comparator: "REGEXP"
            }
            steps {
                script {
                    // Build Docker image
                    sh "docker build -t ${DOCKER_IMAGE} ."
                    // Login to Docker registry
                    sh "echo ${REGISTRY_CREDENTIALS_PSW} | docker login -u ${REGISTRY_CREDENTIALS_USR} --password-stdin"
                    // Push Docker image
                    sh "docker push ${DOCKER_IMAGE}"
                }
            }
            post {
                failure {
                    echo 'Docker build or push failed.'
                }
            }
        }

        stage('Deploy') {
            when {
                // Only deploy on main/master branch
                branch pattern: "main|master", comparator: "REGEXP"
            }
            steps {
                script {
                    // Example deployment: Update running container (replace with your deployment logic)
                    // This could be a kubectl apply, docker-compose up, etc.
                    echo "Deploying Docker image ${DOCKER_IMAGE} to production environment..."
                    // sh "kubectl set image deployment/myapp myapp=${DOCKER_IMAGE} --record"
                }
            }
            post {
                failure {
                    echo 'Deployment failed.'
                }
            }
        }
    }

    post {
        always {
            // Clean up workspace after build
            cleanWs()
        }
        failure {
            // Notify on failure (replace with your notification logic)
            echo 'Pipeline failed!'
        }
        success {
            echo 'Pipeline completed successfully!'
        }
    }
}