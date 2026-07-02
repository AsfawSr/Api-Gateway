package com.asfaw.orderservice.service;

import com.asfaw.orderservice.dto.OrderDto;
import com.asfaw.orderservice.dto.OrderRequest;
import com.asfaw.orderservice.entity.Order;
import com.asfaw.orderservice.entity.OrderItem;
import com.asfaw.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public OrderDto createOrder(OrderRequest.CreateOrderRequest request) {
        Order order = Order.builder()
                .userId(request.getUserId())
                .shippingAddress(request.getShippingAddress())
                .orderNotes(request.getOrderNotes())
                .build();

        List<OrderItem> items = request.getItems().stream().map(itemReq -> {
            BigDecimal totalPrice = itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            return OrderItem.builder()
                    .order(order)
                    .productId(itemReq.getProductId())
                    .productName(itemReq.getProductName())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(itemReq.getUnitPrice())
                    .totalPrice(totalPrice)
                    .build();
        }).collect(Collectors.toList());

        order.setItems(items);
        BigDecimal total = items.stream().map(OrderItem::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);

        return toDto(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long id) {
        return toDto(orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id)));
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public OrderDto updateStatus(Long id, OrderRequest.UpdateStatusRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
        order.setStatus(request.getStatus());
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public void cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    private OrderDto toDto(Order order) {
        List<OrderDto.OrderItemDto> itemDtos = order.getItems().stream()
                .map(i -> OrderDto.OrderItemDto.builder()
                        .id(i.getId())
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .totalPrice(i.getTotalPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderDto.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .orderNotes(order.getOrderNotes())
                .items(itemDtos)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
