# docker command
docker build -t product-composite-service .
docker images | grep product-composite-service
docker run --rm -p8080:8080 -e "SPRING_PROFILES_ACTIVE=docker" product-composite-service
docker run -d -p8080:8080 -e "SPRING_PROFILES_ACTIVE=docker" --name product-composite-service product-composite-service
