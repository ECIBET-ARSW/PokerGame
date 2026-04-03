package eci.edu.co.pokerservice.response;

import lombok.Builder;

@Builder
public class ApiResponse<T>{
    private T data;


}
