# docker command
docker build -t product-service .
docker images | grep product-service
docker run --rm -p8080:8080 -e "SPRING_PROFILES_ACTIVE=docker" product-service
docker run -d -p8080:8080 -e "SPRING_PROFILES_ACTIVE=docker" --name product-service product-service
