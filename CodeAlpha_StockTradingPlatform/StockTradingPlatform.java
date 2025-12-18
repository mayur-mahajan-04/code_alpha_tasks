package CodeAlpha_StockTradingPlatform;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * StockTradingPlatform.java
 * Java 7 compatible console stock trading simulation with:
 * - Live price fluctuations (Timer)
 * - Buy / Sell
 * - Portfolio persistence (portfolio.txt) and history (history.txt)
 *
 * Usage:
 * javac StockTradingPlatform.java
 * java StockTradingPlatform
 */
public class StockTradingPlatform {

    // Market storage
    private static final Map<String, Stock> market = new LinkedHashMap<String, Stock>();
    private static final Random rnd = new Random();

    // App state
    private static final Portfolio portfolio = new Portfolio();
    private static final Scanner sc = new Scanner(System.in);

    // Timer for live price changes
    private static Timer priceTimer = null;

    public static void main(String[] args) {
        seedMarket();

        // Start automatic price updates every 5 seconds
        startPriceTimer(5000);

        System.out.println("=== Stock Trading Platform (Java 7 compatible) ===");
        boolean running = true;
        while (running) {
            printMenu();
            String line = prompt("Choice: ").trim();
            if (line.isEmpty()) continue;
            int choice = 0;
            try {
                choice = Integer.parseInt(line);
            } catch (NumberFormatException ex) {
                System.out.println("Enter valid number.");
                continue;
            }

            switch (choice) {
                case 1: viewMarket(); break;
                case 2: buyStock(); break;
                case 3: sellStock(); break;
                case 4: viewPortfolio(); break;
                case 5: viewHistory(); break;
                case 6: saveFiles(); break;
                case 7: loadFiles(); break;
                case 8: running = false; shutdown(); break;
                default: System.out.println("Invalid choice."); break;
            }
        }
    }

    // ---------- Market setup ----------
    private static void seedMarket() {
        market.put("AAPL", new Stock("AAPL", 150.00));
        market.put("GOOG", new Stock("GOOG", 2800.00));
        market.put("MSFT", new Stock("MSFT", 320.00));
        market.put("TSLA", new Stock("TSLA", 750.00));
        market.put("AMZN", new Stock("AMZN", 145.00));
    }

    private static void printMenu() {
        System.out.println("\nMenu:");
        System.out.println("1) View Market Prices");
        System.out.println("2) Buy Stock");
        System.out.println("3) Sell Stock");
        System.out.println("4) View Portfolio");
        System.out.println("5) View Transaction History");
        System.out.println("6) Save portfolio & history (portfolio.txt, history.txt)");
        System.out.println("7) Load portfolio & history");
        System.out.println("8) Exit");
    }

    private static void viewMarket() {
        System.out.println("\n--- Market (live) ---");
        System.out.printf("%-6s %12s\n", "SYMB", "PRICE");
        System.out.println("----------------------");
        for (Stock s : market.values()) {
            System.out.printf("%-6s %12.2f\n", s.getSymbol(), s.getPrice());
        }
    }

    // ---------- Buy / Sell ----------
    private static void buyStock() {
        String sym = prompt("Enter symbol to BUY: ").toUpperCase();
        Stock s = market.get(sym);
        if (s == null) { System.out.println("Symbol not found."); return; }

        int qty = readInt("Quantity to buy: ");
        if (qty <= 0) { System.out.println("Quantity must be > 0."); return; }

        double cost = s.getPrice() * qty;
        System.out.printf("Cost = %d x %.2f = %.2f (Balance: %.2f)\n", qty, s.getPrice(), cost, portfolio.getBalance());
        String confirm = prompt("Confirm purchase? (y/n): ").toLowerCase();
        if (!"y".equals(confirm)) { System.out.println("Cancelled."); return; }

        if (cost > portfolio.getBalance()) {
            System.out.println("Insufficient funds.");
            return;
        }

        portfolio.buy(s.getSymbol(), qty, s.getPrice());
        System.out.println("Bought " + qty + " of " + s.getSymbol());
    }

    private static void sellStock() {
        String sym = prompt("Enter symbol to SELL: ").toUpperCase();
        Stock s = market.get(sym);
        if (s == null) { System.out.println("Symbol not found."); return; }

        int owned = portfolio.shares(sym);
        System.out.println("You own: " + owned + " shares.");
        int qty = readInt("Quantity to sell: ");
        if (qty <= 0) { System.out.println("Quantity must be > 0."); return; }
        if (qty > owned) { System.out.println("Not enough shares."); return; }

        double proceeds = s.getPrice() * qty;
        String confirm = prompt(String.format("Sell %d shares for %.2f? (y/n): ", qty, proceeds)).toLowerCase();
        if (!"y".equals(confirm)) { System.out.println("Cancelled."); return; }

        portfolio.sell(s.getSymbol(), qty, s.getPrice());
        System.out.println("Sold " + qty + " of " + s.getSymbol());
    }

