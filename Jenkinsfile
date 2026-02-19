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