package service;

import api.ApiClient;
import exception.ApiException;
import model.ApiResponse;
import model.AggregatedRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import util.HttpClientUtil;
import util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiServiceTest
{

  @Mock
  private ApiClient chuckClient;
  @Mock
  private ApiClient randomUserClient;

  private ApiService apiService;

  @BeforeEach
  void setUp()
  {
    when(chuckClient.getSourceName()).thenReturn("chucknorris");
    when(randomUserClient.getSourceName()).thenReturn("randomuser");

    apiService = new ApiService(List.of(chuckClient, randomUserClient), mock(HttpClientUtil.class), new JsonUtil());
  }

  @Test
  void getAvailableApiNames_shouldReturnAllNames()
  {
    List<String> names = apiService.getAvailableApiNames();
    assertEquals(2, names.size());
    assertTrue(names.contains("chucknorris"));
    assertTrue(names.contains("randomuser"));
  }

  @Test
  void getClient_shouldReturnCorrectClient()
  {
    assertSame(chuckClient, apiService.getClient("chucknorris"));
    assertNull(apiService.getClient("unknown"));
  }

  @Test
  void fetchDataFromApis_shouldReturnAggregatedRecords() throws Exception
  {
    JsonNode mockData = new JsonUtil().getMapper().createObjectNode();
    ApiResponse mockResponse = new ApiResponse("chucknorris", Instant.now(), mockData);
    when(chuckClient.fetchData(any())).thenReturn(mockResponse);

    List<AggregatedRecord> records = apiService.fetchDataFromApis(List.of("chucknorris"), new HashMap<>());

    assertEquals(1, records.size());
    AggregatedRecord record = records.get(0);
    assertEquals("chucknorris", record.getSource());
    assertEquals(mockData, record.getData());
  }

  @Test
  void fetchDataFromApis_shouldHandleApiException() throws Exception
  {
    when(chuckClient.fetchData(any())).thenThrow(new ApiException("Error"));

    List<AggregatedRecord> records = apiService.fetchDataFromApis(List.of("chucknorris"), new HashMap<>());

    assertTrue(records.isEmpty());
  }

  @Test
  void fetchDataFromApis_shouldSkipUnknownApi()
  {
    List<AggregatedRecord> records = apiService.fetchDataFromApis(List.of("unknown"), new HashMap<>());
    assertTrue(records.isEmpty());
  }
}
