pipeline {
    // El nodo seleccionado debe ejecutar Windows y tener Git, Docker, Java y Node.js en PATH.
    agent any

    options {
        skipDefaultCheckout(true)
        disableConcurrentBuilds()
        timestamps()
        timeout(time: 20, unit: 'MINUTES')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                bat '''@echo off
                    echo Commit construido:
                    git log -1 --pretty=format:"Hash: %%H%%nAutor: %%an%%nFecha: %%ad%%nMensaje: %%s%%n"
                '''
            }
        }

        stage('Backend Test') {
            steps {
                dir('backend') {
                    bat 'mvnw.cmd test'
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'backend/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Frontend Validation') {
            steps {
                dir('frontend') {
                    bat '''@echo off
                        call npm.cmd ci
                        if errorlevel 1 exit /b %errorlevel%
                        call npm.cmd run test:ci
                        if errorlevel 1 exit /b %errorlevel%
                        call npm.cmd run build
                    '''
                }
            }
        }

        stage('Configurar entorno Docker') {
            when {
                anyOf {
                    branch 'dev'
                    branch 'main'
                }
            }
            steps {
                script {
                    def isProd = env.BRANCH_NAME == 'main'
                    def envFile = isProd ? 'backend\\.env.prod' : 'backend\\.env.dev'
                    def mysqlDatabase = isProd ? 'noteapp_prod' : 'noteapp_dev'
                    def mysqlHostPort = isProd ? '3308' : '3307'
                    def backendHostPort = isProd ? '8082' : '8080'
                    def frontendHostPort = isProd ? '5174' : '5173'
                    def corsOrigins = "http://localhost:${frontendHostPort}"
                    def jwtCredentialId = isProd ? 'noteapp-prod-jwt-secret' : 'noteapp-dev-jwt-secret'
                    def adminCredentialId = isProd ? 'noteapp-prod-admin-password' : 'noteapp-dev-admin-password'
                    def mysqlCredentialId = isProd ? 'noteapp-prod-mysql-root-password' : 'noteapp-dev-mysql-root-password'

                    withCredentials([
                        string(credentialsId: jwtCredentialId, variable: 'APP_JWT_SECRET'),
                        string(credentialsId: adminCredentialId, variable: 'APP_ADMIN_PASSWORD'),
                        string(credentialsId: mysqlCredentialId, variable: 'MYSQL_ROOT_PASSWORD')
                    ]) {
                        powershell """
                            \$ErrorActionPreference = 'Stop'
                            \$lines = @(
                                "APP_JWT_SECRET=\$env:APP_JWT_SECRET",
                                "MYSQL_ROOT_PASSWORD=\$env:MYSQL_ROOT_PASSWORD",
                                "MYSQL_DATABASE=${mysqlDatabase}",
                                "MYSQL_HOST_PORT=${mysqlHostPort}",
                                "BACKEND_HOST_PORT=${backendHostPort}",
                                "FRONTEND_HOST_PORT=${frontendHostPort}",
                                "APP_CORS_ALLOWED_ORIGINS=${corsOrigins}",
                                "APP_ADMIN_PASSWORD=\$env:APP_ADMIN_PASSWORD"
                            )
                            \$utf8NoBom = New-Object System.Text.UTF8Encoding(\$false)
                            [System.IO.File]::WriteAllText("${envFile}", (\$lines -join "`n") + "`n", \$utf8NoBom)
                            Write-Host "Archivo de entorno Docker generado para ${env.BRANCH_NAME}."
                        """
                    }
                }
            }
        }

        stage('Docker Build DEV') {
            when {
                branch 'dev'
            }
            steps {
                bat 'docker compose --env-file backend/.env.dev -p noteapp-dev -f backend/docker-compose.yaml -f frontend/docker-compose.yml build'
            }
        }

        stage('Stop Previous Version DEV') {
            when {
                branch 'dev'
            }
            steps {
                // No usa -v, por lo que conserva los datos de MySQL.
                bat(returnStatus: true, script: 'docker compose --env-file backend/.env.dev -p noteapp-dev -f backend/docker-compose.yaml -f frontend/docker-compose.yml down')
            }
        }

        stage('Deploy DEV') {
            when {
                branch 'dev'
            }
            steps {
                bat 'docker compose --env-file backend/.env.dev -p noteapp-dev -f backend/docker-compose.yaml -f frontend/docker-compose.yml up --no-build -d'
            }
        }

        stage('Health Check DEV') {
            when {
                branch 'dev'
            }
            steps {
                powershell '''
                    $ErrorActionPreference = 'Stop'
                    $compose = @('--env-file', 'backend/.env.dev', '-p', 'noteapp-dev', '-f', 'backend/docker-compose.yaml', '-f', 'frontend/docker-compose.yml')

                    docker compose @compose ps
                    if ($LASTEXITCODE -ne 0) { throw 'No se pudo consultar el stack DEV.' }

                    $running = @(docker compose @compose ps --services --status running)
                    foreach ($service in @('db', 'backend', 'frontend')) {
                        if ($running -notcontains $service) {
                            docker compose @compose logs --tail=100
                            throw "El servicio DEV $service no está ejecutándose."
                        }
                    }

                    $dbContainer = docker compose @compose ps -q db
                    if ([string]::IsNullOrWhiteSpace($dbContainer)) {
                        docker compose @compose logs --tail=100 db
                        throw 'No se pudo identificar el contenedor MySQL DEV.'
                    }

                    $healthy = $false
                    for ($attempt = 1; $attempt -le 30; $attempt++) {
                        $status = docker inspect --format='{{.State.Health.Status}}' $dbContainer 2>$null
                        if ($status -eq 'healthy') { $healthy = $true; break }
                        Start-Sleep -Seconds 2
                    }
                    if (-not $healthy) {
                        docker compose @compose logs --tail=100 db
                        throw 'MySQL DEV no alcanzó el estado healthy.'
                    }

                    $backendReady = $false
                    for ($attempt = 1; $attempt -le 30; $attempt++) {
                        try {
                            Invoke-WebRequest -Uri 'http://localhost:8080/actuator/health' -UseBasicParsing -TimeoutSec 10 | Out-Null
                            $backendReady = $true
                            break
                        } catch {
                            Start-Sleep -Seconds 3
                        }
                    }
                    if (-not $backendReady) {
                        docker compose @compose logs --tail=100 backend
                        throw 'El backend DEV no respondió correctamente por HTTP.'
                    }

                    $frontendReady = $false
                    for ($attempt = 1; $attempt -le 20; $attempt++) {
                        try {
                            Invoke-WebRequest -Uri 'http://localhost:5173/' -UseBasicParsing -TimeoutSec 10 | Out-Null
                            $frontendReady = $true
                            break
                        } catch {
                            Start-Sleep -Seconds 3
                        }
                    }
                    if (-not $frontendReady) {
                        docker compose @compose logs --tail=100 frontend
                        throw 'El frontend DEV no respondió correctamente.'
                    }

                    Write-Host 'Health check DEV de NoteApp completado correctamente.'
                '''
            }
        }

        stage('Docker Build PROD') {
            when {
                branch 'main'
            }
            steps {
                bat 'docker compose --env-file backend/.env.prod -p noteapp-prod -f backend/docker-compose.yaml -f frontend/docker-compose.yml build'
            }
        }

        stage('Stop Previous Version PROD') {
            when {
                branch 'main'
            }
            steps {
                // No usa -v, por lo que conserva los datos de MySQL.
                bat(returnStatus: true, script: 'docker compose --env-file backend/.env.prod -p noteapp-prod -f backend/docker-compose.yaml -f frontend/docker-compose.yml down')
            }
        }

        stage('Deploy PROD') {
            when {
                branch 'main'
            }
            steps {
                bat 'docker compose --env-file backend/.env.prod -p noteapp-prod -f backend/docker-compose.yaml -f frontend/docker-compose.yml up --no-build -d'
            }
        }

        stage('Health Check PROD') {
            when {
                branch 'main'
            }
            steps {
                powershell '''
                    $ErrorActionPreference = 'Stop'
                    $compose = @('--env-file', 'backend/.env.prod', '-p', 'noteapp-prod', '-f', 'backend/docker-compose.yaml', '-f', 'frontend/docker-compose.yml')

                    docker compose @compose ps
                    if ($LASTEXITCODE -ne 0) { throw 'No se pudo consultar el stack PROD.' }

                    $running = @(docker compose @compose ps --services --status running)
                    foreach ($service in @('db', 'backend', 'frontend')) {
                        if ($running -notcontains $service) {
                            docker compose @compose logs --tail=100
                            throw "El servicio PROD $service no está ejecutándose."
                        }
                    }

                    $dbContainer = docker compose @compose ps -q db
                    if ([string]::IsNullOrWhiteSpace($dbContainer)) {
                        docker compose @compose logs --tail=100 db
                        throw 'No se pudo identificar el contenedor MySQL PROD.'
                    }

                    $healthy = $false
                    for ($attempt = 1; $attempt -le 30; $attempt++) {
                        $status = docker inspect --format='{{.State.Health.Status}}' $dbContainer 2>$null
                        if ($status -eq 'healthy') { $healthy = $true; break }
                        Start-Sleep -Seconds 2
                    }
                    if (-not $healthy) {
                        docker compose @compose logs --tail=100 db
                        throw 'MySQL PROD no alcanzó el estado healthy.'
                    }

                    $backendReady = $false
                    for ($attempt = 1; $attempt -le 30; $attempt++) {
                        try {
                            Invoke-WebRequest -Uri 'http://localhost:8082/actuator/health' -UseBasicParsing -TimeoutSec 10 | Out-Null
                            $backendReady = $true
                            break
                        } catch {
                            Start-Sleep -Seconds 3
                        }
                    }
                    if (-not $backendReady) {
                        docker compose @compose logs --tail=100 backend
                        throw 'El backend PROD no respondió correctamente por HTTP.'
                    }

                    $frontendReady = $false
                    for ($attempt = 1; $attempt -le 20; $attempt++) {
                        try {
                            Invoke-WebRequest -Uri 'http://localhost:5174/' -UseBasicParsing -TimeoutSec 10 | Out-Null
                            $frontendReady = $true
                            break
                        } catch {
                            Start-Sleep -Seconds 3
                        }
                    }
                    if (-not $frontendReady) {
                        docker compose @compose logs --tail=100 frontend
                        throw 'El frontend PROD no respondió correctamente.'
                    }

                    Write-Host 'Health check PROD de NoteApp completado correctamente.'
                '''
            }
        }
    }

    post {
        success {
            script {
                if (env.BRANCH_NAME == 'dev') {
                    echo 'NoteApp fue validada y desplegada correctamente en DEV.'
                } else if (env.BRANCH_NAME == 'main') {
                    echo 'NoteApp fue validada y desplegada correctamente en PROD.'
                } else {
                    echo 'Rama validada correctamente. No se realizó deploy.'
                }
            }
        }
        failure {
            echo 'El pipeline de NoteApp falló durante una etapa de validación, construcción, despliegue o health check.'
        }
        always {
            script {
                if (env.BRANCH_NAME == 'dev') {
                    bat(returnStatus: true, script: 'docker compose --env-file backend/.env.dev -p noteapp-dev -f backend/docker-compose.yaml -f frontend/docker-compose.yml ps')
                } else if (env.BRANCH_NAME == 'main') {
                    bat(returnStatus: true, script: 'docker compose --env-file backend/.env.prod -p noteapp-prod -f backend/docker-compose.yaml -f frontend/docker-compose.yml ps')
                } else {
                    bat(returnStatus: true, script: 'docker ps')
                }
            }
        }
    }
}
