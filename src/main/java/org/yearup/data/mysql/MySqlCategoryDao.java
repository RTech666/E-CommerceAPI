package org.yearup.data.mysql;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;
import java.util.List;

@Repository
public class MySqlCategoryDao implements CategoryDao {
    private final JdbcTemplate jdbcTemplate;

    public MySqlCategoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private RowMapper<Category> categoryRowMapper = (rs, rowNum) -> new Category(rs.getInt("category_id"), rs.getString("name"), rs.getString("description"));

    private ResultSetExtractor<Category> categoryExtractor = rs -> rs.next() ? categoryRowMapper.mapRow(rs, 1) : null;

    @Override
    public List<Category> getAllCategories() {
        String sql = "SELECT * FROM categories";
        return jdbcTemplate.query(sql, categoryRowMapper);
    }

    @Override
    public Category getById(int categoryId) {
        String sql = "SELECT * FROM categories WHERE category_id = ?";
        return jdbcTemplate.query(sql, ps -> ps.setInt(1, categoryId), categoryExtractor);
    }

    @Override
    public Category create(Category category) {
        String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";
        jdbcTemplate.update(sql, category.getName(), category.getDescription());
        
        String getLastInsertedIdSql = "SELECT LAST_INSERT_ID()";
        int categoryId = jdbcTemplate.queryForObject(getLastInsertedIdSql, Integer.class);
        return getById(categoryId);
    }

    @Override
    public void update(int categoryId, Category category) {
        String sql = "UPDATE categories SET name = ?, description = ? WHERE category_id = ?";
        jdbcTemplate.update(sql, category.getName(), category.getDescription(), categoryId);
    }

    @Override
    public void delete(int categoryId) {
        String sql = "DELETE FROM categories WHERE category_id = ?";
        jdbcTemplate.update(sql, categoryId);
    }
}
