package com.lightbend.futures;

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

interface CustomerRepository {
    CompletionStage<Void> saveCustomer(Customer customer);
    CompletionStage<Optional<Customer>> getCustomer(UUID customerId);
}

class CachedCustomerRepository implements CustomerRepository, Closeable {

    private final ObjectStore objectStore;
    private final ConcurrentHashMap<UUID, Customer> cache = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    CachedCustomerRepository(ObjectStore objectStore) {
        this.objectStore = objectStore;
    }

    @Override
    public CompletionStage<Void> saveCustomer(Customer customer) {
        return CompletableFuture.runAsync(() -> {
            lock.writeLock().lock();
            objectStore.write(customer.getId(), customer);
            cache.put(customer.getId(), customer);
            lock.writeLock().unlock();
        }, executor);
    }

    @Override
    public CompletionStage<Optional<Customer>> getCustomer(UUID customerId) {
        lock.readLock().lock();

        CompletionStage<Optional<Customer>> customer;

        if (cache.containsKey(customerId)) {
            customer = CompletableFuture.completedFuture(Optional.of(cache.get(customerId)));
        } else {
            customer = CompletableFuture.supplyAsync(() ->
                objectStore.read(customerId).map(obj -> (Customer) obj), executor);
        }

        lock.readLock().unlock();

        return customer;
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}
