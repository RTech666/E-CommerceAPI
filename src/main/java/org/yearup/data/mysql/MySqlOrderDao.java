package org.yearup.data.mysql;
import org.springframework.stereotype.Component;
import org.yearup.models.Order;
import org.yearup.data.OrderDao;
import javax.sql.DataSource;
import java.sql.*;

@Component
public class MySqlOrderDao extends MySqlDaoBase implements OrderDao {
    public MySqlOrderDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Order create(Order order) {
        String sql = "INSERT INTO orders (user_id, total) VALUES (?, ?)";

        try (Connection connection = getConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, order.getUserId());
            ps.setBigDecimal(2, order.getTotal());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next())
            {
                order.setOrderId(rs.getInt(1));
            }

            return order;
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}