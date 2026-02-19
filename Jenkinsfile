pipeline {
    agent any
    
    tools {
        // These names MUST match the names you gave in 
        // Manage Jenkins -> Tools
        jdk 'Java25'
        maven 'Maven3'
    }

    stages {
        stage('Check Java') {
            steps {
                sh 'java -version'
                sh 'mvn -version'
            }
        }
        stage('Static Analysis') {
            steps {
                // Now JAVA_HOME is automatically set for this shell
                sh 'mvn checkstyle:check pmd:check'
            }
        }
        // ... rest of your stages
        stage('Docker Build') {
            steps {
                script {
                    // Building the Order Service
                    sh "docker build -t order-service:latest ./order-service"
                    
                    // Building the Notification Service
                    sh "docker build -t notification-service:latest ./notification-service"
                }
            }
        }
    }
}