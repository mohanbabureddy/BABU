pipeline {
    agent any

    environment {
        GIT_REPO = 'https://github.com/mohanbabureddy/BABU.git'
        GIT_CREDENTIALS_ID = 'github-creds'  // Replace if you named your credential differently
        DOCKER_IMAGE = 'rentapp-backend'
        JAR_NAME = 'tenant-billing-0.0.1-SNAPSHOT.jar'
    }

    stages {
        stage('Clone Repo') {
            steps {
                git credentialsId: "${GIT_CREDENTIALS_ID}", url: "${GIT_REPO}"
            }
        }

        stage('Build App') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${DOCKER_IMAGE} ."
            }
        }

        stage('Stop Old Container') {
            steps {
                sh 'docker stop rentapp-container || true'
                sh 'docker rm rentapp-container || true'
            }
        }

        stage('Run New Container') {
            steps {
                sh "docker run -d -p 8080:8080 --name rentapp-container ${DOCKER_IMAGE}"
            }
        }
    }

    post {
        success {
            echo '✅ Backend deployed successfully!'
        }
        failure {
            echo '❌ Build or Deployment failed.'
        }
    }
}
