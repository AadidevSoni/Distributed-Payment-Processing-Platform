package com.visasim.userservice;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.visasim.userservice.model.User;
import com.visasim.userservice.model.Wallet;
import com.visasim.userservice.repository.UserRepository;
import com.visasim.userservice.repository.WalletRepository;
import com.visasim.userservice.service.WalletService;

@SpringBootTest
public class ConcurrencyTest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void concurrentCreditsShouldNotLoseUpdates() throws Exception {
        User user = userRepository.save(
            new User(
                "Race Condition Test",
                "race-" + UUID.randomUUID() + "@test.com"
            )
        );
        Wallet wallet = walletRepository.save(new Wallet(user.getId()));

        int threadCount = 100;
        BigDecimal creditAmount = new BigDecimal("10.00");

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        List<Future<Wallet>> futures = IntStream.range(0, threadCount)
                .mapToObj(i -> executor.submit(() ->
                        walletService.credit(wallet.getId(), creditAmount)))
                .toList();

        for (Future<?> future : futures) {
            future.get(); // wait for every thread to finish
        }
        executor.shutdown();

        Wallet finalWallet = walletRepository.findById(wallet.getId()).orElseThrow();

        BigDecimal expected = creditAmount.multiply(new BigDecimal(threadCount));
        assertEquals(0, expected.compareTo(finalWallet.getBalance()),
                "Expected " + expected + " but got " + finalWallet.getBalance() + " — updates were lost!");
    }
}