import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.HashMap;

public class SimpleHttpServer {
    private BankManager bank;

    public SimpleHttpServer(BankManager bank) {
        this.bank = bank;
    }

    public void start(int port) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            server.createContext("/", new FileHandler());
            server.createContext("/api/accounts", new AccountsHandler());
            server.createContext("/api/create-account", new CreateAccountHandler());
            server.createContext("/api/login", new LoginHandler());
            server.createContext("/api/reset-pin", new ResetPinHandler());
            server.createContext("/api/deposit", new DepositHandler());
            server.createContext("/api/withdraw", new WithdrawHandler());
            server.createContext("/api/transfer", new TransferHandler());
            server.createContext("/api/transactions", new TransactionsHandler());
            server.createContext("/api/report", new ReportHandler());

            server.setExecutor(null);
            server.start();
            System.out.println("Web server started at: http://localhost:" + port);
        } catch (Exception e) {
            System.out.println("Could not start server: " + e.getMessage());
        }
    }

    static class FileHandler implements HttpHandler {
        public void handle(HttpExchange exchange) {
            try {
                String path = exchange.getRequestURI().getPath();

                if (path.startsWith("/api/")) {
                    sendJson(exchange, 404, "{\"success\":false,\"message\":\"API endpoint not found: " + path + "\"}");
                    return;
                }

                if (path.equals("/")) {
                    path = "/index.html";
                }

                File file = new File("frontend" + path);
                if (!file.exists() || file.isDirectory()) {
                    file = new File("frontend/index.html");
                }

                String contentType = "text/html";
                if (file.getName().endsWith(".css")) contentType = "text/css";
                else if (file.getName().endsWith(".js")) contentType = "application/javascript";

                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, file.length());

                OutputStream os = exchange.getResponseBody();
                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                int count;
                while ((count = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, count);
                }
                fis.close();
                os.close();
            } catch (Exception e) {
                try {
                    exchange.sendResponseHeaders(500, -1);
                } catch (Exception ignored) {}
            }
        }
    }

    private static String getBody(HttpExchange exchange) throws Exception {
        InputStream is = exchange.getRequestBody();
        StringBuilder sb = new StringBuilder();
        int b;
        while ((b = is.read()) != -1) {
            sb.append((char) b);
        }
        return sb.toString();
    }

    private static HashMap<String, String> parseParams(String body) {
        HashMap<String, String> map = new HashMap<String, String>();
        if (body == null || body.trim().equals("")) return map;

        if (body.trim().startsWith("{")) {
            String clean = body.replace("{", "").replace("}", "").replace("\"", "");
            String[] pairs = clean.split(",");
            for (int i = 0; i < pairs.length; i++) {
                String[] kv = pairs[i].split(":");
                if (kv.length >= 2) {
                    map.put(kv[0].trim(), kv[1].trim());
                }
            }
            return map;
        }

        String[] pairs = body.split("&");
        for (int i = 0; i < pairs.length; i++) {
            String[] kv = pairs[i].split("=");
            if (kv.length == 2) {
                try {
                    String k = URLDecoder.decode(kv[0], "UTF-8");
                    String v = URLDecoder.decode(kv[1], "UTF-8");
                    map.put(k, v);
                } catch (Exception ignored) {}
            }
        }
        return map;
    }

    private static void sendJson(HttpExchange exchange, int code, String json) throws Exception {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        byte[] bytes = json.getBytes("UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    class AccountsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) {
            try {
                StringBuilder sb = new StringBuilder("[");
                int index = 0;
                for (Account a : bank.map.values()) {
                    if (index > 0) sb.append(",");
                    sb.append("{")
                      .append("\"accNo\":\"").append(a.accNo).append("\",")
                      .append("\"name\":\"").append(a.name).append("\",")
                      .append("\"email\":\"").append(a.email).append("\",")
                      .append("\"type\":\"").append(a.getAccountType()).append("\",")
                      .append("\"balance\":").append(a.balance)
                      .append("}");
                    index++;
                }
                sb.append("]");
                sendJson(exchange, 200, sb.toString());
            } catch (Exception e) {
                try { sendJson(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}"); } catch (Exception ignored) {}
            }
        }
    }

    class CreateAccountHandler implements HttpHandler {
        public void handle(HttpExchange exchange) {
            try {
                HashMap<String, String> p = parseParams(getBody(exchange));
                String name = p.get("name");
                String email = p.get("email");
                String pin = p.get("pin");
                String type = p.get("type");
                double bal = Double.parseDouble(p.get("balance"));
                double limit = p.containsKey("limit") ? Double.parseDouble(p.get("limit")) : 0;

                Account a = bank.openAccount(name, email, pin, type, bal, limit);
                sendJson(exchange, 200, "{\"success\":true,\"accNo\":\"" + a.accNo + "\"}");
            } catch (Exception e) {
                try { sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}"); } catch (Exception ignored) {}
            }
        }
    }

    class LoginHandler implements HttpHandler {
        public void handle(HttpExchange exchange) {
            try {
                HashMap<String, String> p = parseParams(getBody(exchange));
                String accNo = p.get("accNo");
                String pin = p.get("pin");

                Account a = bank.login(accNo, pin);
                String json = "{\"success\":true,\"accNo\":\"" + a.accNo + "\",\"name\":\"" + a.name + "\",\"email\":\"" + a.email + "\",\"type\":\"" + a.getAccountType() + "\",\"balance\":" + a.balance + "}";
                sendJson(exchange, 200, json);
            } catch (Exception e) {
                try { sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}"); } catch (Exception ignored) {}
            }
        }
    }

    class ResetPinHandler implements HttpHandler {
        public void handle(HttpExchange exchange) {
            try {
                HashMap<String, String> p = parseParams(getBody(exchange));
                String accNo = p.get("accNo");
                String email = p.get("email");
                String newPin = p.get("newPin");

                bank.resetPin(accNo, email, newPin);
                sendJson(exchange, 200, "{\"success\":true,\"message\":\"Security PIN updated successfully! You can now log in.\"}");
            } catch (Exception e) {
                try { sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}"); } catch (Exception ignored) {}
            }
        }
    }

    class DepositHandler implements HttpHandler {
        public void handle(HttpExchange exchange) {
            try {
                HashMap<String, String> p = parseParams(getBody(exchange));
                String targetAcc = p.get("targetAcc");
                double amt = Double.parseDouble(p.get("amount"));
                String depositor = p.containsKey("depositor") ? p.get("depositor") : "Self";

                bank.doDeposit(targetAcc, amt, depositor);
                Account a = bank.findAccount(targetAcc);
                sendJson(exchange, 200, "{\"success\":true,\"balance\":" + a.balance + ",\"message\":\"Successfully deposited ₹" + amt + " into " + targetAcc + "\"}");
            } catch (Exception e) {
                try { sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}"); } catch (Exception ignored) {}
            }
        }
    }

    class WithdrawHandler implements HttpHandler {
        public void handle(HttpExchange exchange) {
            try {
                HashMap<String, String> p = parseParams(getBody(exchange));
                String accNo = p.get("accNo");
                double amt = Double.parseDouble(p.get("amount"));
                String pin = p.get("pin");

                bank.doWithdraw(accNo, amt, pin);
                Account a = bank.findAccount(accNo);
                sendJson(exchange, 200, "{\"success\":true,\"balance\":" + a.balance + ",\"message\":\"Withdrawal of ₹" + amt + " successful!\"}");
            } catch (Exception e) {
                try { sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}"); } catch (Exception ignored) {}
            }
        }
    }

    class TransferHandler implements HttpHandler {
        public void handle(HttpExchange exchange) {
            try {
                HashMap<String, String> p = parseParams(getBody(exchange));
                String from = p.get("from");
                String to = p.get("to");
                double amt = Double.parseDouble(p.get("amount"));
                String pin = p.get("pin");

                bank.doTransfer(from, to, amt, pin);
                sendJson(exchange, 200, "{\"success\":true,\"message\":\"Transfer of ₹" + amt + " to " + to + " completed!\"}");
            } catch (Exception e) {
                try { sendJson(exchange, 400, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}"); } catch (Exception ignored) {}
            }
        }
    }

    class TransactionsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) {
            try {
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < bank.txns.size(); i++) {
                    Transaction t = bank.txns.get(i);
                    if (i > 0) sb.append(",");
                    sb.append("{")
                      .append("\"id\":").append(t.id).append(",")
                      .append("\"accNo\":\"").append(t.accNo).append("\",")
                      .append("\"type\":\"").append(t.type).append("\",")
                      .append("\"amount\":").append(t.amount).append(",")
                      .append("\"balance\":").append(t.remBal).append(",")
                      .append("\"remark\":\"").append(t.remark).append("\"")
                      .append("}");
                }
                sb.append("]");
                sendJson(exchange, 200, sb.toString());
            } catch (Exception e) {
                try { sendJson(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}"); } catch (Exception ignored) {}
            }
        }
    }

    class ReportHandler implements HttpHandler {
        public void handle(HttpExchange exchange) {
            try {
                double total = 0;
                for (Account a : bank.map.values()) total += a.balance;
                String json = "{\"accounts\":" + bank.map.size() + ",\"cash\":" + total + ",\"transactions\":" + bank.txns.size() + "}";
                sendJson(exchange, 200, json);
            } catch (Exception e) {
                try { sendJson(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}"); } catch (Exception ignored) {}
            }
        }
    }
}
