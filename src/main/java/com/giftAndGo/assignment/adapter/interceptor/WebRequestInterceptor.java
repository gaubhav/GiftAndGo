package com.giftAndGo.assignment.adapter.interceptor;

import com.giftAndGo.assignment.domain.model.FileProcessingRequest;
import com.giftAndGo.assignment.adapter.repository.FileProcessingRequestLogRepository;
import com.giftAndGo.assignment.web.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class WebRequestInterceptor implements HandlerInterceptor {

    @Autowired
    private FileProcessingRequestLogRepository requestLogRepository;

    @Autowired
    private RequestContext requestContext;


    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("requestId", UUID.randomUUID());
        request.setAttribute("requestStartTime", System.currentTimeMillis());
        request.setAttribute("requestTimestamp", LocalDateTime.now());
        return true;
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UUID requestId = (UUID) request.getAttribute("requestId");
        LocalDateTime requestTimestamp = (LocalDateTime) request.getAttribute("requestTimestamp");
        long timeLapsed = getTimeLapsed((long) request.getAttribute("requestStartTime"));

        String requestUri = request.getRequestURI();
        int responseCode = response.getStatus();

        String requestIpAddress = requestContext.getIpAddress();
        String requestCountryCode = requestContext.getCountryCode();
        String requestIpProvider = requestContext.getIsp();

        FileProcessingRequest logEntry = new FileProcessingRequest(
                requestId,
                requestUri,
                requestTimestamp,
                responseCode,
                requestIpAddress,
                requestCountryCode,
                requestIpProvider,
                timeLapsed
        );

        requestLogRepository.save(logEntry);
    }

    private static long getTimeLapsed(long startTime) {
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }
}
