pipeline {
    agent any

    triggers {
            pollSCM('* * * * *')
    }

    environment {
        APPLICATION_NAME = 'liqui'
    }

    stages {
        stage('Build') {
            steps {
                sh './mvnw clean verify'
            }
        }
    }
}
