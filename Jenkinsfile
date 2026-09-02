pipeline {
    agent any

    options {
        // El stack usa puertos y nombres de contenedor fijos; no debe desplegarse en paralelo.
        skipDefaultCheckout(true)
        disableConcurrentBuilds()
        timestamps()
        timeout(time: 20, unit: 'MINUTES')
    }

    environment {
        COMPOSE_FILES = '-f backend/docker-compose.yaml -f frontend/docker-compose.yml'
    }

    stages {
        stage('Checkout') {
            steps {
                // Obtiene la revisión configurada en el job y muestra el commit construido.
                checkout scm
                sh '''
                    set -eu
                    echo "Commit construido:"
                    git log -1 --pretty=format:'Hash: %H%nAutor: %an%nFecha: %ad%nMensaje: %s%n'
                '''
            }
        }

        stage('Backend Test') {
            steps {
                // El test de contexto necesita la instancia MySQL definida por el proyecto.
                sh '''
                    set -eu
                    docker compose -f backend/docker-compose.yaml up -d db

                    attempts=0
                    until [ "$(docker inspect --format='{{.State.Health.Status}}' noteapp-db 2>/dev/null || true)" = "healthy" ]; do
                        attempts=$((attempts + 1))
                        if [ "$attempts" -ge 30 ]; then
                            echo "MySQL no alcanzó el estado healthy dentro del tiempo esperado."
                            docker compose -f backend/docker-compose.yaml logs --tail=100 db
                            exit 1
                        fi
                        sleep 2
                    done

                    chmod +x backend/mvnw
                    cd backend
                    SPRING_DOCKER_COMPOSE_ENABLED=false \
                    SPRING_DATASOURCE_URL='jdbc:mysql://localhost:3307/noteapp?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true' \
                    SPRING_DATASOURCE_USERNAME=root \
                    SPRING_DATASOURCE_PASSWORD=root \
                    ./mvnw test
                '''
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'backend/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Frontend Validation') {
            steps {
                // No existe script de tests; el build valida TypeScript y la compilación de Vite.
                dir('frontend') {
                    echo 'No se encontró un script de tests en frontend/package.json; se validará el build de producción.'
                    sh '''
                        set -eu
                        npm ci
                        npm run build
                    '''
                }
            }
        }

        stage('Docker Build') {
            steps {
                // Construye backend y frontend mediante los archivos Compose del repositorio.
                sh '''
                    set -eu
                    docker compose $COMPOSE_FILES build
                '''
            }
        }

        stage('Stop Previous Version') {
            steps {
                // Detiene la versión anterior sin usar -v, por lo que conserva los datos de MySQL.
                sh '''
                    docker compose $COMPOSE_FILES down || true
                '''
            }
        }

        stage('Deploy') {
            steps {
                // Levanta el stack completo en segundo plano con las imágenes actuales.
                sh '''
                    set -eu
                    docker compose $COMPOSE_FILES up --build -d
                '''
            }
        }

        stage('Health Check') {
            steps {
                // Comprueba los contenedores, la salud de MySQL y la respuesta HTTP del frontend.
                sh '''
                    set -eu
                    docker compose $COMPOSE_FILES ps

                    for service in db backend frontend; do
                        if ! docker compose $COMPOSE_FILES ps --services --status running | grep -qx "$service"; then
                            echo "El servicio $service no está ejecutándose."
                            docker compose $COMPOSE_FILES logs --tail=100
                            exit 1
                        fi
                    done

                    attempts=0
                    until [ "$(docker inspect --format='{{.State.Health.Status}}' noteapp-db 2>/dev/null || true)" = "healthy" ]; do
                        attempts=$((attempts + 1))
                        if [ "$attempts" -ge 30 ]; then
                            echo "MySQL no alcanzó el estado healthy."
                            docker compose $COMPOSE_FILES logs --tail=100 db
                            exit 1
                        fi
                        sleep 2
                    done

                    if ! curl --fail --silent --show-error \
                        --retry 20 --retry-delay 3 --retry-connrefused \
                        http://localhost:5173/ > /dev/null; then
                        echo "El frontend no respondió correctamente."
                        docker compose $COMPOSE_FILES logs --tail=100
                        exit 1
                    fi

                    echo "Health check de NoteApp completado correctamente."
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
            // Deja visible el estado final sin convertir esta tarea informativa en otro fallo.
            sh 'docker compose $COMPOSE_FILES ps || docker ps || true'
        }
    }
}