    // ---------- View ----------
    private static void viewPortfolio() {
        System.out.println("\n=== Portfolio ===");
        System.out.printf("Balance: %.2f\n", portfolio.getBalance());
        if (portfolio.holdingsEmpty()) {
            System.out.println("(No holdings)");
        } else {
            System.out.printf("%-6s %8s %12s %12s\n", "SYMB", "SHARES", "PRICE", "VALUE");
            for (String sym : portfolio.holdingsSymbols()) {
                int sh = portfolio.shares(sym);
                Stock st = market.get(sym);
                double price = (st != null) ? st.getPrice() : 0.0;
                double value = price * sh;
                System.out.printf("%-6s %8d %12.2f %12.2f\n", sym, sh, price, value);
            }
        }
        System.out.printf("Total (cash + holdings): %.2f\n", portfolio.totalValue(market));
    }

    private static void viewHistory() {
        System.out.println("\n=== Transaction History ===");
        if (portfolio.historyEmpty()) {
            System.out.println("(no transactions)");
            return;
        }
        int idx = 1;
        for (Transaction t : portfolio.getHistory()) {
            System.out.println(idx + ") " + t);
            idx++;
        }
    }

    // ---------- Persistence ----------
    private static void saveFiles() {
        savePortfolioToFile("portfolio.txt");
        saveHistoryToFile("history.txt");
    }

    private static void loadFiles() {
        loadPortfolioFromFile("portfolio.txt");
        loadHistoryFromFile("history.txt");
    }

