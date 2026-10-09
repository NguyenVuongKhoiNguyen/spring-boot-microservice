package com.poly.payment.dtos.responses;

public record ErrorResponse(int status, String error, String message) {}
