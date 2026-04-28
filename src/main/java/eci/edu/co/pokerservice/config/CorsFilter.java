package eci.edu.co.pokerservice.config;



import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import java.io.IOException;



@Component
public class CorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        httpResponse.setHeader("Access-Control-Allow-Origin", "https://ecibet-front.vercel.app");
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE, PUT");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");

        chain.doFilter(request, response);
    }
}
