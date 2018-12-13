package com.example.gatewaydemokotlin

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux

@SpringBootApplication
class GatewayDemoKotlinApplication

fun main(args: Array<String>) {
    runApplication<GatewayDemoKotlinApplication>(*args)
}

@Configuration
@EnableWebFluxSecurity
@Profile("gateway")
class GatewayConfiguration {

    @Value("\${surprise.baseuri:http://localhost:8180}")
    internal var baseUri: String? = null

    @Bean
    fun openRoutes(builder: RouteLocatorBuilder): RouteLocator = builder
            .routes()
            .route { predicateSpec ->
                predicateSpec.method(HttpMethod.GET)
                        .and()
                        .path("/hello")
                        .uri("$baseUri/hello")
            }
            .route { predicateSpec ->
                predicateSpec.method(HttpMethod.GET)
                        .and()
                        .path("/surprise")
                        .uri("$baseUri/surprise")
            }
            .build()


}

@Configuration
@EnableWebFluxSecurity
class SecurityConfiguration {
    @Bean
    fun securityConfig(security: ServerHttpSecurity): SecurityWebFilterChain = security
            .authorizeExchange()
            .pathMatchers(HttpMethod.GET,
                    "/**")
            .permitAll()
            .and()
            .build()
}

@Configuration
@EnableWebFlux
@Profile("web")
class SurpriseWebEndpoints {

    @Bean
    fun routerFunctions(): RouterFunction<ServerResponse> = router {
        GET("/hello") {
            ServerResponse
                    .ok()
                    .body(Flux.just("Hello There"))
        }
        GET("/surprise") {
            ServerResponse
                    .ok()
                    .body(Surpriser.nextSurprise())
        }
    }

    data class Surprise(val who: String, val `when`: String)

    object Surpriser {
        fun nextSurprise(): Flux<Surprise> = Flux.just(Surprise("self", "" + System.currentTimeMillis()))
    }
}