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
import java.util.HashMap;
import java.util.Map;

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
            
            ShoppingCart cart = shoppingCartDao.getByUserId(userId);
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

    @PostMapping("/products/{id}")
    @PreAuthorize("isAuthenticated()")
    public ShoppingCartItem addProductToCart(@PathVariable int id, Principal principal){

        int userId = getUserId(principal);

        ShoppingCart shoppingCart = shoppingCartDao.getByUserId(userId);

        if (!shoppingCart.contains(id)) {
            shoppingCartDao.addProduct(userId,id,1);
            shoppingCart = shoppingCartDao.getByUserId(userId);

        } else {
            shoppingCartDao.updateProduct(userId, id, shoppingCart.get(id).getQuantity() + 1);
            shoppingCart = shoppingCartDao.getByUserId(userId);
        }
        return shoppingCart.get(id);
    }

    private int getUserId(Principal principal) {
        String userName = principal.getName();
        User user = userDao.getByUserName(userName);
        return user.getId();
    }

    @PutMapping("/products/{id}")
    @PreAuthorize("isAuthenticated()")
    public void updateCartProduct(@PathVariable int id, Principal principal, @RequestBody ShoppingCartItem item){
        int userId = getUserId(principal);

        ShoppingCart shoppingCart = shoppingCartDao.getByUserId(userId);

        if (shoppingCart.contains(id)){
            shoppingCartDao.updateProduct(userId, id, item.getQuantity());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item doesn't exist to update.");
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
