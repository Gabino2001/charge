package com.charge.backend.dto;

public class AlertDtos {

    public record PlayerAlert(String type, String message, String severity) {}
}
