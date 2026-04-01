package eci.edu.co.pokerservice.model.response;

import lombok.Builder;

@Builder
public class ApiResponse<T>{
    private T data;


}
