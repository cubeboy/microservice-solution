# docker command
docker build -t recommendation-service .
docker images | grep recommendation-service
docker run --rm -p8080:8080 -e "SPRING_PROFILES_ACTIVE=docker" recommendation-service
docker run -d -p8080:8080 -e "SPRING_PROFILES_ACTIVE=docker" --name recommendation-service recommendation-service
