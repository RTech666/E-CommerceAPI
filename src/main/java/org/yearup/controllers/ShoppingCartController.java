package org.yearup.controllers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.models.User;
import java.security.Principal;

@RestController
@RequestMapping("/cart")
@CrossOrigin
@PreAuthorize("isAuthenticated()")
public class ShoppingCartController {
    private ShoppingCartDao shoppingCartDao;
    private UserDao userDao;
    private ProductDao productDao;

    @Autowired
    public ShoppingCartController(ShoppingCartDao shoppingCartDao, UserDao userDao, ProductDao productDao) {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.productDao = productDao;
    }

    @GetMapping("")
    public ShoppingCart getCart(Principal principal) {
        try {
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            int userId = user.getId();
            
            System.out.println("User ID: " + userId);
            
            ShoppingCart cart = shoppingCartDao.getCartByUserId(userId);
            if (cart == null) {
                cart = new ShoppingCart();
                System.out.println("Cart is empty and newly created.");
            } else {
                System.out.println("Cart items: " + cart.getItems());
            }
    
            for (ShoppingCartItem item : cart.getItems().values()) {
                if (item.getProduct() == null) {
                    item.setProduct(productDao.getById(item.getProductId()));
                    if (item.getProduct() == null) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found for item: " + item.getProductId());
                    }
                }
            }
    
            return cart;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.", e);
        }
    }

    @PostMapping("/products/{productId}")
    public ResponseEntity<Void> addProductToCart(Principal principal, @PathVariable int productId, @RequestBody(required = false) ShoppingCartItem item) {
        try {
            if (item == null) {
                System.out.println("Request body is empty.");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body cannot be empty");
            }
            
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            int userId = user.getId();
    
            Product product = productDao.getById(productId);
            if (product == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
            }
    
            item.setUserId(userId);
            item.setProduct(product);
    
            ShoppingCartItem existingItem = shoppingCartDao.getItemByUserIdAndProductId(userId, productId);
            if (existingItem == null) {
                item.setQuantity(1);
                shoppingCartDao.addItem(item);
            } else {
                existingItem.setQuantity(existingItem.getQuantity() + 1);
                shoppingCartDao.updateItem(existingItem);
            }
    
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.", e);
        }
    }

    @PutMapping("/products/{productId}")
    public ResponseEntity<Void> updateProductInCart(Principal principal, @PathVariable int productId, @RequestBody ShoppingCartItem item)
    {
        try
        {
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            item.setUserId(userId);
            item.setProductId(productId);

            shoppingCartDao.updateItem(item);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        catch(Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.", e);
        }
    }

    @DeleteMapping("")
    public ResponseEntity<Void> clearCart(Principal principal) {
        try {
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            int userId = user.getId();
    
            shoppingCartDao.clearCart(userId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.", e);
        }
    }
}
