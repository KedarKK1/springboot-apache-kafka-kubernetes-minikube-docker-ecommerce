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

3. Image Loading
    Kubernetes cannot see the images on your local machine by default. You have two choices:

    1. **Push to Docker Hub:** Run `docker push your-username/order-service`.
    2. **Load into Minikube/Kind:** If you are using Minikube, run `minikube image load order-service:latest`.

    Installing Minikube is the final step to getting your microservices running in a "real" local Kubernetes environment. Since you already have Docker Desktop, we can use it as the **driver**, which makes the setup much smoother.

    1. Installation

        Depending on your Operating System, run the following in your terminal:

        **macOS** - `brew install minikube`

    2. Start Minikube

        Since you already have Docker installed, tell Minikube to use it as the engine. This avoids the need for heavy Virtual Machines like VirtualBox.

        ```bash
        minikube start --driver=docker
        ```

    3. heck the status:

        ```bash
        minikube status
        ```

        You should see `host: Running`, `kubelet: Running`, and `apiserver: Running`

    4. The "Image Secret" for your Spring Apps

        This is the most important part for your project: **Minikube cannot see the images on your laptop.** To fix this without pushing to Docker Hub, you can "point" your terminal to Minikube's internal Docker registry:

        ```bash
        # Run this to link your terminal to Minikube
        eval $(minikube docker-env)

        # Now, build your images again while this is active
        docker build -t order-service:latest ./order-service
        docker build -t notification-service:latest ./notification-service
        ```

        Now, when you run your Kubernetes YAML files, Minikube will find the images locally!

    5. Basic Commands You'll Need

        | Task | Command |
        | --- | --- |
        | **Open Dashboard** | `minikube dashboard` (Visual UI for your cluster) |
        | **Access Service** | `minikube service order-service --url` (Gets the URL for your API) |
        | **Stop Cluster** | `minikube stop` |
        | **Delete Cluster** | `minikube delete` (Use this if things get messy to start fresh) |

        [Minikube and Kubectl explained for Beginners](https://www.youtube.com/watch?v=E2pP1MOfo3g)

        This video provides a visual walkthrough of setting up Minikube and using Kubectl, which is perfect for ensuring your environment is configured correctly for your Spring Boot services.

    ----------------------------------------------


        When you are using Minikube and want to use images you haven't pushed to a registry (like Docker Hub), the `image` field in your `order-k8s.yaml` needs two things to work: **the local name** and a **strict pull policy**.

        ### 1. The Deployment YAML Configuration

        In your `containers` section, use the exact name you used when building the image locally. The "secret sauce" is the `imagePullPolicy: Never`. Without this, Kubernetes will try to search for your image on the internet, fail, and give you an `ErrImagePull` error.

        **`order-k8s.yaml` (Updated snippet):**

        ```yaml
        spec:
        containers:
        - name: order-service
            image: order-service:latest  # The name you gave it during 'docker build'
            imagePullPolicy: Never       # IMPORTANT: Tells K8s not to look online
            ports:
            - containerPort: 8081

        ```

        ---

        ### 2. How to make Minikube "See" your images

        Even with the YAML set correctly, Minikube's internal "brain" is separate from your laptop's Docker. You have two options to fix this:

        #### Option A: Build directly inside Minikube (The Pro Way)

        Run this command in your terminal **before** you build your Java apps. It points your terminal's Docker commands to the Minikube cluster:

        ```bash
        eval $(minikube docker-env)

        ```

        Now, build your image:

        ```bash
        docker build -t order-service:latest ./order-service

        ```

        Since you're "inside" Minikube's environment now, the image is instantly available for your YAML file.

        #### Option B: Load an existing image

        If you already built the image on your laptop and don't want to rebuild it, just "push" it into Minikube:

        ```bash
        minikube image load order-service:latest

        ```

        ---

        ### 3. Verification

        Once you've done one of the above, apply your manifest:

        ```bash
        kubectl apply -f order-k8s.yaml

        ```

        To see if it worked, run:

        ```bash
        kubectl get pods # Check the status: If it says ErrImagePull, remember to run minikube image load order-service:latest first
        ```

        ```bash
        minikube service order-service # Open the Service (since you are on Minikube, LoadBalancers need a tunnel)
        ```

        If the status is **Running**, you've successfully bypassed the need for a Docker registry!

        **Would you like me to help you set up the Kafka "Service" name so your pods can find the broker once they are running in Minikube?**

        [Deploying local docker images to Minikube](https://www.youtube.com/watch?v=7tWJbsGglYA)
        This video provides a practical walkthrough on how to force Minikube to recognize and use images from your local system without needing a remote registry.

    ----------------------------------------------------

4. How to Deploy Kafka
    Instead of writing a 200-line YAML file for Kafka, you should use the **Strimzi Quickstart**. Run these commands in your terminal to set up Kafka in about 2 minutes:

    ```bash
    # 1. Install the Strimzi Operator (The "Brain")
    kubectl create namespace kafka
    kubectl create -f 'https://strimzi.io/install/latest?namespace=kafka' -n kafka

    # 2. Provision a small Kafka cluster
    kubectl apply -f https://strimzi.io/examples/kafka/kafka-persistent-single.yaml -n kafka
    ```


    Note: The value for `SPRING_KAFKA_BOOTSTRAP_SERVERS` depends entirely on how you deployed Kafka inside Minikube. Since we discussed using the **Strimzi Operator** (the standard for K8s), there is a specific naming convention you must follow.

    ### The Answer

    If you followed the Strimzi installation where your Kafka cluster is named `my-cluster` and it is in the **same namespace** as your apps, the value should be:

    **`my-cluster-kafka-bootstrap:9092`**

    ---

    ### Why this value?

    In Kubernetes, when you create a Kafka cluster using Strimzi, it automatically creates a Service called a "Bootstrap Service." The name is always structured as:
    `[Cluster-Name]-kafka-bootstrap`

    ### Important: Cross-Namespace Communication

    If you installed Kafka in a namespace called `kafka` but your microservices are in the `default` namespace, you must use the "Fully Qualified Domain Name" (FQDN):

    **`my-cluster-kafka-bootstrap.kafka.svc.cluster.local:9092`**

    ---

    ### How to verify the exact name

    If you aren't sure what your Kafka service is named, run this command:

    ```bash
    kubectl get svc -A | grep bootstrap

    ```

    Look for the entry under the **NAME** column. That is exactly what you should put in your `value:` field.

    ---

    ### Pro-Tip: Update your Notification Service too!

    Make sure your `notification-k8s.yaml` uses the exact same `SPRING_KAFKA_BOOTSTRAP_SERVERS` value. In a Kubernetes cluster, internal DNS allows both services to find Kafka using that same hostname.

    ### Final Verification Step

    After applying your YAMLs, check if the app connected successfully by looking at the logs:

    ```bash
    kubectl logs deployment/order-service

    ```

    If you see **"Producer clientId=... - Cluster ID: ..."**, you are officially running a microservice architecture on Kubernetes!

    **Would you like me to help you set up the `notification-k8s.yaml` or show you how to use `minikube tunnel` to actually hit that LoadBalancer port from your browser?**

    ------------------------------------

    This is a great catch! It highlights the difference between how Kafka is accessed **inside** a network vs. **outside** a network.

    Your **Dockerfile** is actually fine because it doesn't hardcode the port; however, your **Kubernetes/Docker environment variables** must change to match the specific environment you are running in.

    ---

    ### 1. The "Two Port" Rule (Listeners)

    Kafka uses "Listeners" to handle different types of traffic. Think of it like a hotel with a front door for guests and a back door for staff.

    When you are **inside** the Kafka container, the bootstrap server is often just `localhost:9092` or `kafka:29092` depending on the image settings.*

    | Environment | Port | Who uses it? | Why? |
    | --- | --- | --- | --- |
    | **Docker Compose** | `29092` | Your Microservices | Containers talking to each other inside the Docker network. |
    | **Local Machine** | `9092` | Your IDE / Postman | You, looking into the Docker container from your laptop. |
    | **Kubernetes** | `9092` | Your Microservices | Inside K8s, the Strimzi/Kafka service usually defaults back to `9092` for internal traffic. |

    ---

    ### 2. Is the Dockerfile affected?

    **No.** Your Dockerfile should stay "generic." It just packages your Java code.
    The logic of which port to use is handled by **Environment Variables** in your YAML files.

    **In your Java Code (`application.properties`):**

    ```properties
    # Always use a variable so you can change it without rebuilding the image!
    spring.kafka.bootstrap-servers=${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}

    ```

    ---

    ### 3. What to use in your K8s YAML

    When you move to Kubernetes (Minikube), the Confluent settings from Docker Compose **no longer apply**. You are now using the K8s Service name.

    **In your `order-k8s.yaml`:**

    ```yaml
    env:
    - name: SPRING_KAFKA_BOOTSTRAP_SERVERS
    value: "my-cluster-kafka-bootstrap:9092" # Standard K8s internal port

    ```

    To use `kafka-console-consumer`, you must run that command inside the **Kafka container**, not your Java service container.

    ### 1. The Correct Command

    Find your Kafka container name again (likely `ecommerce-kafka-1`) and run the command there:

    ```bash
    # Replace 'ecommerce-kafka-1' with your actual Kafka container name
    docker exec -it ecommerce-kafka-1 kafka-console-consumer --bootstrap-server localhost:9092 --topic order-topic --from-beginning

    ```

    *Note: When you are **inside** the Kafka container, the bootstrap server is often just `localhost:9092` or `kafka:29092` depending on the image settings.*

    ---

    ### 2. Transitioning this Logic to Kubernetes

    Now that we are moving to **Minikube**, the way you "debug" or "look inside" changes. You won't use `docker exec` anymore. You will use `kubectl`.

    **If you want to consume messages in Kubernetes:**
    You can run a temporary "Pod" that contains the Kafka utilities:

    ```bash
    kubectl run kafka-consumer -ti --image=confluentinc/cp-kafka:7.6.0 --rm \
    --restart=Never -- kafka-console-consumer \
    --bootstrap-server my-cluster-kafka-bootstrap:9092 \
    --topic order-topic --from-beginning

    ```

    ---

    ### 3. Summary of Ports for your K8s YAMLs

    To clear up the `29092` vs `9092` doubt for your Kubernetes manifests:

    * **Docker Compose (Confluent):** Used `29092` for internal container-to-container talk.
    * **Kubernetes (Strimzi/Standard):** Uses `9092` for internal pod-to-pod talk.

    ### Next Step for you:

    1. **Build** your images using the Minikube environment: `eval $(minikube docker-env)` then `docker build...`
    2. **Apply** your `order-k8s.yaml`.
    3. **Check logs** to see if it connected: `kubectl logs -l app=order-service`

    **Would you like me to provide the `notification-k8s.yaml` now, ensuring the environment variables match the Kafka service name exactly?**


    -------------------------------

    ### 4. Summary of the Confusion

    * **In Docker Compose:** You used `29092` because the Confluent image was configured to keep the internal and external traffic on separate ports to avoid conflicts on your laptop.
    * **In Kubernetes:** Every Service gets its own internal IP address, so there is no conflict. Most K8s Kafka setups use `9092` for everything inside the cluster.

    ### Quick Verification

    If your Spring Boot app logs say `Connection to node -1 (localhost/127.0.0.1:9092) could not be established`, it means the Environment Variable wasn't picked up, and it's falling back to the default.

    **Would you like me to show you the command to "Exec" into your running Kubernetes pod to see if it can actually "see" the Kafka server at that address?**



### Next Step

Once Kafka is up, you apply your files with:
`kubectl apply -f order-k8s.yaml`
`kubectl apply -f notification-k8s.yaml`

### Next Step

Once Kafka is up, you apply your files with:
`kubectl apply -f order-k8s.yaml`
`kubectl apply -f notification-k8s.yaml`


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

minikube-dashboard1 -
![dashboard1](./ss/minikube-dashboard1.png)
minikube-dashboard2 -
![dashboard2](./ss/minikube-dashboard2.png)
minikube-dashboard3 -
![dashboard3](./ss/minikube-dashboard3.png)
minikube-dashboard4 -
![dashboard4](./ss/minikube-dashboard4.png)
minikube-dashboard5 -
![dashboard5](./ss/minikube-dashboard5.png)
minikube-dashboard6 -
![dashboard6](./ss/minikube-dashboard6.png)
minikube-dashboard7 -
![dashboard7](./ss/minikube-dashboard7.png)
minikube-dashboard8 -
![dashboard8](./ss/minikube-dashboard8.png)
minikube-dashboard9 -
![dashboard9ß](./ss/minikube-dashboard9.png)

project-setup1 -
![Final Input](./ss/project-setup1.png)
project-setup2 -
![Final Output](./ss/project-setup2.png)
project-setup3 -
![Final Input](./ss/project-setup3.png)
project-setup4 -
![Final Output](./ss/project-setup4.png)

minikube-setup1 -
![minikube-setup1](./ss/minikube-setup1.png)
minikube-setup2 -
![minikube-setup2](./ss/minikube-setup2.png)
minikube-setup3 -
![minikube-setup3](./ss/minikube-setup3.png)
minikube-setup4 -
![minikube-setup4](./ss/minikube-setup4.png)
minikube-setup5 -
![minikube-setup5](./ss/minikube-setup5.png)
minikube-setup6 -
![minikube-setup6](./ss/minikube-setup6.png)
minikube-setup7 -
![minikube-setup7](./ss/minikube-setup7.png)
minikube-setup8 -
![minikube-setup8](./ss/minikube-setup8.png)
minikube-setup9 -
![minikube-setup9](./ss/minikube-setup9.png)
minikube-setup10 -
![minikube-setup10](./ss/minikube-setup10.png)



