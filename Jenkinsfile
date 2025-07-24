pipeline {
    agent any

    environment {
        GIT_REPO = 'https://github.com/mohanbabureddy/BABU.git' // backend repo
        GIT_CREDENTIALS_ID = 'github-creds'
        IMAGE_NAME = 'rentapp-backend'
        CONTAINER_NAME = 'rentapp-backend'
        PORT = '8080'
    }

    stages {
        stage('Clone') {
            steps {
                git credentialsId: "${GIT_CREDENTIALS_ID}", url: "${GIT_REPO}"
            }
        }

        stage('Build JAR') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }


        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${IMAGE_NAME} ."
            }
        }

        stage('Stop Old Container') {
            steps {
                sh "docker stop ${CONTAINER_NAME} || true"
                sh "docker rm ${CONTAINER_NAME} || true"
            }
        }

        stage('Run New Container') {
            steps {
                sh "docker run -d -p ${PORT}:${PORT} --name ${CONTAINER_NAME} ${IMAGE_NAME}"
            }
        }
    }

    post {
        success {
            echo '✅ Backend deployed successfully!'
        }
        failure {
            echo '❌ Deployment failed.'
        }
    }
}
