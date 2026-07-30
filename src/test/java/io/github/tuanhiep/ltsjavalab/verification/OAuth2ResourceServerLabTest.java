package io.github.tuanhiep.ltsjavalab.verification;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.tuanhiep.ltsjavalab.web.OrderController;
import io.github.tuanhiep.ltsjavalab.web.SecurityConfig;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "lab.security.enabled=true")
@AutoConfigureMockMvc
@Import(OAuth2ResourceServerLabTest.DecoderConfig.class)
class OAuth2ResourceServerLabTest {

    @Autowired
    MockMvc mvc;

    @Test
    void authenticationAndAuthorizationRemainDistinct() throws Exception {
        var request = post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"sku":"sku-1","quantity":1}
                        """);

        mvc.perform(request).andExpect(status().isUnauthorized());
        mvc.perform(request.with(jwt())).andExpect(status().isForbidden());
        mvc.perform(request.with(jwt().authorities(
                        new SimpleGrantedAuthority("SCOPE_orders:write"))))
                .andExpect(status().isOk());
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class DecoderConfig {
        @Bean
        JwtDecoder jwtDecoder() {
            return token -> {
                throw new JwtException("network decoding is outside this MockMvc scenario");
            };
        }
    }
}
