package com.wallet;

import com.wallet.exception.WalletException;
import com.wallet.model.*;
import com.wallet.service.WalletService;
import com.wallet.storage.TransactionLogger;

import com.wallet.ui.WalletFrame;

import javax.swing.SwingUtilities;
import java.math.BigDecimal;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws Exception {
        WalletService svc = new WalletService(new TransactionLogger("transactions.csv"));
        User alice = new StandardUser("U001", "Alice");
        User bob = new PremiumUser("U002", "Bob");
        User shop = new MerchantUser("U003", "CityMart");
        svc.register(alice); svc.register(bob); svc.register(shop);

        svc.topUp("U001", new BigDecimal("30000"));
        svc.topUp("U002", new BigDecimal("50000"));

        if (args.length > 0 && args[0].equals("gui")) {          // java com.wallet.Main gui
            SwingUtilities.invokeLater(() -> new WalletFrame(svc).setVisible(true));
            return;
        }

        System.out.println("--- Business rules ---");
        attempt(svc, "U001", "U002", "6000");   // above Standard per-txn limit
        attempt(svc, "U001", "U002", "-5");     // invalid amount
        attempt(svc, "U001", "U009", "100");    // unknown user
        attempt(svc, "U002", "U003", "12000");  // premium tiered cashback (3%)
        attempt(svc, "U001", "U003", "1000");   // standard cashback (1%)

        System.out.println("\n--- Concurrency test: 300 parallel transfers of Rs.100 from Alice to Bob ---");
        ExecutorService pool = Executors.newFixedThreadPool(8);
        AtomicInteger ok = new AtomicInteger(), failed = new AtomicInteger();
        CountDownLatch done = new CountDownLatch(300);
        for (int i = 0; i < 300; i++) {
            pool.submit(() -> {
                try { svc.transfer("U001", "U002", new BigDecimal("100")); ok.incrementAndGet(); }
                catch (WalletException e) { failed.incrementAndGet(); }
                finally { done.countDown(); }
            });
        }
        done.await();
        pool.shutdown();
        System.out.println("Successful: " + ok + ", Rejected (limit/balance): " + failed);
        System.out.println("Alice balance: Rs." + alice.getWallet().getBalance());
        System.out.println("Bob balance  : Rs." + bob.getWallet().getBalance());

        System.out.println("\n--- Opposite-direction transfers (deadlock check) ---");
        ExecutorService p2 = Executors.newFixedThreadPool(8);
        CountDownLatch d2 = new CountDownLatch(200);
        for (int i = 0; i < 100; i++) {
            p2.submit(() -> quietTransfer(svc, "U002", "U003", d2));
            p2.submit(() -> quietTransfer(svc, "U003", "U002", d2));
        }
        boolean finished = d2.await(10, TimeUnit.SECONDS);
        System.out.println(finished ? "All 200 transfers finished - no deadlock" : "DEADLOCK!");
        p2.shutdown();

        System.out.println("\nLedger entries: " + svc.getLedger().size());
        System.out.println("Last 3 history lines of Alice:");
        var h = alice.getWallet().getHistory();
        h.subList(h.size() - 3, h.size()).forEach(System.out::println);
    }

    private static void quietTransfer(WalletService s, String f, String t, CountDownLatch latch) {
        try { s.transfer(f, t, new BigDecimal("50")); }
        catch (WalletException ignored) { }
        finally { latch.countDown(); }
    }

    private static void attempt(WalletService s, String f, String t, String amt) {
        try {
            Transaction tx = s.transfer(f, t, new BigDecimal(amt));
            System.out.println("OK     : " + tx);
        } catch (WalletException e) {
            System.out.println("REJECT : " + e.getMessage());
        }
    }
}
