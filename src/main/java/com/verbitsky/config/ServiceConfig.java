package com.verbitsky.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

import reactor.netty.http.client.HttpClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.verbitsky.api.client.RemoteServiceClient;
import com.verbitsky.api.client.RemoteServiceClientImpl;
import com.verbitsky.property.WebClientProperties;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableAsync
@EnableScheduling
class ServiceConfig {
    @Bean
    @Primary
    ObjectMapper customObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);

        return objectMapper;
    }

    @Bean
    WebClient webClient(WebClientProperties webClientProperties, ExchangeStrategies exchangeStrategies) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, webClientProperties.connectionTimeout())
                .responseTimeout(Duration.ofMillis(webClientProperties.responseTimeout()))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(
                                        webClientProperties.readTimeout(), TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(
                                        webClientProperties.writeTimeout(), TimeUnit.MILLISECONDS)));

        WebClient.Builder builder = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient));
        builder.exchangeStrategies(exchangeStrategies);

        return builder.build();
    }

    @Bean
    protected RemoteServiceClient remoteServiceClient(WebClient webClient) {
        return new RemoteServiceClientImpl(webClient);
    }

    /*
        Spring webclient uses its own object mapper bean to decode http messages.
        This bean is used to configure webclient, so it will use custom object mapper.
    */
    @Bean
    ExchangeStrategies customExchangeStrategies(ObjectMapper customObjectMapper) {
        return ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs()
                            .jackson2JsonEncoder(new Jackson2JsonEncoder(customObjectMapper));
                    configurer.defaultCodecs()
                            .jackson2JsonDecoder(new Jackson2JsonDecoder(customObjectMapper));
                    configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024);
                })
                .build();
    }
}
