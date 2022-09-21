package io.aiven.klaw.clusterapi.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.aiven.klaw.clusterapi.UtilMethods;
import io.aiven.klaw.clusterapi.models.AclIPPrincipleType;
import io.aiven.klaw.clusterapi.models.AclsNativeType;
import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
import java.util.*;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class ManageKafkaComponentsTest {

  @Mock private ClusterApiUtils getAdminClient;

  @Mock private Environment env;

  @Mock private AdminClient adminClient;

  @Mock private ListTopicsResult listTopicsResult;

  @Mock private KafkaFuture<Set<String>> kafkaFuture;

  @Mock private KafkaFuture<Map<String, TopicDescription>> kafkaFutureTopicdesc;

  @Mock private KafkaFuture<Collection<AclBinding>> kafkaFutureCollection;

  @Mock private DescribeTopicsResult describeTopicsResult;

  @Mock private DescribeAclsResult describeAclsResult;

  @Mock private AccessControlEntry accessControlEntry;

  @Mock private CreateTopicsResult createTopicsResult;

  @Mock private CreateAclsResult createAclsResult;

  @Mock private Map<String, KafkaFuture<Void>> futureTocpiCreateResult;

  @Mock private KafkaFuture<Void> kFutureVoid;

  @Mock private RestTemplate restTemplate;

  private UtilMethods utilMethods;

  private ManageKafkaComponents manageKafkaComponents;

  private SchemaService schemaService;

  @Before
  public void setUp() {
    manageKafkaComponents = new ManageKafkaComponents(env, getAdminClient);
    utilMethods = new UtilMethods();
  }

  @Test
  @Ignore
  public void getStatusOnline() throws Exception {
    Set<HashMap<String, String>> topicsSet = utilMethods.getTopics();

    when(getAdminClient.getAdminClient(any(), eq("PLAINTEXT"), anyString()))
        .thenReturn(adminClient);
    String result = manageKafkaComponents.getStatus("localhost", "PLAINTEXT", "", "");
    assertEquals("ONLINE", result);
  }

  @Test
  public void getStatusOffline1() {

    String result = manageKafkaComponents.getStatus("localhost", "PLAINTEXT", "", "");
    assertEquals("OFFLINE", result);
  }

  @Test
  public void getStatusOffline2() {

    String result = manageKafkaComponents.getStatus("localhost", "PLAINTEXT", "", "");
    assertEquals("OFFLINE", result);
  }

  @Test
  @Ignore
  public void loadAcls1() throws Exception {
    List<AclBinding> listAclBindings = utilMethods.getListAclBindings(accessControlEntry);

    when(getAdminClient.getAdminClient(any(), eq("PLAINTEXT"), anyString()))
        .thenReturn(adminClient);
    when(adminClient.describeAcls(any())).thenReturn(describeAclsResult);
    when(describeAclsResult.values()).thenReturn(kafkaFutureCollection);
    when(kafkaFutureCollection.get()).thenReturn(listAclBindings);
    when(accessControlEntry.host()).thenReturn("11.12.33.456");
    when(accessControlEntry.operation()).thenReturn(AclOperation.READ);
    when(accessControlEntry.permissionType()).thenReturn(AclPermissionType.ALLOW);

    Set<Map<String, String>> result = manageKafkaComponents.loadAcls("localhost", "PLAINTEXT", "");
    assertEquals(1, result.size());
  }

  @Test
  @Ignore
  public void loadAcls2() throws Exception {
    List<AclBinding> listAclBindings = utilMethods.getListAclBindings(accessControlEntry);

    when(getAdminClient.getAdminClient(any(), eq("PLAINTEXT"), anyString()))
        .thenReturn(adminClient);
    when(adminClient.describeAcls(any())).thenReturn(describeAclsResult);
    when(describeAclsResult.values()).thenReturn(kafkaFutureCollection);
    when(kafkaFutureCollection.get()).thenReturn(listAclBindings);
    when(accessControlEntry.host()).thenReturn("11.12.33.456");
    when(accessControlEntry.operation()).thenReturn(AclOperation.CREATE);
    when(accessControlEntry.permissionType()).thenReturn(AclPermissionType.ALLOW);

    Set<Map<String, String>> result = manageKafkaComponents.loadAcls("localhost", "PLAINTEXT", "");
    assertEquals(0, result.size());
  }

  @Test
  public void loadAcls3() throws Exception {
    when(getAdminClient.getAdminClient(any(), eq("PLAINTEXT"), anyString()))
        .thenReturn(adminClient);
    when(adminClient.describeAcls(any())).thenThrow(new RuntimeException("Describe Acls Error"));

    Set<Map<String, String>> result = manageKafkaComponents.loadAcls("localhost", "PLAINTEXT", "");
    assertEquals(0, result.size());
  }

  @Test
  @Ignore
  public void loadTopics() throws Exception {
    Set<HashMap<String, String>> topicsSet = utilMethods.getTopics();
    Set<String> list = new HashSet<>();
    when(getAdminClient.getAdminClient(any(), eq("PLAINTEXT"), anyString()))
        .thenReturn(adminClient);
    when(adminClient.listTopics()).thenReturn(listTopicsResult);
    when(listTopicsResult.names()).thenReturn(kafkaFuture);
    when(kafkaFuture.get()).thenReturn(list);

    when(adminClient.describeTopics((Collection<String>) any())).thenReturn(describeTopicsResult);
    when(describeTopicsResult.all()).thenReturn(kafkaFutureTopicdesc);
    when(kafkaFutureTopicdesc.get()).thenReturn(getTopicDescs());

    Set<HashMap<String, String>> result =
        manageKafkaComponents.loadTopics("localhost", "PLAINTEXT", "");

    HashMap<String, String> hashMap = new HashMap<>();
    hashMap.put("partitions", "2");
    hashMap.put("replicationFactor", "1");
    hashMap.put("topicName", "testtopic2");

    HashMap<String, String> hashMap1 = new HashMap<>();
    hashMap1.put("partitions", "2");
    hashMap1.put("replicationFactor", "1");
    hashMap1.put("topicName", "testtopic1");

    assertEquals(2, result.size());
    assertEquals(hashMap, new ArrayList<>(result).get(0));
    assertEquals(hashMap1, new ArrayList<>(result).get(1));
  }

  @Test
  @Ignore
  public void createTopicSuccess() throws Exception {
    String name = "testtopic1",
        partitions = "1",
        replicationFactor = "1",
        environment = "localhost";
    when(getAdminClient.getAdminClient(any(), eq("PLAINTEXT"), "")).thenReturn(adminClient);
    when(adminClient.createTopics(any())).thenReturn(createTopicsResult);
    when(createTopicsResult.values()).thenReturn(futureTocpiCreateResult);
    when(futureTocpiCreateResult.get(anyString())).thenReturn(kFutureVoid);

    String result =
        manageKafkaComponents.createTopic(
            name, partitions, replicationFactor, environment, "PLAINTEXT", "");
    assertEquals("success", result);
  }

  @Test(expected = Exception.class)
  public void createTopicFailure1() throws Exception {
    String name = "testtopic1",
        partitions = "1",
        replicationFactor = "1",
        environment = "localhost";
    when(getAdminClient.getAdminClient(any(), eq("PLAINTEXT"), anyString())).thenReturn(null);

    manageKafkaComponents.createTopic(
        name, partitions, replicationFactor, environment, "PLAINTEXT", "");
  }

  @Test(expected = NumberFormatException.class)
  public void createTopicFailure2() throws Exception {
    String name = "testtopic1",
        partitions = "1aa",
        replicationFactor = "1aa",
        environment = "localhost";
    when(getAdminClient.getAdminClient(any(), eq("PLAINTEXT"), anyString()))
        .thenReturn(adminClient);

    manageKafkaComponents.createTopic(
        name, partitions, replicationFactor, environment, "PLAINTEXT", "");
  }

  @Test(expected = RuntimeException.class)
  public void createTopicFailure4() throws Exception {
    String name = "testtopic1",
        partitions = "1",
        replicationFactor = "1",
        environment = "localhost";
    when(getAdminClient.getAdminClient(any(), eq("PLAINTEXT"), anyString()))
        .thenReturn(adminClient);
    when(adminClient.createTopics(any())).thenThrow(new RuntimeException("Runtime exption"));

    manageKafkaComponents.createTopic(
        name, partitions, replicationFactor, environment, "PLAINTEXT", "");
  }

  @Test
  @Ignore
  public void createProducerAcl1() throws Exception {
    String topicName = "testtopic",
        environment = "localhost",
        protocol = "PLAINTEXT",
        acl_ip = "110.11.21.112";
    when(getAdminClient.getAdminClient(any(), eq("PLAINTEXT"), anyString()))
        .thenReturn(adminClient);
    when(adminClient.createAcls(any())).thenReturn(createAclsResult);

    String result =
        manageKafkaComponents.updateProducerAcl(
            topicName,
            environment,
            protocol,
            "",
            acl_ip,
            null,
            "Create",
            "false",
            "",
            AclIPPrincipleType.PRINCIPLE.name(),
            AclsNativeType.NATIVE.name());
    assertEquals("success", result);
  }

  @Test
  @Ignore
  public void createProducerAcl2() throws Exception {
    String topicName = "testtopic", environment = "localhost", acl_ssl = "CN=host,OU=...";
    when(getAdminClient.getAdminClient(any(), eq("PLAINTEXT"), anyString()))
        .thenReturn(adminClient);
    when(adminClient.createAcls(any())).thenReturn(createAclsResult);

    String result =
        manageKafkaComponents.updateProducerAcl(
            topicName,
            environment,
            "PLAINTEXT",
            "",
            null,
            acl_ssl,
            "Create",
            "false",
            "",
            AclIPPrincipleType.PRINCIPLE.name(),
            AclsNativeType.NATIVE.name());
    assertEquals("success", result);
  }

  @Test
  @Ignore
  public void createConsumerAcl1() throws Exception {
    String topicName = "testtopic",
        environment = "localhost",
        acl_ip = "110.11.21.112",
        consumerGroup = "congroup1";
    when(getAdminClient.getAdminClient(any(), eq("PLAINTEXT"), anyString()))
        .thenReturn(adminClient);
    when(adminClient.createAcls(any())).thenReturn(createAclsResult);

    String result =
        manageKafkaComponents.updateConsumerAcl(
            topicName,
            environment,
            "PLAINTEXT",
            "",
            acl_ip,
            null,
            consumerGroup,
            "Create",
            "false",
            AclIPPrincipleType.PRINCIPLE.name(),
            AclsNativeType.NATIVE.name());
    assertEquals("success", result);
  }

  @Test
  @Ignore
  public void createConsumerAcl2() throws Exception {
    String topicName = "testtopic",
        environment = "localhost",
        acl_ssl = "CN=host,OU=...",
        consumerGroup = "congroup1";
    when(getAdminClient.getAdminClient(any(), eq("PLAINTEXT"), anyString()))
        .thenReturn(adminClient);
    when(adminClient.createAcls(any())).thenReturn(createAclsResult);

    String result =
        manageKafkaComponents.updateConsumerAcl(
            topicName,
            environment,
            "PLAINTEXT",
            "",
            null,
            acl_ssl,
            consumerGroup,
            "Create",
            "false",
            AclIPPrincipleType.PRINCIPLE.name(),
            AclsNativeType.NATIVE.name());
    assertEquals("success", result);
  }

  @Test
  public void postSchema1() {
    String topicName = "testtopic1", schema = "{type:string}", environmentVal = "localhost";
    ResponseEntity<String> response =
        new ResponseEntity<>("Schema created id : 101", HttpStatus.OK);

    when(getAdminClient.getRestTemplate()).thenReturn(restTemplate);
    when(restTemplate.postForEntity(anyString(), any(), eq(String.class))).thenReturn(response);

    String result = schemaService.registerSchema(topicName, schema, environmentVal, "PLAINTEXT");
    assertEquals("Schema created id : 101", result);
  }

  @Test
  public void postSchema2() {
    String topicName = "testtopic1", schema = "{type:string}", environmentVal = null;

    String result = schemaService.registerSchema(topicName, schema, environmentVal, "PLAINTEXT");
    assertEquals("Cannot retrieve SchemaRegistry Url", result);
  }

  private Map<String, TopicDescription> getTopicDescs() {
    Node node = new Node(1, "localhost", 1);

    TopicPartitionInfo topicPartitionInfo =
        new TopicPartitionInfo(2, node, Arrays.asList(node), Arrays.asList(node));
    TopicDescription tDesc =
        new TopicDescription(
            "testtopic", true, Arrays.asList(topicPartitionInfo, topicPartitionInfo));
    Map<String, TopicDescription> mapResults = new HashMap<>();
    mapResults.put("testtopic1", tDesc);

    tDesc =
        new TopicDescription(
            "testtopic2", true, Arrays.asList(topicPartitionInfo, topicPartitionInfo));
    mapResults.put("testtopic2", tDesc);

    return mapResults;
  }
}
