
## product service images clear
```
docker image rm \
microservice-solution_product-composite \
microservice-solution_product \
microservice-solution_recommendation \
microservice-solution_review
```

## docker logpath 확인하기
```
docker inspect 0a6d0854ab5b --format "{{.LogPath}}"
```
