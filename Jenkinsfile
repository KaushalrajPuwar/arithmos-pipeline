pipeline {
    agent any

    environment {
        IMAGE = 'kaushalrajpuwar/arithmos-calculator'
        NOTIFY_EMAIL = 'kaushalrajpuwar@gmail.com'
    }

    triggers {
        githubPush()
        pollSCM('H/2 * * * *')
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'git log --oneline -3'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }

        stage('Docker Build') {
            steps {
                sh '''
                    docker build -t $IMAGE:${BUILD_NUMBER} -t $IMAGE:latest .
                '''
            }
        }

        stage('Push') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-creds',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker push $IMAGE:${BUILD_NUMBER}
                        docker push $IMAGE:latest
                    '''
                }
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    docker compose up -d
                    for i in $(seq 1 24); do
                      if curl -fsS http://localhost:8081/actuator/health 2>/dev/null | grep -q '"status":"UP"'; then
                        echo "calculator is UP"
                        break
                      fi
                      echo "waiting for calculator... ($i)"
                      sleep 5
                    done
                    curl -fsS http://localhost:8081/actuator/health | grep -q '"status":"UP"'
                    curl -fsS "http://localhost:8082/api/v1/add?a=2&b=3" | grep -q '"result":5.0'
                    echo "deploy verified via app port and Nginx proxy"
                    docker compose ps
                '''
            }
        }
    }

    post {
        always {
            junit 'target/surefire-reports/*.xml'
        }
        success {
            mail to: "${NOTIFY_EMAIL}",
                subject: "Arithmos pipeline #${BUILD_NUMBER} SUCCESS",
                body: """Arithmos Calculator pipeline build #${BUILD_NUMBER} SUCCEEDED.

Details: ${BUILD_URL}
Image: ${IMAGE}:${BUILD_NUMBER} (and :latest)
Deployed stack verified via /actuator/health and Nginx proxy."""
        }
        failure {
            mail to: "${NOTIFY_EMAIL}",
                subject: "Arithmos pipeline #${BUILD_NUMBER} FAILED",
                body: """Arithmos Calculator pipeline build #${BUILD_NUMBER} FAILED.

Check console output: ${BUILD_URL}console
"""
        }
    }
}
