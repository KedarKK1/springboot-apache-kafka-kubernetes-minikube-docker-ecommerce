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
        stage('Debug Environment') {
            steps {
                script {
                    def mHome = tool 'Maven3'
                    echo "MHome is: ${mHome}"
                    sh "ls -la ${mHome}/bin || echo 'Maven Bin folder not found'"
                    sh "env | sort"
                }
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
                    // recordCoverage(tools: [[parser: 'JOCOCO']])
                    // ! deprecated above method have no plugins available in jenkins

                    // This captures Checkstyle, PMD, and SpotBugs reports
                    recordIssues(tools: [checkStyle(), pmdParser()])
                    
                    // Use the standard jacoco step instead of recordCoverage
                    jacoco(
                        execPattern: '**/target/*.exec',
                        classPattern: '**/target/classes',
                        sourcePattern: '**/src/main/java',
                        exclusionPattern: '**/src/test/**'
                    )
                }
            }
        }

        stage('Dockerize &  Build'){
            steps{
                script{
                    // Variables must be inside a script block
                    // This 'maven' name must match what you set in Manage Jenkins -> Tools
                    def mavenHome = tool 'Maven3'

                    dir('order-service') {
                        // // Give execute permission to the wrapper first
                        // sh 'chmod +x ../mvnw'
                        // Run it
                        // 1. Build the JAR first!
                        sh '${mavenHome}/bin/mvn clean package -DskipTests' 
                        
                        // 2. Now build the image
                        sh 'docker build -t order-service:latest .'
                    }
                    
                    dir('notification-service') {
                        // sh 'chmod +x ../mvnw'
                        sh '${mavenHome}/bin/mvn clean package -DskipTests'
                        sh 'docker build -t notification-service:latest .'
                    }
                    //     sh 'docker build -t order-service:latest ./order-service'
                    //     sh 'docker build -t notification-service:latest ./notification-service'
                }
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