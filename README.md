# kafka-streams-avro-to-protobuf
transform avro message <identity> to protobuf <identity> (Protocol Buffers Google ) 
  
#Build project
gradle build

#Execution 
java -jar build/libs/kstreams-avro-to-protobuf-0.0.1.jar configuration/dev.properties

#create topics :
avro-identity and proto-indentity

#consume message protobuf:
./kafka-protobuf-console-consumer --bootstrap-server localhost:9092 --topic proto-identity --from-beginning

#produce message avro:
./kafka-avro-console-producer --topic avro-identity --broker-list localhost:9092 --property value.schema="$(< src/main/avro/identity.avsc)"

#message
{"nom":"Starsky","prenom":"hutch","age":"53","sexe":"M"}



