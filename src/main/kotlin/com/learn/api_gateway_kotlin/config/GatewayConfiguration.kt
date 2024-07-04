package com.learn.api_gateway_kotlin.config

import com.learn.api_gateway_kotlin.filter.AuthFilter
import com.learn.api_gateway_kotlin.filter.PostGlobalFilter
import com.learn.api_gateway_kotlin.filter.RequestFilter
import com.learn.api_gateway_kotlin.model.Company
import com.learn.api_gateway_kotlin.model.Student
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.WebFilter

@Configuration
class GatewayConfiguration {
    @Autowired
    var requestFilter: RequestFilter? = null

    @Autowired
    var authFilter: AuthFilter? = null


    @Bean
    fun customRouteLocator(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes()
            .route("first-microservice") { r: PredicateSpec ->
                r.path("/first/**")
                    .and().method("POST")
                    .and().readBody(Student::class.java) { true }
                    .filters { f: GatewayFilterSpec -> f.filters(requestFilter, authFilter) }
                    .uri("http://localhost:8081")
            }
            .route("first-microservice") { r: PredicateSpec ->
                r.path("/first/**")
                    .and().method("GET")
                    .filters { f: GatewayFilterSpec -> f.filters(authFilter) }
                    .uri("http://localhost:8081")
            }
            .route("second-microservice") { r: PredicateSpec ->
                r.path("/second")
                    .and().method("POST")
                    .and().readBody(Company::class.java) { true }
                    .filters { f: GatewayFilterSpec -> f.filters(requestFilter, authFilter) }
                    .uri("http://localhost:8082")
            }
            .route("second-microservice") { r: PredicateSpec ->
                r.path("/second")
                    .and().method("GET")
                    .filters { f: GatewayFilterSpec -> f.filters(authFilter) }
                    .uri("http://localhost:8082")
            }
            .route("auth-server") { r: PredicateSpec ->
                r.path("/login")
                    .uri("http://localhost:8088")
            }
        .build()
    }


    @Bean
    fun getRestTemplate(): RestTemplate {
        return RestTemplate()
    }

    @Bean
    fun responseFilter(): WebFilter {
        return PostGlobalFilter()
    }
}