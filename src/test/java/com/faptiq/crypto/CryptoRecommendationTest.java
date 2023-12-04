package com.faptiq.crypto;


import com.faptiq.crypto.pojo.CryptoDetails;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CryptoApplication.class})
@WebAppConfiguration
public class CryptoRecommendationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    ObjectMapper mapper = new ObjectMapper();


    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    void verifyContextLoads_thenRecommendationController() {
        ServletContext servletContext = webApplicationContext.getServletContext();


        Assertions.assertNotNull(servletContext);
        Assertions.assertTrue(servletContext instanceof MockServletContext);
        Assertions.assertNotNull(webApplicationContext.getBean("cryptoRecommendationController"));
    }

    @Test
    void shouldReturnCryptoSortedByNormalizedRange() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/sorted"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("ETH"))
                .andExpect(jsonPath("$[1]").value("XRP"))
                .andExpect(jsonPath("$[2]").value("DOGE"))
                .andExpect(jsonPath("$[3]").value("LTC"))
                .andExpect(jsonPath("$[4]").value("BTC"));
    }

    @Test
    void shouldReturnMinMaxOldestNewestValuesForBTC() throws Exception {
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/details")
                                .param("symbol", "BTC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        CryptoDetails cryptoDetails = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<CryptoDetails>() {
        });
        assertEquals(cryptoDetails.getCryptoSymbol().toString(), "BTC");
        assertEquals(cryptoDetails.getMinValue(), 33276.59);
        assertEquals(cryptoDetails.getMaxValue(), 47722.66);
        assertEquals(cryptoDetails.getOldestValue(), 1641009600000L);
        assertEquals(cryptoDetails.getNewestValue(), 1643659200000L);
    }

    @Test
    void shouldReturnBadRequestWhenCryptoSymbolNotSupported() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/details")
                                .param("symbol", "ADA"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertEquals("No enum constant com.faptiq.crypto.pojo.CryptoSymbol.ADA", result.getResponse().getContentAsString()));
    }

    @Test
    void shouldReturnCorrectHighestNormalizedRangeForDateSupported() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/highest")
                                .param("date", "2022-01-01"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(result -> assertEquals("XRP", result.getResponse().getContentAsString()));
    }

    @Test
    void shouldReturnNotFoundForHighestNormalizedRangeForDateNotSupported() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/highest")
                                .param("date", "2022-02-01"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals("No records found for date 2022-02-01", result.getResponse().getContentAsString()));
    }
}
