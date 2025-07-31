package com.lexiai.exception;

public class WebScrapingException extends RuntimeException {
    
    public WebScrapingException(String message) {
        super(message);
    }
    
    public WebScrapingException(String message, Throwable cause) {
        super(message, cause);
    }
}