    private static void savePortfolioToFile(String filename) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(filename));
            pw.println("BALANCE=" + String.format("%.2f", portfolio.getBalance()));
            for (String sym : portfolio.holdingsSymbols()) {
                pw.println("HOLDING=" + sym + "," + portfolio.shares(sym));
            }
            pw.close();
            System.out.println("Saved portfolio to " + filename);
        } catch (IOException ex) {
            System.out.println("Error saving portfolio: " + ex.getMessage());
        }
    }

    private static void saveHistoryToFile(String filename) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(filename));
            // write transactions as comma-separated: timeMillis,type,symbol,qty,price
            for (Transaction t : portfolio.getHistory()) {
                pw.println(t.toCSV());
            }
            pw.close();
            System.out.println("Saved history to " + filename);
        } catch (IOException ex) {
            System.out.println("Error saving history: " + ex.getMessage());
        }
    }

    private static void loadPortfolioFromFile(String filename) {
        File f = new File(filename);
        if (!f.exists()) { System.out.println(filename + " not found."); return; }
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            // reset portfolio holdings and balance to default then load
            portfolio.clearHoldings();
            portfolio.setBalance(10000.00); // default before reading
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (line.startsWith("BALANCE=")) {
                    String val = line.substring("BALANCE=".length());
                    try { portfolio.setBalance(Double.parseDouble(val)); } catch (Exception e) {}
                } else if (line.startsWith("HOLDING=")) {
                    String rest = line.substring("HOLDING=".length());
                    String[] parts = rest.split(",");
                    if (parts.length >= 2) {
                        String sym = parts[0].toUpperCase();
                        int qty = 0;
                        try { qty = Integer.parseInt(parts[1]); } catch (Exception e) {}
                        if (qty > 0) portfolio.setHolding(sym, qty);
                    }
                }
            }
            br.close();
            System.out.println("Loaded portfolio from " + filename);
        } catch (IOException ex) {
            System.out.println("Error loading portfolio: " + ex.getMessage());
        }
    }

    private static void loadHistoryFromFile(String filename) {
        File f = new File(filename);
        if (!f.exists()) { System.out.println(filename + " not found."); return; }
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            portfolio.clearHistory();
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                // expected CSV: timeMillis,type,symbol,qty,price
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    try {
                        long time = Long.parseLong(parts[0]);
                        String type = parts[1];
                        String sym = parts[2];
                        int qty = Integer.parseInt(parts[3]);
                        double price = Double.parseDouble(parts[4]);
                        Transaction t = new Transaction(type, sym, qty, price, time);
                        portfolio.addTransaction(t);
                    } catch (Exception e) {
                        // ignore malformed
                    }
                }
            }
            br.close();
            System.out.println("Loaded history from " + filename);
        } catch (IOException ex) {
            System.out.println("Error loading history: " + ex.getMessage());
        }
    }

    // ---------- Price timer ----------
    private static void startPriceTimer(long intervalMs) {
        priceTimer = new Timer(true);
        priceTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                tickMarket();
            }
        }, intervalMs, intervalMs);
    }

    private static void tickMarket() {
        // change each stock by -5% .. +5%
        for (Stock s : market.values()) {
            double pct = (rnd.nextDouble() * 10.0) - 5.0; // -5 .. +5
            double factor = 1.0 + pct / 100.0;
            double newPrice = s.getPrice() * factor;
            // round to 2 decimals
            newPrice = Math.round(newPrice * 100.0) / 100.0;
            if (newPrice < 0.01) newPrice = 0.01;
            s.setPrice(newPrice);
        }
        // optional: uncomment to see live tick messages in console:
        // System.out.println("[Market ticked]");
    }

    private static void shutdown() {
        if (priceTimer != null) priceTimer.cancel();
        System.out.println("Shutting down. Goodbye!");
    }

    // ---------- Helpers ----------
    private static String prompt(String msg) {
        System.out.print(msg);
        return sc.nextLine();
    }

    private static int readInt(String msg) {
        while (true) {
            String s = prompt(msg);
            try {
                return Integer.parseInt(s.trim());
            } catch (Exception e) {
                System.out.println("Enter valid integer.");
            }
        }
    }

    // ---------- Inner domain classes ----------
    static class Stock {
        private final String symbol;
        private double price;
        public Stock(String symbol, double price) { this.symbol = symbol; this.price = price; }
        public String getSymbol() { return symbol; }
        public double getPrice() { return price; }
        public void setPrice(double p) { price = p; }
    }

    static class Transaction {
        private final String type; // BUY or SELL
        private final String symbol;
        private final int quantity;
        private final double price;
        private final long timeMillis;

        public Transaction(String type, String symbol, int quantity, double price) {
            this(type, symbol, quantity, price, System.currentTimeMillis());
        }

        // used when loading history
        public Transaction(String type, String symbol, int quantity, double price, long timeMillis) {
            this.type = type;
            this.symbol = symbol;
            this.quantity = quantity;
            this.price = price;
            this.timeMillis = timeMillis;
        }

        public String toCSV() {
            // timeMillis,type,symbol,qty,price
            return timeMillis + "," + type + "," + symbol + "," + quantity + "," + String.format("%.2f", price);
        }

        public String toString() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String t = sdf.format(new Date(timeMillis));
            return "[" + t + "] " + type + " " + quantity + " x " + symbol + " @ " + String.format("%.2f", price);
        }
    }

    static class Portfolio {
        private final Map<String, Integer> holdings = new LinkedHashMap<String, Integer>();
        private double balance = 10000.00;
        private final List<Transaction> history = new ArrayList<Transaction>();

        public double getBalance() { return balance; }
        public void setBalance(double b) { balance = b; }

        public void buy(String symbol, int qty, double price) {
            double cost = qty * price;
            balance -= cost;
            Integer cur = holdings.get(symbol);
            if (cur == null) holdings.put(symbol, qty);
            else holdings.put(symbol, cur + qty);
            history.add(new Transaction("BUY", symbol, qty, price));
        }

        public void sell(String symbol, int qty, double price) {
            Integer cur = holdings.get(symbol);
            if (cur == null || cur < qty) {
                System.out.println("Not enough shares to sell.");
                return;
            }
            holdings.put(symbol, cur - qty);
            if (holdings.get(symbol) == 0) holdings.remove(symbol);
            balance += qty * price;
            history.add(new Transaction("SELL", symbol, qty, price));
        }

        public int shares(String symbol) {
            Integer v = holdings.get(symbol);
            return (v == null) ? 0 : v;
        }

        public Set<String> holdingsSymbols() {
            return new LinkedHashSet<String>(holdings.keySet());
        }

        public boolean holdingsEmpty() { return holdings.isEmpty(); }
        public boolean historyEmpty() { return history.isEmpty(); }

        public void clearHoldings() { holdings.clear(); }
        public void clearHistory() { history.clear(); }
        public void setHolding(String symbol, int qty) { if (qty > 0) holdings.put(symbol, qty); }

        public void addTransaction(Transaction t) { history.add(t); }

        public List<Transaction> getHistory() { return new ArrayList<Transaction>(history); }

        public double totalValue(Map<String, Stock> market) {
            double val = balance;
            for (Map.Entry<String, Integer> e : holdings.entrySet()) {
                Stock s = market.get(e.getKey());
                if (s != null) val += s.getPrice() * e.getValue();
            }
            return val;
        }
    }
}
