import java.util.*;
import java.time.*;

interface Expirable {
    LocalDate getExpiryDate();
}

interface Shippable {
    double getWeight();
    String getName();
}

abstract class Product {
    String name;
    double price;
    int quantity;

    public Product(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public void reduceQuantity(int amount) { this.quantity -= amount; }
}

class Cheese extends Product implements Expirable, Shippable {
    private LocalDate expiryDate;
    private double weight;

    public Cheese(String name, double price, int quantity, double weight, LocalDate expiryDate) {
        super(name, price, quantity);
        this.weight = weight;
        this.expiryDate = expiryDate;
    }

    public double getWeight() { return weight; }
    public LocalDate getExpiryDate() { return expiryDate; }
}

class Biscuits extends Product implements Expirable, Shippable {
    private LocalDate expiryDate;
    private double weight;

    public Biscuits(String name, double price, int quantity, double weight, LocalDate expiryDate) {
        super(name, price, quantity);
        this.weight = weight;
        this.expiryDate = expiryDate;
    }

    public double getWeight() { return weight; }
    public LocalDate getExpiryDate() { return expiryDate; }
}

class TV extends Product implements Shippable {
    private double weight;

    public TV(String name, double price, int quantity, double weight) {
        super(name, price, quantity);
        this.weight = weight;
    }

    public double getWeight() { return weight; }
}

class Mobile extends Product {
    public Mobile(String name, double price, int quantity) {
        super(name, price, quantity);
    }
}

class ScratchCard extends Product {
    public ScratchCard(String name, double price, int quantity) {
        super(name, price, quantity);
    }
}

class Customer {
    String name;
    double balance;

    public Customer(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    public boolean canAfford(double amount) {
        return balance >= amount;
    }

    public void deduct(double amount) {
        this.balance -= amount;
    }

    public double getBalance() {
        return balance;
    }
}

class CartItem {
    Product product;
    int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return product.getPrice() * quantity;
    }

    public String toString() {
        return quantity + "x " + product.getName() + " " + product.getPrice() * quantity;
    }
}

class Cart {
    List<CartItem> items = new ArrayList<>();

    public void add(Product product, int quantity) {
        if (quantity > product.getQuantity()) {
            throw new IllegalArgumentException("Not enough stock for " + product.getName());
        }
        items.add(new CartItem(product, quantity));
    }

    public List<CartItem> getItems() {
        return items;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public double getSubtotal() {
        return items.stream().mapToDouble(CartItem::getTotalPrice).sum();
    }

    public double getShippingWeight() {
        double totalWeight = 0;
        for (CartItem item : items) {
            if (item.product instanceof Shippable) {
                Shippable s = (Shippable) item.product;
                totalWeight += s.getWeight() * item.quantity;
            }
        }
        return totalWeight;
    }

    public List<Shippable> getShippableItems() {
        List<Shippable> shippables = new ArrayList<>();
        for (CartItem item : items) {
            if (item.product instanceof Shippable) {
                for (int i = 0; i < item.quantity; i++) {
                    shippables.add((Shippable) item.product);
                }
            }
        }
        return shippables;
    }
}

class ShippingService {
    public static void ship(List<Shippable> items) {
        if (items.isEmpty()) return;

        System.out.println("** Shipment notice **");
        Map<String, Integer> nameCount = new HashMap<>();
        double totalWeight = 0;

        for (Shippable item : items) {
            nameCount.put(item.getName(), nameCount.getOrDefault(item.getName(), 0) + 1);
            totalWeight += item.getWeight();
        }

        for (var entry : nameCount.entrySet()) {
            System.out.println(entry.getValue() + "x " + entry.getKey());
        }

        System.out.println("Total package weight " + totalWeight + "kg");
    }
}

public class ECommerceSystem {
    public static void checkout(Customer customer, Cart cart) {
        if (cart.isEmpty()) throw new RuntimeException("Cart is empty!");

        // check for expired or out-of-stock
        for (CartItem item : cart.getItems()) {
            if (item.product instanceof Expirable) {
                Expirable e = (Expirable) item.product;
                if (e.getExpiryDate().isBefore(LocalDate.now())) {
                    throw new RuntimeException(item.product.getName() + " is expired!");
                }
            }

            if (item.quantity > item.product.getQuantity()) {
                throw new RuntimeException(item.product.getName() + " is out of stock!");
            }
        }

        double subtotal = cart.getSubtotal();
        double shippingFee = cart.getShippingWeight() > 0 ? 30 : 0;
        double total = subtotal + shippingFee;

        if (!customer.canAfford(total)) {
            throw new RuntimeException("Insufficient balance!");
        }

        // Deduct balance
        customer.deduct(total);

        // Reduce product quantities
        for (CartItem item : cart.getItems()) {
            item.product.reduceQuantity(item.quantity);
        }

        // Shipping
        List<Shippable> toShip = cart.getShippableItems();
        ShippingService.ship(toShip);

        // Print receipt
        System.out.println("** Checkout receipt **");
        for (CartItem item : cart.getItems()) {
            System.out.println(item);
        }
        System.out.println("----------------------");
        System.out.println("Subtotal " + subtotal);
        System.out.println("Shipping " + shippingFee);
        System.out.println("Amount " + total);
        System.out.println("Customer balance: " + customer.getBalance());
    }

    public static void main(String[] args) {
        Product cheese = new Cheese("Cheese 400g", 100, 5, 0.2, LocalDate.of(2025, 7, 20));
        Product biscuits = new Biscuits("Biscuits 700g", 150, 3, 0.7, LocalDate.of(2025, 7, 30));
        Product tv = new TV("TV", 500, 3, 5);
        Product scratchCard = new ScratchCard("Scratch Card", 50, 10);

        Customer fatma = new Customer("Fatma", 1000);
        Cart cart = new Cart();

        cart.add(cheese, 2);
        cart.add(biscuits, 1);
        cart.add(scratchCard, 1);

        checkout(fatma, cart);
    }
}
