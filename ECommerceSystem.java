import java.time.LocalDate;
import java.util.*;

public class ECommerceSystem {

    // Abstract Product class
    public static abstract class Product {
        protected String name;
        protected double price;
        protected int quantity;

        public Product(String name, double price, int quantity) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }
        public String getName() { return name; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
        public void reduceQuantity(int amount) { this.quantity -= amount; }
        public boolean isExpired() { return false; }
        public boolean isShippable() { return false; }
    }

    // Expirable interface
    public interface Expirable {
        boolean isExpired();
    }

    // Shippable interface
    public interface Shippable {
        String getName();
        double getWeight();
    }

    // Cheese class
    public static class Cheese extends Product implements Expirable, Shippable {
        private LocalDate expiryDate;
        private double weight;

        public Cheese(String name, double price, int quantity, LocalDate expiryDate, double weight) {
            super(name, price, quantity);
            this.expiryDate = expiryDate;
            this.weight = weight;
        }
        @Override
        public boolean isExpired() {
            return LocalDate.now().isAfter(expiryDate);
        }
        @Override
        public boolean isShippable() { return true; }
        @Override
        public double getWeight() { return weight; }
    }

    // Biscuits class
    public static class Biscuits extends Product implements Expirable, Shippable {
        private LocalDate expiryDate;
        private double weight;

        public Biscuits(String name, double price, int quantity, LocalDate expiryDate, double weight) {
            super(name, price, quantity);
            this.expiryDate = expiryDate;
            this.weight = weight;
        }
        @Override
        public boolean isExpired() {
            return LocalDate.now().isAfter(expiryDate);
        }
        @Override
        public boolean isShippable() { return true; }
        @Override
        public double getWeight() { return weight; }
    }

    // TV class
    public static class TV extends Product implements Shippable {
        private double weight;

        public TV(String name, double price, int quantity, double weight) {
            super(name, price, quantity);
            this.weight = weight;
        }
        @Override
        public boolean isShippable() { return true; }
        @Override
        public double getWeight() { return weight; }
    }

    // Mobile class
    public static class Mobile extends Product {
        public Mobile(String name, double price, int quantity) {
            super(name, price, quantity);
        }
    }

    // ScratchCard class
    public static class ScratchCard extends Product {
        public ScratchCard(String name, double price, int quantity) {
            super(name, price, quantity);
        }
    }

    // CartItem class
    public static class CartItem {
        public Product product;
        public int quantity;

        public CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
    }

    // Cart class
    public static class Cart {
        private List<CartItem> items = new ArrayList<>();

        public void add(Product product, int quantity) throws Exception {
            if (quantity > product.getQuantity()) {
                throw new Exception("Not enough stock for " + product.getName());
            }
            items.add(new CartItem(product, quantity));
        }
        public List<CartItem> getItems() { return items; }
        public boolean isEmpty() { return items.isEmpty(); }
    }

    // Customer class
    public static class Customer {
        private String name;
        private double balance;

        public Customer(String name, double balance) {
            this.name = name;
            this.balance = balance;
        }
        public double getBalance() { return balance; }
        public void deduct(double amount) { this.balance -= amount; }
        public String getName() { return name; }
    }

    // ShippingService interface
    public interface ShippingService {
        void ship(List<Shippable> items);
    }

    // SimpleShippingService class
    public static class SimpleShippingService implements ShippingService {
        @Override
        public void ship(List<Shippable> items) {
            double totalWeight = 0;
            System.out.println("** Shipment notice **");
            Map<String, Integer> counts = new LinkedHashMap<>();
            Map<String, Double> weights = new LinkedHashMap<>();

            for (Shippable item : items) {
                counts.put(item.getName(), counts.getOrDefault(item.getName(), 0) + 1);
                weights.put(item.getName(), item.getWeight());
                totalWeight += item.getWeight();
            }
            for (String name : counts.keySet()) {
                System.out.printf("%dx %s %.0fg\n", counts.get(name), name, weights.get(name));
            }
            System.out.printf("Total package weight %.1fkg\n", totalWeight / 1000.0);
        }
    }

    // CheckoutService class
    public static class CheckoutService {
        private static final double SHIPPING_FEE = 30.0;

        public static void checkout(Customer customer, Cart cart, ShippingService shippingService) throws Exception {
            if (cart.isEmpty()) throw new Exception("Cart is empty");

            double subtotal = 0;
            double shipping = 0;
            List<Shippable> shippables = new ArrayList<>();

            for (CartItem item : cart.getItems()) {
                Product p = item.product;
                if (item.quantity > p.getQuantity()) throw new Exception(p.getName() + " is out of stock");
                if (p instanceof Expirable && ((Expirable) p).isExpired()) throw new Exception(p.getName() + " is expired");
                subtotal += p.getPrice() * item.quantity;
                if (p instanceof Shippable) {
                    for (int i = 0; i < item.quantity; i++) shippables.add((Shippable) p);
                }
            }
            if (!shippables.isEmpty()) {
                shipping = SHIPPING_FEE;
                shippingService.ship(shippables);
            }
            double total = subtotal + shipping;
            if (customer.getBalance() < total) throw new Exception("Insufficient balance");
            customer.deduct(total);

            // Print receipt
            System.out.println("** Checkout receipt **");
            for (CartItem item : cart.getItems()) {
                System.out.printf("%dx %s %.0f\n", item.quantity, item.product.getName(), item.product.getPrice() * item.quantity);
                item.product.reduceQuantity(item.quantity);
            }
            System.out.println("----------------------");
            System.out.printf("Subtotal %.0f\n", subtotal);
            System.out.printf("Shipping %.0f\n", shipping);
            System.out.printf("Amount %.0f\n", total);
            System.out.printf("Customer balance %.0f\n", customer.getBalance());
        }
    }

    // Main method to test
    public static void main(String[] args) {
        try {
            Cheese cheese = new Cheese("Cheese", 100, 5, LocalDate.now().plusDays(5), 200);
            Biscuits biscuits = new Biscuits("Biscuits", 150, 2, LocalDate.now().plusDays(2), 700);
            TV tv = new TV("TV", 5000, 3, 8000);
            Mobile mobile = new Mobile("Mobile", 3000, 10);
            ScratchCard scratchCard = new ScratchCard("ScratchCard", 50, 100);

            Customer customer = new Customer("Ali", 1000);

            Cart cart = new Cart();
            cart.add(cheese, 2);
            cart.add(biscuits, 1);
            cart.add(scratchCard, 1);

            CheckoutService.checkout(customer, cart, new SimpleShippingService());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
