@Library ('folio_jenkins_shared_libs') _

pipeline {

  environment {
    ORG_GRADLE_PROJECT_appName = 'mod-erm'
    GRADLE_OPTS = '--console plain --no-daemon'
    BUILD_DIR = "${env.WORKSPACE}/service"
    MD = "${env.WORKSPACE}/service/build/resources/main/okapi/ModuleDescriptor.json"
  }

  options {
    timeout(30)
    buildDiscarder(logRotator(numToKeepStr: '30'))
  }

  agent {
    node {
      label 'jenkins-slave-all'
    }
  }

  stages {
    stage ('Setup') {
      steps {
        script {
          def foliociLib = new org.folio.foliociCommands()
          def gradleVersion = foliociLib.gradleProperty('appVersion')

          env.name = env.ORG_GRADLE_PROJECT_appName
        
          // if release 
          if ( foliociLib.isRelease() ) {
            // make sure git tag and version match
            if ( foliociLib.tagMatch(version) ) {
              env.isRelease = true 
              env.dockerRepo = 'folioorg'
              env.version = gradleVersion
            }
            else {
              env.dockerRepo = 'folioci'
              env.version = "${gradleVersion}-SNAPSHOT.${env.BUILD_NUMBER}
            }
          }
        }
        sendNotifications 'STARTED'  
      }
    }

    stage('Gradle Build') { 
      steps {
        dir(env.BUILD_DIR) {
          sh "./gradlew $env.GRADLE_OPTS -PappVersion=${env.version} assemble
        }
      }
    }
   
    stage('Build Docker') {
      steps {
        dir(env.BUILD_DIR) {
          sh "./gradlew $env.GRADLE_OPTS -PappVersion=${env.version} -PdockerRepo=${env.dockerRepo} buildImage"
        }
        // debug
        sh "cat $env.MD"
      } 
    }

    stage('Publish Docker Image') { 
      when { 
        anyOf {
          branch 'master'
          environment name: isRelease, value: true 
        }
      }
      steps {
        docker.withRegistry('https://index.docker.io/v1/', 'DockerHubIDJenkins') {
          sh "docker push ${env.dockerRepo}/${env.name}:${env.version}"
          sh "docker push ${env.dockerRepo}/${env.name}:latest"
        }
      }
    }

    stage('Publish Module Descriptor') {
      when {
        anyOf { 
          branch 'master'
          environment name: isRelease, value: true
        }
      }
      steps {
        script {
          def foliociLib = new org.folio.foliociCommands()
          foliociLib.updateModDescriptor(env.MD) 
        }
        postModuleDescriptor(env.MD)
      }
    }

  } // end stages

  post {
    always {
      dockerCleanup()
      sendNotifications currentBuild.result 
    }
  }
}
         

