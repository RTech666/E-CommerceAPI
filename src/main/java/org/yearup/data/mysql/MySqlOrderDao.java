package org.yearup.data.mysql;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.yearup.data.OrderDao;
import org.yearup.models.Order;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Date;

@Repository
public class MySqlOrderDao implements OrderDao {
    private final JdbcTemplate jdbcTemplate;

    public MySqlOrderDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Order create(Order order) {
        String sql = "INSERT INTO orders (order_id, user_id, date, address, city, state, zip, shipping_amount) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, order.getOrderId());
            ps.setInt(2, order.getUserId());
            ps.setDate(3, new java.sql.Date(order.getDate().getTime()));
            ps.setString(4, order.getAddress());
            ps.setString(5, order.getCity());
            ps.setString(6, order.getState());
            ps.setString(7, order.getZip());
            ps.setDouble(8, order.getShipping());
            return ps;
        }, keyHolder);

        int orderId = keyHolder.getKey().intValue();
        order.setOrderId(orderId);
        return order;
    }
}
