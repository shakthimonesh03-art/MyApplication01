package com.ticketing.mvp.entity;

public class Enums {
    public enum Role { CUSTOMER, ADMIN }
    public enum SeatStatus { AVAILABLE, HELD, BOOKED, BLOCKED }
    public enum BookingStatus { PENDING, PAYMENT_IN_PROGRESS, CONFIRMED, FAILED, CANCELLED, EXPIRED }
    public enum PaymentStatus { INITIATED, SUCCESS, FAILED }
}
