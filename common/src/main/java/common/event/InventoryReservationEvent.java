package common.event;

import java.util.UUID;

public record InventoryReservationEvent(UUID orderId, UUID eventId, String status){}
