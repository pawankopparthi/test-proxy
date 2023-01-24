def call(){
node ("master") {
       stage('access-token') {
	         withCredentials([file(credentialsId: params.orgn, variable: 'serviceAccount')]) {
                            script {
				    
                               git branch: 'master', credentialsId: 'newgithubid', url: 'https://github.hdfcbankuat.com/ALCMAPIGEEUAT/token-repo.git'
				   
                            sh '''
                            ls -la
                            
                            sed -i -e 's/\r$//'  get-access-token.sh
                            sed -i -e 's/\r$//'  create-jwt-token.sh
                            access_token=$(sh get-access-token.sh ${serviceAccount} "https://www.googleapis.com/auth/cloud-platform"  "http://172.23.3.103:3128")
                            echo "${access_token}"  >> token1
                            cut -b 1-232 token1 > token
                            cat token
                            
                            
                            
                            '''
			    }  
                           // echo "${access_token}"
                            //env.access = access_token
                            //echo "${env.access}"
                    }
                  }
				  def token = readFile"${env.WORKSPACE}/token"
		                def bearer = readFile"${env.WORKSPACE}/token"
        deleteDir()
   withMaven(globalMavenSettingsConfig: 'jfrog2', maven: 'maven') {    
            stage('Checkout') {
                git credentialsId: 'newgithubid', url: 'https://github.hdfcbankuat.com/ALCMAPIGEEUAT/hdfc-env_org-configurations.git'
                sh "ls -la"
                sh "git checkout ${orgn}"
            }
            dir('edge') {
            stage('package'){
            sh "mvn package"
            }
                        stage('env-org') {
                            withCredentials([file(credentialsId: params.orgn, variable: 'file')]) {
                                echo params.orgn
                                sh'''
                                echo "${type},${orgn},${envt}"
                                '''
                                    sh"mvn -X apigee-config:${type} -Dapigee.config.options=${operation} -Phybrid-apiproxy -Dorg=${orgn} -Denv=${envt} -Dbearer=${token}"   
                            }
                        }    
            }
      }
  }   
}
