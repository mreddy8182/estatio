package org.estatio.capex.dom.order;

import org.joda.time.LocalDate;
import org.junit.Test;

import org.estatio.dom.asset.Property;
import org.estatio.dom.party.Organisation;
import org.estatio.dom.party.Party;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderRepository_Test {

    private Order order = new Order();

    @Test
    public void upsert_when_already_exists() throws Exception {

        // given
        OrderRepository orderRepository = new OrderRepository(){
            @Override
            public Order findByOrderNumber(final String orderNumber) {
                return order;
            }
        };
        String number = "some number";
        String sellerOrderReference = "ref";
        LocalDate entryDate = new LocalDate(2017,1,1);
        LocalDate orderDate = new LocalDate(2017,1,2);
        Party seller = new Organisation();
        Party buyer = new Organisation();
        Property property = new Property();
        String atPath = "atPath";

        assertThat(order.getOrderNumber()).isNull();

        // when
        orderRepository.upsert(
                property, number,
                sellerOrderReference,
                entryDate,
                orderDate,
                seller,
                buyer,
                atPath
        );

        // then
        assertThat(order.getOrderNumber()).isNull();
        assertThat(order.getSellerOrderReference()).isEqualTo(sellerOrderReference);
        assertThat(order.getEntryDate()).isEqualTo(entryDate);
        assertThat(order.getOrderDate()).isEqualTo(orderDate);
        assertThat(order.getSeller()).isEqualTo(seller);
        assertThat(order.getBuyer()).isEqualTo(buyer);
        assertThat(order.getProperty()).isEqualTo(property);
        assertThat(order.getAtPath()).isEqualTo(atPath);

    }

}