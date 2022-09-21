package io.aiven.klaw.clusterapi.controller;

import io.aiven.klaw.clusterapi.models.AclsNativeType;
import io.aiven.klaw.clusterapi.services.AivenApiService;
import io.aiven.klaw.clusterapi.services.ManageKafkaComponents;
import io.aiven.klaw.clusterapi.services.MonitoringService;
import io.aiven.klaw.clusterapi.services.SchemaService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/topics")
@Slf4j
public class ClusterApiController {

  @Autowired ManageKafkaComponents manageKafkaComponents;

  @Autowired SchemaService schemaService;

  @Autowired MonitoringService monitoringService;

  @Autowired AivenApiService aivenApiService;

  @RequestMapping(
      value = "/getApiStatus",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<String> getApiStatus() {
    return new ResponseEntity<>("ONLINE", HttpStatus.OK);
  }

  //    @RequestMapping(value = "/reloadTruststore/{protocol}/{clusterName}", method =
  // RequestMethod.GET,produces = {MediaType.APPLICATION_JSON_VALUE})
  //    public ResponseEntity<String> reloadTruststore(@PathVariable String protocol,
  //                                                   @PathVariable String clusterName){
  //        return new ResponseEntity<>(manageKafkaComponents.reloadTruststore(protocol,
  // clusterName), HttpStatus.OK);
  //    }

  @RequestMapping(
      value = "/getStatus/{bootstrapServers}/{protocol}/{clusterName}/{clusterType}",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<String> getStatus(
      @PathVariable String bootstrapServers,
      @PathVariable String protocol,
      @PathVariable String clusterName,
      @PathVariable String clusterType) {
    String envStatus =
        manageKafkaComponents.getStatus(bootstrapServers, protocol, clusterName, clusterType);

    return new ResponseEntity<>(envStatus, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getTopics/{bootstrapServers}/{protocol}/{clusterName}",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Set<HashMap<String, String>>> getTopics(
      @PathVariable String bootstrapServers,
      @PathVariable String protocol,
      @PathVariable String clusterName)
      throws Exception {
    Set<HashMap<String, String>> topics =
        manageKafkaComponents.loadTopics(bootstrapServers, protocol, clusterName);
    return new ResponseEntity<>(topics, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getAcls/{bootstrapServers}/{aclsNativeType}/{protocol}/{clusterName}/{projectName}/{serviceName}",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Set<Map<String, String>>> getAcls(
      @PathVariable String bootstrapServers,
      @PathVariable String protocol,
      @PathVariable String clusterName,
      @PathVariable String aclsNativeType,
      @PathVariable String projectName,
      @PathVariable String serviceName)
      throws Exception {
    Set<Map<String, String>> acls;
    if(AclsNativeType.NATIVE.name().equals(aclsNativeType))
      acls = manageKafkaComponents.loadAcls(bootstrapServers, protocol, clusterName);
    else
      acls = aivenApiService.listAcls(projectName, serviceName);

    return new ResponseEntity<>(acls, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getSchema/{bootstrapServers}/{protocol}/{clusterName}/{topicName}",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Map<Integer, Map<String, Object>>> getSchema(
      @PathVariable String bootstrapServers,
      @PathVariable String protocol,
      @PathVariable String topicName,
      @PathVariable String clusterName) {
    Map<Integer, Map<String, Object>> schema =
        schemaService.getSchema(bootstrapServers, protocol, topicName);

    return new ResponseEntity<>(schema, HttpStatus.OK);
  }

  @RequestMapping(
      value =
          "/getConsumerOffsets/{bootstrapServers}/{protocol}/{clusterName}/{consumerGroupId}/{topicName}",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<HashMap<String, String>>> getConsumerOffsets(
      @PathVariable String bootstrapServers,
      @PathVariable String protocol,
      @PathVariable String clusterName,
      @PathVariable String consumerGroupId,
      @PathVariable String topicName)
      throws Exception {
    List<HashMap<String, String>> consumerOffsetDetails =
        monitoringService.getConsumerGroupDetails(
            consumerGroupId, topicName, bootstrapServers, protocol, clusterName);

    return new ResponseEntity<>(consumerOffsetDetails, HttpStatus.OK);
  }

  @PostMapping(value = "/createTopics")
  public ResponseEntity<String> createTopics(
      @RequestBody MultiValueMap<String, String> topicRequest) {
    try {
      manageKafkaComponents.createTopic(
          topicRequest.get("topicName").get(0),
          topicRequest.get("partitions").get(0),
          topicRequest.get("rf").get(0),
          topicRequest.get("env").get(0),
          topicRequest.get("protocol").get(0),
          topicRequest.get("clusterName").get(0));
    } catch (Exception e) {
      log.error(e.getMessage());
      return new ResponseEntity<>("failure " + e, HttpStatus.OK);
    }

    return new ResponseEntity<>("success", HttpStatus.OK);
  }

  @PostMapping(value = "/updateTopics")
  public ResponseEntity<String> updateTopics(
      @RequestBody MultiValueMap<String, String> topicRequest) {
    try {
      manageKafkaComponents.updateTopic(
          topicRequest.get("topicName").get(0),
          topicRequest.get("partitions").get(0),
          topicRequest.get("rf").get(0),
          topicRequest.get("env").get(0),
          topicRequest.get("protocol").get(0),
          topicRequest.get("clusterName").get(0));
    } catch (Exception e) {
      log.error(e.getMessage());
      return new ResponseEntity<>("failure " + e, HttpStatus.OK);
    }

    return new ResponseEntity<>("success", HttpStatus.OK);
  }

  @PostMapping(value = "/deleteTopics")
  public ResponseEntity<String> deleteTopics(
      @RequestBody MultiValueMap<String, String> topicRequest) {
    try {
      manageKafkaComponents.deleteTopic(
          topicRequest.get("topicName").get(0),
          topicRequest.get("env").get(0),
          topicRequest.get("protocol").get(0),
          topicRequest.get("clusterName").get(0));
    } catch (Exception e) {
      log.error(e.getMessage());
      return new ResponseEntity<>("failure " + e, HttpStatus.OK);
    }

    return new ResponseEntity<>("success", HttpStatus.OK);
  }

  @PostMapping(value = "/createAcls")
  public ResponseEntity<Map<String, String>> createAcls(
      @RequestBody MultiValueMap<String, String> aclRequest) {

    Map<String, String> resultMap = new HashMap<>();
    String result;
    try {
      String aclNativeType = aclRequest.get("aclsNativeType").get(0);

      if (AclsNativeType.NATIVE.name().equals(aclNativeType)) {
        String aclType = aclRequest.get("aclType").get(0);
        if ("Producer".equals(aclType))
          result =
              manageKafkaComponents.updateProducerAcl(
                  aclRequest.get("topicName").get(0),
                  aclRequest.get("env").get(0),
                  aclRequest.get("protocol").get(0),
                  aclRequest.get("clusterName").get(0),
                  aclRequest.get("acl_ip").get(0),
                  aclRequest.get("acl_ssl").get(0),
                  "Create",
                  aclRequest.get("isPrefixAcl").get(0),
                  aclRequest.get("transactionalId").get(0),
                  aclRequest.get("aclIpPrincipleType").get(0),
                  aclRequest.get("aclsNativeType").get(0));
        else
          result =
              manageKafkaComponents.updateConsumerAcl(
                  aclRequest.get("topicName").get(0),
                  aclRequest.get("env").get(0),
                  aclRequest.get("protocol").get(0),
                  aclRequest.get("clusterName").get(0),
                  aclRequest.get("acl_ip").get(0),
                  aclRequest.get("acl_ssl").get(0),
                  aclRequest.get("consumerGroup").get(0),
                  "Create",
                  aclRequest.get("isPrefixAcl").get(0),
                  aclRequest.get("aclIpPrincipleType").get(0),
                  aclRequest.get("aclsNativeType").get(0));
        resultMap.put("result", result);
        return new ResponseEntity<>(resultMap, HttpStatus.OK);
      } else if (AclsNativeType.AIVEN.name().equals(aclNativeType)) {
        resultMap = aivenApiService.createAcls(aclRequest);
        return new ResponseEntity<>(resultMap, HttpStatus.OK);
      }
    } catch (Exception e) {
      resultMap.put("result", "failure " + e.getMessage());
      return new ResponseEntity<>(resultMap, HttpStatus.OK);
    }
    resultMap.put("result", "Not a valid request");
    return new ResponseEntity<>(resultMap, HttpStatus.NOT_FOUND);
  }

  @PostMapping(value = "/deleteAcls")
  public ResponseEntity<Map<String, String>> deleteAcls(
      @RequestBody MultiValueMap<String, String> aclRequest) {
    Map<String, String> resultMap = new HashMap<>();
    String result;
    try {
      String aclNativeType = aclRequest.get("aclsNativeType").get(0);

      if (AclsNativeType.NATIVE.name().equals(aclNativeType)) {
        String aclType = aclRequest.get("aclType").get(0);
        if (aclType.equals("Producer"))
          result =
              manageKafkaComponents.updateProducerAcl(
                  aclRequest.get("topicName").get(0),
                  aclRequest.get("env").get(0),
                  aclRequest.get("protocol").get(0),
                  aclRequest.get("clusterName").get(0),
                  aclRequest.get("acl_ip").get(0),
                  aclRequest.get("acl_ssl").get(0),
                  "Delete",
                  aclRequest.get("isPrefixAcl").get(0),
                  aclRequest.get("transactionalId").get(0),
                  aclRequest.get("aclIpPrincipleType").get(0),
                  aclRequest.get("aclsNativeType").get(0));
        else
          result =
              manageKafkaComponents.updateConsumerAcl(
                  aclRequest.get("topicName").get(0),
                  aclRequest.get("env").get(0),
                  aclRequest.get("protocol").get(0),
                  aclRequest.get("clusterName").get(0),
                  aclRequest.get("acl_ip").get(0),
                  aclRequest.get("acl_ssl").get(0),
                  aclRequest.get("consumerGroup").get(0),
                  "Delete",
                  aclRequest.get("isPrefixAcl").get(0),
                  aclRequest.get("aclIpPrincipleType").get(0),
                  aclRequest.get("aclsNativeType").get(0));
        resultMap.put("result", result);
        return new ResponseEntity<>(resultMap, HttpStatus.OK);
      } else if (AclsNativeType.AIVEN.name().equals(aclNativeType)) {
        result = aivenApiService.deleteAcls(aclRequest);
        resultMap.put("result", result);
        return new ResponseEntity<>(resultMap, HttpStatus.OK);
      }

    } catch (Exception e) {
      resultMap.put("result", "failure " + e.getMessage());
      return new ResponseEntity<>(resultMap, HttpStatus.OK);
    }
    resultMap.put("result", "Not a valid request");
    return new ResponseEntity<>(resultMap, HttpStatus.NOT_FOUND);
  }

  @PostMapping(value = "/postSchema")
  public ResponseEntity<String> postSchema(
      @RequestBody MultiValueMap<String, String> fullSchemaDetails) {
    try {
      String topicName = fullSchemaDetails.get("topicName").get(0);
      String schemaFull = fullSchemaDetails.get("fullSchema").get(0);
      String env = fullSchemaDetails.get("env").get(0);
      String protocol = fullSchemaDetails.get("protocol").get(0);

      String result = schemaService.registerSchema(topicName, schemaFull, env, protocol);
      return new ResponseEntity<>("Status:" + result, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>("failure " + e.getMessage(), HttpStatus.OK);
    }
  }
}
