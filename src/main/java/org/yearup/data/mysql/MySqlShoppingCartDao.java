package org.yearup.data.mysql;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class MySqlShoppingCartDao implements ShoppingCartDao
{
    private final JdbcTemplate jdbcTemplate;

    public MySqlShoppingCartDao(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    private RowMapper<ShoppingCartItem> itemRowMapper = (rs, rowNum) -> new ShoppingCartItem(
            rs.getInt("user_id"),
            rs.getInt("product_id"),
            rs.getInt("quantity")
    );

    @Override
    public ShoppingCart getCartByUserId(int userId)
    {
        String sql = "SELECT * FROM shopping_cart WHERE user_id = ?";
        List<ShoppingCartItem> items = jdbcTemplate.query(sql, ps -> ps.setInt(1, userId), itemRowMapper);

        Map<Integer, ShoppingCartItem> itemMap = new HashMap<>();
        for (ShoppingCartItem item : items)
        {
            itemMap.put(item.getProductId(), item);
        }

        ShoppingCart cart = new ShoppingCart();
        cart.setUserId(userId);
        cart.setItems(itemMap);

        return cart;
    }

    @Override
    public ShoppingCartItem getItemByUserIdAndProductId(int userId, int productId)
    {
        String sql = "SELECT * FROM shopping_cart WHERE user_id = ? AND product_id = ?";
        List<ShoppingCartItem> items = jdbcTemplate.query(sql, ps -> {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
        }, itemRowMapper);

        return items.isEmpty() ? null : items.get(0);
    }

    @Override
    public void addItem(ShoppingCartItem item)
    {
        String sql = "INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, item.getUserId(), item.getProductId(), item.getQuantity());
    }

    @Override
    public void updateItem(ShoppingCartItem item)
    {
        String sql = "UPDATE shopping_cart SET quantity = ? WHERE user_id = ? AND product_id = ?";
        jdbcTemplate.update(sql, item.getQuantity(), item.getUserId(), item.getProductId());
    }

    @Override
    public void clearCart(int userId) {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }
}
