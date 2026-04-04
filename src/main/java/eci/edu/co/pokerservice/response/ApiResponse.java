package eci.edu.co.pokerservice.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T>{
    private int code;
    private T data;
    private String message;

}
