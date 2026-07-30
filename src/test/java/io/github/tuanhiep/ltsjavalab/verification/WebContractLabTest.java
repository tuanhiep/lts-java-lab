package io.github.tuanhiep.ltsjavalab.verification;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.tuanhiep.ltsjavalab.web.OrderController;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = OrderController.class,
        properties = "spring.mvc.problemdetails.enabled=true")
@AutoConfigureMockMvc(addFilters = false)
class WebContractLabTest {

    @Autowired
    MockMvc mvc;

    @Test
    void invalidBodyReturnsProblemDetailWithoutInternalNames() throws Exception {
        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"","quantity":-1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.detail", not(containsString("Exception"))));
    }

    @Test
    void builtInMethodValidationWorksWithoutClassLevelValidated() throws Exception {
        mvc.perform(get("/api/orders/quantity").param("value", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.detail", not(containsString("Exception"))));
    }
}
