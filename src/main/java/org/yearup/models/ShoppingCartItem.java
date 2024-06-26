package org.yearup.models;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;

public class ShoppingCartItem {
    private int userId;
    private int productId;
    private Integer quantity;
    private BigDecimal discountPercent = BigDecimal.ZERO;
    private BigDecimal lineTotal;

    private Product product;

    public ShoppingCartItem() {}

    public ShoppingCartItem(int userId, int productId, int quantity) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(BigDecimal discountPercent) {
        this.discountPercent = discountPercent;
    }

    @JsonIgnore
    public int getProductId() {
        return product != null ? product.getProductId() : productId;
    }

    public BigDecimal getLineTotal() {
        BigDecimal basePrice = product.getPrice();
        BigDecimal quantity = new BigDecimal(this.quantity);

        BigDecimal subTotal = basePrice.multiply(quantity);
        BigDecimal discountAmount = subTotal.multiply(discountPercent);

        return subTotal.subtract(discountAmount);
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }
}