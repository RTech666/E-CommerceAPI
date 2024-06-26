package org.yearup.data.mysql;
import org.springframework.stereotype.Component;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao {

    public MySqlShoppingCartDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public ShoppingCart getByUserId(int userId) {
        ShoppingCart shoppingCart = new ShoppingCart();
        ShoppingCartItem item = new ShoppingCartItem();

        String query = "SELECT products.product_id, name, price, category_id, description, " + "color, image_url, stock, featured, quantity FROM " + "shopping_cart JOIN products ON (shopping_cart.product_id = products.product_id) WHERE shopping_cart.user_id = ?";

        try(Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query);){

            preparedStatement.setInt(1, userId);

            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()){
                    do{
                        Product product = mapRow(resultSet);
                        item.setProduct(product);
                        item.setQuantity(resultSet.getInt("quantity"));
                        shoppingCart.add(item);
                    } while (resultSet.next());
                }
                return shoppingCart;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addProduct(int userId, int productId, int quantity) {
        String query = "INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, ?)";

        try(Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)){

            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, productId);
            preparedStatement.setInt(3,quantity);

            int rows = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void updateProduct(int userId, int productId, int quantity) {

        String query = "UPDATE shopping_cart SET quantity = ? WHERE user_id = ? AND product_id = ?";

        try(Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)){

            preparedStatement.setInt(1, quantity);
            preparedStatement.setInt(2, userId);
            preparedStatement.setInt(3, productId);

            int rows = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clearCart(int userId) {
        String query = "DELETE FROM shopping_cart WHERE user_id = ?";

        try(Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)){

            preparedStatement.setInt(1, userId);

            int rows = preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected static Product mapRow(ResultSet row) throws SQLException
    {
        int productId = row.getInt("product_id");
        String name = row.getString("name");
        BigDecimal price = row.getBigDecimal("price");
        int categoryId = row.getInt("category_id");
        String description = row.getString("description");
        String color = row.getString("color");
        int stock = row.getInt("stock");
        boolean isFeatured = row.getBoolean("featured");
        String imageUrl = row.getString("image_url");

        return new Product(productId, name, price, categoryId, description, color, stock, isFeatured, imageUrl);
    }
}