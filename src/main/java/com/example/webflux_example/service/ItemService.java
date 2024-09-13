package com.example.webflux_example.service;

import com.example.webflux_example.entity.Item;
import com.example.webflux_example.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public Flux<Item> findAll() {
        return itemRepository.findAll();
    }

    public Mono<Item> findById(String id) {
        return itemRepository.findById(id);
    }

    public Mono<Item> findByName(String name) {
        return itemRepository.findByName(name);
    }

    public Mono<Item> save(Item item) {
        item.setId(UUID.randomUUID().toString());
        return itemRepository.save(item);
    }

    public Mono<Item> update(String id, Item item) {
        return findById(id)
                .flatMap(itemForUpdate -> {
                    if (StringUtils.hasText(item.getName())) {
                        itemForUpdate.setName(item.getName());
                    }

                    if (item.getCount() != null) {
                        itemForUpdate.setCount(item.getCount());
                    }

                    if (item.getSubItems() != null) {
                        itemForUpdate.setSubItems(item.getSubItems());
                    }

                    return itemRepository.save(itemForUpdate);
                });
    }

    public Mono<Void> deleteById(String id) {
        return itemRepository.deleteById(id);
    }
}
