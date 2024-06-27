class ShoppingCartService {

    cart = {
        items:[],
        total:0
    };

    addToCart(productId) {
        const url = `${config.baseUrl}/cart/products/${productId}`;
    
        axios.post(url, {})
            .then(response => {
                console.log("Server response:", response);
    
                if (response.data) {
                    this.setCart(response.data, true);
                    this.updateCartDisplay();
                } else {
                    throw new Error("No data received from server.");
                }
            })
            .catch(error => {
                let errorMessage = "Add to cart failed.";
                if (error.response) {
                    errorMessage += ` Server responded with status: ${error.response.status}. ${error.response.data}`;
                } else if (error.request) {
                    errorMessage += " No response received from server.";
                } else {
                    errorMessage += ` Error setting up request: ${error.message}`;
                }
                console.error(errorMessage);
                const data = { error: errorMessage };
                templateBuilder.append("error", data, "errors");
            });
    }

    setCart(data, isSingleItem = false)
    {
        if (isSingleItem) {
            const existingItemIndex = this.cart.items.findIndex(item => item.product.id === data.product.id);
            if (existingItemIndex !== -1) {
                this.cart.items[existingItemIndex].quantity = data.quantity;
                this.cart.items[existingItemIndex].lineTotal = data.lineTotal;
            } else {
                this.cart.items.push(data);
            }
            this.cart.total += data.lineTotal;
        } else {
            this.cart = {
                items: [],
                total: 0
            };

            if (data && data.items) {
                this.cart.total = data.total || 0;

                for (const [key, value] of Object.entries(data.items)) {
                    this.cart.items.push(value);
                }
            } else {
                console.error("Invalid cart data received:", data);
                throw new Error("Invalid cart data received from server.");
            }
        }
    }

    loadCart() {
        const url = `${config.baseUrl}/cart`;

        axios.get(url)
            .then(response => {
                this.setCart(response.data);
                this.updateCartDisplay();
            })
            .catch(error => {
                const data = { error: "Load cart failed." };
                templateBuilder.append("error", data, "errors");
            });
    }

    loadCartPage() {
        const main = document.getElementById("main");
        main.innerHTML = "";

        let div = document.createElement("div");
        div.classList = "filter-box";
        main.appendChild(div);

        const contentDiv = document.createElement("div");
        contentDiv.id = "content";
        contentDiv.classList.add("content-form");

        const cartHeader = document.createElement("div");
        cartHeader.classList.add("cart-header");

        const h1 = document.createElement("h1");
        h1.innerText = "Cart";
        cartHeader.appendChild(h1);

        const button = document.createElement("button");
        button.classList.add("btn", "btn-danger");
        button.innerText = "Clear";
        button.addEventListener("click", () => this.clearCart());
        cartHeader.appendChild(button);

        contentDiv.appendChild(cartHeader);
        main.appendChild(contentDiv);

        this.cart.items.forEach(item => {
            this.buildItem(item, contentDiv);
        });
    }

    buildItem(item, parent) {
        let outerDiv = document.createElement("div");
        outerDiv.classList.add("cart-item");

        let div = document.createElement("div");
        outerDiv.appendChild(div);
        let h4 = document.createElement("h4");
        h4.innerText = item.product.name;
        div.appendChild(h4);

        let photoDiv = document.createElement("div");
        photoDiv.classList.add("photo");
        let img = document.createElement("img");
        img.src = `/images/products/${item.product.imageUrl}`;
        img.addEventListener("click", () => {
            showImageDetailForm(item.product.name, img.src);
        });
        photoDiv.appendChild(img);
        let priceH4 = document.createElement("h4");
        priceH4.classList.add("price");
        priceH4.innerText = `$${item.product.price}`;
        photoDiv.appendChild(priceH4);
        outerDiv.appendChild(photoDiv);

        let descriptionDiv = document.createElement("div");
        descriptionDiv.innerText = item.product.description;
        outerDiv.appendChild(descriptionDiv);

        let quantityDiv = document.createElement("div");
        quantityDiv.innerText = `Quantity: ${item.quantity}`;
        outerDiv.appendChild(quantityDiv);

        parent.appendChild(outerDiv);
    }

    clearCart() {
        const url = `${config.baseUrl}/cart`;
    
        axios.delete(url)
             .then(response => {
                 if (response.status === 204) {
                     this.cart = {
                         items: [],
                         total: 0
                     };
    
                     this.updateCartDisplay();
                     this.loadCartPage();
                 } else {
                     throw new Error("Unexpected response status: " + response.status);
                 }
             })
             .catch(error => {
                 let errorMessage = "Empty cart failed.";
                 if (error.response) {
                     errorMessage += ` Server responded with status: ${error.response.status}. ${error.response.data}`;
                 } else if (error.request) {
                     errorMessage += " No response received from server.";
                 } else {
                     errorMessage += ` Error setting up request: ${error.message}`;
                 }
                 console.error(errorMessage);
                 const data = { error: errorMessage };
                 templateBuilder.append("error", data, "errors");
             });
    }

    updateCartDisplay() {
        try {
            const cartControl = document.getElementById("cart-items");
            const itemCount = this.cart.items.reduce((acc, item) => acc + item.quantity, 0);

            cartControl.innerText = itemCount;
        } catch (e) {
            console.error(e);
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    cartService = new ShoppingCartService();

    if(userService.isLoggedIn()) {
        cartService.loadCart();
    }
});
