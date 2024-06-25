package org.yearup.data.mysql;
import org.springframework.stereotype.Component;
import org.yearup.models.OrderLineItem;
import org.yearup.data.OrderLineItemDao;
import javax.sql.DataSource;
import java.sql.*;

@Component
public class MySqlOrderLineItemDao extends MySqlDaoBase implements OrderLineItemDao {
    public MySqlOrderLineItemDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void create(OrderLineItem orderLineItem) {
        String sql = "INSERT INTO order_line_items (order_id, product_id, quantity, unit_price, discount_percent, total) " + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, orderLineItem.getOrderId());
            ps.setInt(2, orderLineItem.getProductId());
            ps.setInt(3, orderLineItem.getQuantity());
            ps.setBigDecimal(4, orderLineItem.getUnitPrice());
            ps.setBigDecimal(5, orderLineItem.getDiscountPercent());
            ps.setBigDecimal(6, orderLineItem.getTotal());

            ps.executeUpdate();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}