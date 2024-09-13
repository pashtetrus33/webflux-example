package com.example.webflux_example.controller;

import com.example.webflux_example.AbstractTest;
import com.example.webflux_example.model.ItemModel;
import com.example.webflux_example.model.SubItemModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;


class ItemControllerTest extends AbstractTest {

    @Test
    public void whenGetAllItems_thenReturnListOfItemsFromDatabase() {
        var expectedData = List.of(
                new ItemModel(FIRST_ITEM_ID, "Name 1", 10, Collections.emptyList()),
                new ItemModel(SECOND_ITEM_ID, "Name 2", 20, List.of(
                        new SubItemModel("SubItem 1", BigDecimal.valueOf(1001)),
                        new SubItemModel("SubItem 2", BigDecimal.valueOf(2001))
                )
                )
        );

        webClient.get().uri("/api/v1/items")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ItemModel.class)
                .hasSize(2)
                .contains(expectedData.toArray(ItemModel[]::new));
    }

    @Test
    public void whenGetItemById_thenReturnItemByIdFromDatabase() {
        var expectedData = new ItemModel(SECOND_ITEM_ID, "Name 2", 20, List.of(
                new SubItemModel("SubItem 1", BigDecimal.valueOf(1001)),
                new SubItemModel("SubItem 2", BigDecimal.valueOf(2001))
        )
        );

        webClient.get().uri("/api/v1/items/{id}", SECOND_ITEM_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ItemModel.class)
                .isEqualTo(expectedData);
    }

    @Test
    public void whenGetItemByName_thenReturnItemByName() {
        var expectedData = new ItemModel(FIRST_ITEM_ID, "Name 1", 10, Collections.emptyList());

        webClient.get().uri("/api/v1/items/by-name?name=Name 1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ItemModel.class)
                .isEqualTo(expectedData);

    }

    @Test
    public void whenCreateItem_thenReturnNewItemWithIdAndPublishEvent() {
        StepVerifier.create(itemRepository.count())
                .expectNext(2L)
                .expectComplete()
                .verify();

        ItemModel requestModel = new ItemModel();
        requestModel.setName("Test Item");
        requestModel.setCount(5);
        requestModel.setSubItems(Collections.emptyList());

        webClient.post().uri("/api/v1/items")
                .body(Mono.just(requestModel), ItemModel.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ItemModel.class)
                .value(responseModel -> {
                    Assertions.assertNotNull(responseModel.getId());
                });

        StepVerifier.create(itemRepository.count())
                .expectNext(3L)
                .expectComplete()
                .verify();

        webClient.get().uri("/api/v1/items/stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(new ParameterizedTypeReference<ServerSentEvent<ItemModel>>() {
                })
                .getResponseBody()
                .take(1)
                .as(StepVerifier::create)
                .consumeNextWith(serverSentEvent -> {
                    var data = serverSentEvent.data();
                    Assertions.assertNotNull(data);
                    Assertions.assertNotNull(data.getId());
                    Assertions.assertEquals("Test Item", data.getName());
                })
                .thenCancel()
                .verify();
    }

    @Test
    public void whenUpdateItem_thenReturnUpdatedItem() {

        ItemModel requestModel = new ItemModel();
        requestModel.setName("New Item name");
        requestModel.setCount(50);
        requestModel.setSubItems(Collections.emptyList());

        webClient.put().uri("/api/v1/items/{id}", FIRST_ITEM_ID)
                .body(Mono.just(requestModel), ItemModel.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ItemModel.class)
                .value(responseModel -> {
                    Assertions.assertEquals(50, responseModel.getCount());
                    Assertions.assertEquals("New Item name", responseModel.getName());
                    Assertions.assertNotNull(responseModel.getId());
                });

        StepVerifier.create(itemRepository.findByName("Name 1"))
                .expectNextCount(0L)
                .expectComplete()
                .verify();

        StepVerifier.create(itemRepository.findByName("New Item name"))
                .expectNextCount(1L)
                .expectComplete()
                .verify();
    }

    @Test
    public void whenDeleteItem_thenRemoveItemFromDatabase() {
        webClient.delete().uri("/api/v1/items/{id}", FIRST_ITEM_ID)
                .exchange()
                .expectStatus().isNoContent();

        StepVerifier.create(itemRepository.count())
                .expectNext(1L)
                .expectComplete()
                .verify();
    }
}