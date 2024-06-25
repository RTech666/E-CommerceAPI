package org.yearup.data.mysql;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import org.yearup.data.ProductDao;
import org.yearup.models.Product;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MySqlProductDao implements ProductDao {

    private final JdbcTemplate jdbcTemplate;

    public MySqlProductDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private RowMapper<Product> productRowMapper = (rs, rowNum) -> new Product (
        rs.getInt("product_id"),
        rs.getString("name"),
        rs.getBigDecimal("price"),
        rs.getInt("category_id"),
        rs.getString("description"),
        rs.getString("color"),
        rs.getString("image_url"),
        rs.getInt("stock"),
        rs.getBoolean("featured")
    );

    private ResultSetExtractor<Product> productExtractor = rs -> rs.next() ? productRowMapper.mapRow(rs, 1) : null;

    @Override
    public List<Product> search(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String color) {
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (categoryId != null) {
            sql.append(" AND category_id = ?");
            params.add(categoryId);
        }

        if (minPrice != null) {
            sql.append(" AND price >= ?");
            params.add(minPrice);
        }

        if (maxPrice != null) {
            sql.append(" AND price <= ?");
            params.add(maxPrice);
        }

        if (color != null && !color.isEmpty()) {
            sql.append(" AND color = ?");
            params.add(color);
        }

        return jdbcTemplate.query(sql.toString(), new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
            }
        }, productRowMapper);
    }

    @Override
    public List<Product> listByCategoryId(int categoryId) {
        String sql = "SELECT * FROM products WHERE category_id = ?";
        return jdbcTemplate.query(sql, ps -> ps.setInt(1, categoryId), productRowMapper);
    }

    @Override
    public Product getById(int productId) {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        return jdbcTemplate.query(sql, ps -> ps.setInt(1, productId), productExtractor);
    }

    @Override
    public Product create(Product product) {
        String sql = "INSERT INTO products (name, price, color, description, category_id) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, product.getName(), product.getPrice(), product.getColor(), product.getDescription(), product.getCategoryId());

        String getLastInsertedIdSql = "SELECT LAST_INSERT_ID()";
        int productId = jdbcTemplate.queryForObject(getLastInsertedIdSql, Integer.class);
        return getById(productId);
    }

    @Override
    public void update(int productId, Product product) {
        String sql = "UPDATE products SET name = ?, price = ?, color = ?, description = ?, category_id = ? WHERE product_id = ?";
        jdbcTemplate.update(sql, product.getName(), product.getPrice(), product.getColor(), product.getDescription(), product.getCategoryId(), productId);
    }

    @Override
    public void delete(int productId) {
        String sql = "DELETE FROM products WHERE product_id = ?";
        jdbcTemplate.update(sql, productId);
    }
}
