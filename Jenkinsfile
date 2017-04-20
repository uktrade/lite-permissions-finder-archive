node('jdk8') {
  currentBuild.displayName = "#${env.BUILD_NUMBER} - ${params.BUILD_VERSION}"

  def serviceName = 'permissions-finder'
  def gitURL = "github.com/BISDigital/lite-${serviceName}"

  stage('Clean workspace'){
    deleteDir()
  }
  stage('Checkout files'){
    checkout scm
  }
  stage('SBT publish'){
    sh 'sbt publish'
  }
  stage('Tag build'){
    withCredentials([usernamePassword(credentialsId: 'LITE-bot-github', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
      sh("git -c 'user.name=Jenkins' -c 'user.email=jenkins@digital' tag  -a ${params.BUILD_VERSION} -m 'Jenkins'")
      sh("git push https://${env.GIT_USERNAME}:${env.GIT_PASSWORD}@${gitURL} --tags")
    }
  }
  stage('Docker build'){
    build job: 'new-docker-build', parameters: [
      [$class: 'StringParameterValue', name: 'SERVICE_NAME', value: serviceName],
      [$class: 'StringParameterValue', name: 'BUILD_VERSION', value: params.BUILD_VERSION],
      [$class: 'StringParameterValue', name: 'DOCKERFILE_PATH', value: '.']
    ]
  }
  stage('Dev deploy'){
    build job: 'new-release-job', parameters: [
      [$class: 'StringParameterValue', name: 'IMAGE_NAME', value: 'app/permissions-finder'],
      [$class: 'StringParameterValue', name: 'BUILD_VERSION', value: params.BUILD_VERSION],
      [$class: 'StringParameterValue', name: 'TARGET_ENVIRONMENT', value: 'dev'],
      [$class: 'BooleanParameterValue', name: 'COMMENT_REFERENCED_JIRAS', value: true]
    ]
  }
}