```
temporal server start-dev
```

```sh
temporal operator namespace create --namespace my-target-namespace
temporal operator namespace create --namespace my-caller-namespace
temporal operator nexus endpoint create \
  --name my-nexus-endpoint-name \
  --target-namespace my-target-namespace \
  --target-task-queue my-handler-task-queue \
  --description-file ./core/src/main/java/io/temporal/samples/nexus/service/description.md
```

```
./gradlew -q execute -PmainClass=io.temporal.samples.nexus.handler.HandlerWorker \
    --args="-target-host localhost:7233 -namespace my-target-namespace"

./gradlew -q execute -PmainClass=io.temporal.samples.nexus.caller.CallerWorker \
    --args="-target-host localhost:7233 -namespace my-caller-namespace"

./gradlew -q execute -PmainClass=io.temporal.samples.nexus.caller.ErrorTestStarter \
    --args="-target-host localhost:7233 -namespace my-caller-namespace"
```    