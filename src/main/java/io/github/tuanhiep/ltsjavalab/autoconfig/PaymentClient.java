package io.github.tuanhiep.ltsjavalab.autoconfig;

@FunctionalInterface
public interface PaymentClient {

    String charge(String orderId);
}
