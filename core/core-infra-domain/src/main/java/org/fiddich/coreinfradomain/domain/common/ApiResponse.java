package org.fiddich.coreinfradomain.domain.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor // 모든 필드를 파라미터로 받는 생성자
@JsonPropertyOrder({"statusCode", "message", "content"})
public class ApiResponse<T> {

    @JsonProperty("statusCode")
    @NonNull
    private String statusCode;

    @JsonProperty("message")
    @NonNull
    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("content")
    private T content; // 필요 시 확장 가능

    // 성공한 경우 응답 생성
    public static <T> ApiResponse<T> onSuccess(T content) {
        return new ApiResponse<>(HttpStatus.OK.name(), HttpStatus.OK.getReasonPhrase(), content);
    }

    // 실패한 경우 응답 생성
    public static <T> ApiResponse<T> onFailure(String statusCode, String message) {
        return new ApiResponse<>(statusCode, message, null);
    }

    // 실패했을때 컨텐츠도 같이 반환
    public static <T> ApiResponse<T> onFailure(String statusCode, String message, T content) {
        return new ApiResponse<>(statusCode, message, content);
    }

    // Json serialize
    // json을 문자열로
    public String toJsonString() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }
}

