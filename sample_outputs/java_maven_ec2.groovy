pipeline {
    agent any

    environment {
        // Define environment variables
        MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'
        // Docker image name (if applicable)
        IMAGE_NAME = "my-java-app:${env.BUILD_NUMBER}"
        // AWS credentials and region
        AWS_REGION = 'us-east-1'
        EC2_USER = 'ec2-user'
        EC2_HOST = 'your.ec2.public.ip'
        // Path to PEM key stored as Jenkins secret file credential
        SSH_KEY = credentials('ec2-ssh-key')
    }

    options {
        // Keep only the last 10 builds
        buildDiscarder(logRotator(numToKeepStr: '10'))
        // Fail the build if any stage fails
        disableConcurrentBuilds()
        timeout(time: 30, unit: 'MINUTES')
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
                    // Build the project with Maven
                    sh 'mvn clean package -DskipTests=false'
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
                    sh 'mvn test'
                }
            }
            post {
                always {
                    // Archive test reports even if tests fail
                    junit '**/target/surefire-reports/*.xml'
                }
                failure {
                    echo 'Tests failed. Please check the test reports.'
                }
            }
        }

        stage('Docker Build') {
            when {
                // Only run if Dockerfile exists
                expression { fileExists('Dockerfile') }
            }
            steps {
                script {
                    // Build Docker image
                    sh "docker build -t ${IMAGE_NAME} ."
                }
            }
            post {
                failure {
                    echo 'Docker build failed.'
                }
            }
        }

        stage('Deployment') {
            steps {
                script {
                    // Deploy to EC2 via SSH
                    // Copy artifact or Docker image as needed
                    // Example: Copy JAR file and restart service
                    def jarFile = sh(script: "ls target/*.jar | head -n 1", returnStdout: true).trim()
                    // Copy JAR to EC2
                    sh """
                        scp -i ${SSH_KEY} -o StrictHostKeyChecking=no ${jarFile} ${EC2_USER}@${EC2_HOST}:/home/${EC2_USER}/app.jar
                    """
                    // Restart application on EC2 (assumes systemd service named 'myapp')
                    sh """
                        ssh -i ${SSH_KEY} -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} '
                            sudo systemctl stop myapp || true
                            sudo cp /home/${EC2_USER}/app.jar /opt/myapp/app.jar
                            sudo systemctl start myapp
                        '
                    """
                }
            }
            post {
                success {
                    echo 'Deployment succeeded!'
                }
                failure {
                    echo 'Deployment failed. Please check EC2 logs.'
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
            mail to: 'devops-team@example.com',
                 subject: "Jenkins Build Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "Check Jenkins for details: ${env.BUILD_URL}"
        }
    }
}