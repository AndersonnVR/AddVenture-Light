package com.addventure.AddVenture.handler;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;


@Controller
public class GlobalErrorHandler implements ErrorController {
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        
        Object statusCode = request.getAttribute("jakarta.servlet.error.status_code");
        int status = statusCode != null ? Integer.parseInt(statusCode.toString()) : 500;

        model.addAttribute("status", status);

        switch (status) {
            case 403:
                return "error/403";
            case 404:
                return "error/404";
            case 500:
                return "error/500";
            default:
                return "error/generic";
        }
    }
}