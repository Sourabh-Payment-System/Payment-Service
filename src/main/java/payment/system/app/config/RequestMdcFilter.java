package payment.system.app.config;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import static payment.system.app.constants.LogMessages.*;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestMdcFilter
        extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        try {
        	String requestId =
        	        Optional.ofNullable(
        	                request.getHeader("X-Correlation-Id"))
        	        .orElse(UUID.randomUUID().toString());
            MDC.put(
            		MDC_REQUEST_ID,requestId);
            MDC.put(MDC_REQUEST_URI, request.getRequestURI());
            MDC.put(MDC_HTTP_METHOD, request.getMethod());
            
            response.setHeader(
            	    "X-Correlation-Id",
            	    requestId);

            filterChain.doFilter(
                    request,
                    response);

        } finally {

        	    MDC.remove(MDC_REQUEST_ID);
        	    MDC.remove(MDC_REQUEST_URI);
        	    MDC.remove(MDC_HTTP_METHOD);
        	
        }
    }
}
