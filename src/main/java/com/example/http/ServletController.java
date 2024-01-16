package com.example.http;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.BufferedReader;
import java.io.IOException;

@Slf4j
@Controller
public class ServletController {
    // HttpServlet 직접 다루기
    @PostMapping("/servlet")
    public void servletHandling(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        BufferedReader reader = request.getReader();
        reader.lines().forEach(log::info);

        response.setStatus(200);
    }
}
