package com.duong.salesmanagement.model;

public enum ChatMessageType {
    TEXT,    // Normal user message
    SYSTEM,  // Status-change notifications (e.g. "Order confirmed")
    JOINED   // User entered the chat room
}
