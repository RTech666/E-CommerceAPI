package org.yearup.data;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
public interface ShoppingCartDao {
    ShoppingCart getCartByUserId(int userId);
    ShoppingCartItem getItemByUserIdAndProductId(int userId, int productId);
    void addItem(ShoppingCartItem item);
    void updateItem(ShoppingCartItem item);
    void clearCart(int userId);
}