pipeline{
    agent any

    tools{
        // These names MUST match the names you gave in
        // Manage Jenkins -> Tools
        jdk 'Java25'
        maven 'Maven3'
    }

    stages{
        stage('Checkout'){
            steps{
                // Git URL
                // This clears the directory and does a fresh clone every time
                cleanWs()
                checkout scmGit(
                    branches: [[name: '*/main']],
                    userRemoteConfigs: [[url: 'https://github.com/KedarKK1/springboot-apache-kafka-kubernetes-minikube-docker-ecommerce.git']]
                )
            }
        }
        stage('Check Java') {
            steps {
                sh 'java -version'
                sh 'mvn -version'
            }
        }
        stage('Static Analysis'){
            steps{
                // Checkstyle, PMD, and Spotbugs
                // Use 'dir' to change context to where the pom.xml actually lives
                dir('order-service') {
                    sh 'mvn checkstyle:check pmd:check'
                }
                dir('notification-service') {
                    sh 'mvn checkstyle:check pmd:check'
                }
            }
        }
        stage('Test & Coverage'){
            steps{
                // Run unit tests and generate JoCoCo report
                sh 'java -version'
            }
            post{
                always{
                    // Publishes the JaCoCo report in Jenkins UI
                    recordCoverage(tools: [[parser: 'JOCOCO']])
                }
            }
        }

        stage('Docker Build'){
            steps{
                sh 'docker build -t order-service:latest ./order-service'
                sh 'docker build -t notification-service:latest ./notification-service'
            }
        }

        stage('Deploy to Minikube'){
            steps{
                sh 'kubectl apply -f order-k8s.yaml'
                sh 'kubectl apply -f notification-k8s.yaml'
            }
        }

    }
}