package com.poly.minio.dtos.responses;

public record ErrorResponse(int status, String error, String message) {}
