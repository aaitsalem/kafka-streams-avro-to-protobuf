# kafka-streams-avro-to-protobuf
Transform avro message "identity" to protobuf "identity" message ( Protocol Buffers Google ) 
  
### Build project
gradle build

### Execution 
java -jar build/libs/kstreams-avro-to-protobuf-0.0.1.jar configuration/dev.properties

### Create topics 
avro-identity and proto-indentity

### Consume message protobuf
./kafka-protobuf-console-consumer --bootstrap-server localhost:9092 --topic proto-identity --from-beginning

### Produce message avro:
./kafka-avro-console-producer --topic avro-identity --broker-list localhost:9092 --property value.schema="$(< src/main/avro/identity.avsc)"

### Example
{"nom":"Starsky","prenom":"hutch","age":"53","sexe":"M"}



