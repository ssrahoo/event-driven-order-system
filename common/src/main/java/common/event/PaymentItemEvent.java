package common.event;

import java.math.BigDecimal;

public record PaymentItemEvent(String productId, Integer quantity){}
