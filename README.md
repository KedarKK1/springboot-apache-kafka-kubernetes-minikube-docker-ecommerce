# Ecommerce app using Springboot & Apache Kafka


### How to start project


1. Build your springboot apps

```bash
# Inside order-service
./mvnw clean package -DskipTests

# Inside notification-service
./mvnw clean package -DskipTests
```

2. Run the docker containers in one go using -
```bash
docker-compose up

# to close
docker-compose down
```

- Verify Messages are actually in Kafka

```bash
docker exec -it ecommerce-kafka-1 kafka kafka-console-consumer --bootstrap-server kafka:29092 --topic orders-topic --from-beginning

docker exec -it ecommerce-kafka-1 kafka kafka-console-producer --bootstrap-server kafka:29092 --topic orders-topic --from-beginning
```


Note: incase of below error
The No such container: kafka error happens because Docker Compose automatically prefixes container names with your project folder name. For example, if your folder is named ecommerce, the container is likely named ecommerce-kafka-1.

Let's find the real name and get you those logs.

1. Find the exact container name
Run this to see what Docker actually named your Kafka container:

```Bash
docker ps --format "table {{.Names}}\t{{.Status}}"
```
Look for the name that has "kafka" in it.

2. 
```bash
docker exec -it NAME_FROM_STEP_1 kafka-console-consumer --bootstrap-server kafka:29092 --topic order-topic --from-beginning
```

```bash
curl -X POST http://localhost:8081/orders \
     -H "Content-Type: text/plain" \
     -d "Order_#1001_Large_Pizza"
```

```bash
ecommerce/
├── docker-compose.yml
├── ss
├── README.md
├── order-service/
│   ├── Dockerfile       <-- Just created
│   ├── target/
│   │   └── order-service-0.0.1-SNAPSHOT.jar
│   └── src/
└── notification-service/
    ├── Dockerfile       <-- Just created
    ├── target/
    │   └── notification-service-0.0.1-SNAPSHOT.jar
    └── src/
```

### Screenshots

Final input -
![Final Input](./ss/final_input.png)
Final output -
![Final Output](./ss/final_output.png)

project-setup1 -
![Final Input](./ss/project-setup1.png)
project-setup2 -
![Final Output](./ss/project-setup2.png)
project-setup3 -
![Final Input](./ss/project-setup3.png)
project-setup4 -
![Final Output](./ss/project-setup4.png)

