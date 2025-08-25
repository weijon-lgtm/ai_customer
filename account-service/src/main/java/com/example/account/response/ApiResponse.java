// common/src/main/java/com/example/common/response/ApiResponse.java
package com.example.account.response;

public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    private boolean success;

    // 构造函数
    public ApiResponse() {}

    public ApiResponse(int code, String message, T data, boolean success) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.success = success;
    }

    // 成功响应
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "Success", data, true);
    }

    // 错误响应
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(400, message, null, false);
    }

    // 错误响应（带状态码）
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null, false);
    }

    // Getter 和 Setter
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
