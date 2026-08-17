package payment.system.app.config;

import java.io.IOException;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

@Component
public class JwtPropagationInterceptor
        implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution)
            throws IOException {

        org.springframework.web.context.request.RequestAttributes
                attributes =
                org.springframework.web.context.request
                        .RequestContextHolder
                        .getRequestAttributes();

        if (attributes instanceof
                org.springframework.web.context.request
                        .ServletRequestAttributes servletAttributes) {

            jakarta.servlet.http.HttpServletRequest
                    currentRequest =
                    servletAttributes.getRequest();

            String authorization =
                    currentRequest.getHeader(
                            "Authorization");

            if (authorization != null
                    && authorization.startsWith("Bearer ")) {

                request.getHeaders().set(
                        "Authorization",
                        authorization);
            }
        }

        return execution.execute(
                request,
                body);
    }
}