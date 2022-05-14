# docker command
docker build -t review-service .
docker images | grep review-service
docker run --rm -p8080:8080 -e "SPRING_PROFILES_ACTIVE=docker" review-service
docker run -d -p8080:8080 -e "SPRING_PROFILES_ACTIVE=docker" --name review-service review-service
