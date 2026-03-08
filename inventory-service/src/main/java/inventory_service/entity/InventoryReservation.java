package inventory_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name="inventory_reservations")
public class InventoryReservation {
    @Id
    @Column(name = "inventory_reservation_id")
    private UUID InventoryReservationId;

    private UUID orderId;
    private String productId;
    private Integer quantity;

    public InventoryReservation() {
    }

    public InventoryReservation(UUID inventoryReservationId, UUID orderId, String productId, Integer quantity) {
        InventoryReservationId = inventoryReservationId;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public UUID getInventoryReservationId() {
        return InventoryReservationId;
    }

    public void setInventoryReservationId(UUID inventoryReservationId) {
        InventoryReservationId = inventoryReservationId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
