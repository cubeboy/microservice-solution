
java -jar microservices/product-composite-service/build/libs/*-SNAPSHOT.jar &
java -jar microservices/product-service/build/libs/*SNAPSHOT.jar &
java -jar microservices/recommendation-service/build/libs/*SNAPSHOT.jar &
java -jar microservices/review-service/build/libs/*SNAPSHOT.jar
