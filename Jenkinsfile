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
                    echo 'No se encontró un script de tests; se validará el build de producción.'
                    bat '''@echo off
                        call npm.cmd ci
                        if errorlevel 1 exit /b %errorlevel%
                        call npm.cmd run build
                    '''
                }
            }
        }

        stage('Docker Build') {
            steps {
                bat 'docker compose -f backend/docker-compose.yaml -f frontend/docker-compose.yml build'
            }
        }

        stage('Stop Previous Version') {
            steps {
                // No usa -v, por lo que conserva los datos de MySQL.
                bat(returnStatus: true, script: 'docker compose -f backend/docker-compose.yaml -f frontend/docker-compose.yml down')
            }
        }

        stage('Deploy') {
            steps {
                bat 'docker compose -f backend/docker-compose.yaml -f frontend/docker-compose.yml up --build -d'
            }
        }

        stage('Health Check') {
            steps {
                powershell '''
                    $ErrorActionPreference = 'Stop'
                    $compose = @('-f', 'backend/docker-compose.yaml', '-f', 'frontend/docker-compose.yml')

                    docker compose @compose ps
                    if ($LASTEXITCODE -ne 0) { throw 'No se pudo consultar el stack.' }

                    $running = @(docker compose @compose ps --services --status running)
                    foreach ($service in @('db', 'backend', 'frontend')) {
                        if ($running -notcontains $service) {
                            docker compose @compose logs --tail=100
                            throw "El servicio $service no está ejecutándose."
                        }
                    }

                    $healthy = $false
                    for ($attempt = 1; $attempt -le 30; $attempt++) {
                        $status = docker inspect --format='{{.State.Health.Status}}' noteapp-db 2>$null
                        if ($status -eq 'healthy') { $healthy = $true; break }
                        Start-Sleep -Seconds 2
                    }
                    if (-not $healthy) {
                        docker compose @compose logs --tail=100 db
                        throw 'MySQL no alcanzó el estado healthy.'
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
                        docker compose @compose logs --tail=100
                        throw 'El frontend no respondió correctamente.'
                    }

                    Write-Host 'Health check de NoteApp completado correctamente.'
                '''
            }
        }
    }

    post {
        success {
            echo 'NoteApp fue validada y desplegada correctamente.'
        }
        failure {
            echo 'El pipeline de NoteApp falló durante una etapa de validación, construcción, despliegue o health check.'
        }
        always {
            script {
                def composeStatus = bat(
                    returnStatus: true,
                    script: 'docker compose -f backend/docker-compose.yaml -f frontend/docker-compose.yml ps'
                )
                if (composeStatus != 0) {
                    bat(returnStatus: true, script: 'docker ps')
                }
            }
        }
    }
}
