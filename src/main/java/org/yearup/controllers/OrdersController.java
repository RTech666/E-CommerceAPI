package org.yearup.controllers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.OrderDao;
import org.yearup.data.OrderLineItemDao;
import org.yearup.data.ProfileDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.Order;
import org.yearup.models.OrderLineItem;
import org.yearup.models.Profile;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.models.User;
import java.security.Principal;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/orders")
@CrossOrigin
@PreAuthorize("isAuthenticated()")
public class OrdersController {
    private final OrderDao orderDao;
    private final OrderLineItemDao orderLineItemDao;
    private final ShoppingCartDao shoppingCartDao;
    private final UserDao userDao;
    private final ProfileDao profileDao;

    @Autowired
    public OrdersController(OrderDao orderDao, OrderLineItemDao orderLineItemDao, ShoppingCartDao shoppingCartDao, UserDao userDao, ProfileDao profileDao) {
        this.orderDao = orderDao;
        this.orderLineItemDao = orderLineItemDao;
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.profileDao = profileDao;
    }

    @PostMapping("")
    public ResponseEntity<Order> checkout(Principal principal) {
        try {
            String userName = principal.getName();
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            ShoppingCart cart = shoppingCartDao.getByUserId(userId);
            if (cart == null || cart.getItems().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shopping cart is empty");
            }

            Profile profile = profileDao.getByUserId(userId);

            Order order = new Order();
            order.setUserId(userId);
            order.setTotal(cart.getTotal().doubleValue());
            order.setDate(new Date());
            order.setAddress(profile.getAddress());
            order.setCity(profile.getCity());
            order.setState(profile.getState());
            order.setZip(profile.getZip());
            order.setShipping(order.getShipping());

            Order createdOrder = orderDao.create(order);

            for (ShoppingCartItem item : cart.getItems().values()) {
                OrderLineItem orderLineItem = new OrderLineItem();
                orderLineItem.setOrderId(createdOrder.getOrderId());
                orderLineItem.setProductId(item.getProductId());
                orderLineItem.setQuantity(item.getQuantity());
                orderLineItem.setUnitPrice(item.getProduct().getPrice());
                orderLineItem.setDiscountPercent(item.getDiscountPercent());
                orderLineItem.setTotal(item.getLineTotal());

                orderLineItemDao.create(orderLineItem);
            }

            shoppingCartDao.clearCart(userId);

            return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to process order", e);
        }
    }
}
