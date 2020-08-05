package io.agh.serialization;

import io.agh.avro.Identity;
import io.agh.proto.IdentityProtos;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.confluent.kafka.streams.serdes.protobuf.KafkaProtobufSerde;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static org.apache.kafka.common.serialization.Serdes.Long;
import static org.apache.kafka.common.serialization.Serdes.String;

public class KafkaStreamsAvroToProtobuf {

  public static final String SCHEMA_REGISTRY_URL = "schema.registry.url";
  public static final String APPLICATION_ID = "application.id";
  public static final String BOOTSTRAP_SERVERS = "bootstrap.servers";
  public static final String SCHEMA_REGISTRY_URL1 = "schema.registry.url";
  public static final String AVRO_IDENTITY = "avro-identity";
  public static final String PROTO_IDENTITY = "proto-identity";

  public static void main(String[] args) throws IOException {
    new KafkaStreamsAvroToProtobuf().run(args[0]);
  }
  protected Properties buildProperties(Properties envProps) {
    Properties properties = new Properties();

    properties.put(StreamsConfig.APPLICATION_ID_CONFIG, envProps.getProperty(APPLICATION_ID));
    properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, envProps.getProperty(BOOTSTRAP_SERVERS));
    properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, String().getClass());
    properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, String().getClass());
    properties.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, envProps.getProperty(SCHEMA_REGISTRY_URL1));

    return properties;
  }

  protected Properties loadEnvProperties(String fileName) throws IOException {
    Properties envProps = new Properties();
    FileInputStream input = new FileInputStream(fileName);
    envProps.load(input);
    input.close();
    return envProps;
  }

  private void run(String configPath) throws IOException {

    Properties envProps = this.loadEnvProperties(configPath);
    Properties streamProps = this.buildProperties(envProps);
    final StreamsBuilder builder = new StreamsBuilder();

    final KStream<Long, Identity> avroIdentityStream =
            builder.stream(AVRO_IDENTITY, Consumed.with(Long(), this.identityAvroSerde(envProps)));

    avroIdentityStream
            .map((key, avroIdentity) ->
                    new KeyValue<>(key, IdentityProtos.Identity.newBuilder()
                            .setNom(avroIdentity.getNom())
                            .setPrenom(avroIdentity.getPrenom())
                            .setAge(avroIdentity.getAge())
                            .setSexe(avroIdentity.getSexe())
                            .build()))
            .to(PROTO_IDENTITY, Produced.with(Long(), this.identityProtobufSerde(envProps)));

    Topology topology=builder.build();

    final KafkaStreams streams = new KafkaStreams(topology, streamProps);
    final CountDownLatch latch = new CountDownLatch(1);

    Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
      @Override
      public void run() {
        streams.close(Duration.ofSeconds(5));
        latch.countDown();
      }
    });

    try {
      streams.cleanUp();
      streams.start();
      latch.await();
    } catch (Throwable e) {
      System.exit(1);
    }
    System.exit(0);
  }

  protected SpecificAvroSerde<Identity> identityAvroSerde(Properties envProps) {
    SpecificAvroSerde<Identity> identitySpecificAvroSerde = new SpecificAvroSerde<>();

    Map<String, String> serdeConfig = new HashMap<>();
    serdeConfig.put(SCHEMA_REGISTRY_URL_CONFIG, envProps.getProperty(SCHEMA_REGISTRY_URL));
    identitySpecificAvroSerde.configure(serdeConfig, false);
    return identitySpecificAvroSerde;
  }

  protected KafkaProtobufSerde<IdentityProtos.Identity> identityProtobufSerde(Properties envProps) {
    final KafkaProtobufSerde<IdentityProtos.Identity> protobufSerde = new KafkaProtobufSerde<>();

    Map<String, String> serdeConfig = new HashMap<>();
    serdeConfig.put(SCHEMA_REGISTRY_URL_CONFIG, envProps.getProperty(SCHEMA_REGISTRY_URL));
    protobufSerde.configure(serdeConfig, false);
    return protobufSerde;
  }

}
