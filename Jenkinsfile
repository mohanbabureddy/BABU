pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/mohanbabureddy/BABU.git'
            }
        }

        stage('Build') {
            steps {
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('Docker Build & Push') {
            steps {
                script {
                    dockerImage = docker.build("mohanbabureddy/rentapp-backend")
                    docker.withRegistry('', 'docker-hub-credentials') {
                        dockerImage.push("latest")
                    }
                }
            }
        }

        stage('Deploy to AWS EC2') {
            steps {
                sshagent(['ec2-ssh-key']) {
                    sh '''
                    ssh ec2-user@<http://13.53.193.78/> '
                    docker stop rentapp-backend || true && docker rm rentapp-backend || true
                    docker pull mohanbabureddy/rentapp-backend:latest
                    docker run -d --name rentapp-backend -p 8080:8080 mohanbabureddy/rentapp-backend
                    '
                    '''
                }
            }
        }
    }
}
