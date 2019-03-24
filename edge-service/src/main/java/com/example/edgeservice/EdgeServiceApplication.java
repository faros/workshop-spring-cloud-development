package com.example.edgeservice;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableDiscoveryClient
@EnableZuulProxy
@EnableFeignClients
@EnableHystrix
public class EdgeServiceApplication {

	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	public static void main(String[] args) {
		SpringApplication.run(EdgeServiceApplication.class, args);
	}
}

/*
Setting up /product/names REST endpoint without using Feign

@RestController
class ProductApiAdapterRestController {

	private final RestTemplate restTemplate;

	public ProductApiAdapterRestController(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@GetMapping("/product/names")
	public ResponseEntity<List<String>> names() {
		ResponseEntity<List<Product>> response = restTemplate.exchange(
				"http://product-service/products",
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<List<Product>>() {
				});

		List<String> productNames = response.getBody().stream()
				.map(Product::getName)
				.collect(Collectors.toList());

		return ResponseEntity.ok().body(productNames);
	}
}

*/


@FeignClient("product-service")
interface ProductClient {
	@GetMapping("/products")
	Collection<Product> read();
}

@RestController
class GiftApiAdapterRestController {
	private final ProductClient productClient;

	public GiftApiAdapterRestController(ProductClient productClient) {
		this.productClient = productClient;
	}

	public Collection<String> fallback() {
		return new ArrayList<>();
	}

	@GetMapping("/product/names")
	@HystrixCommand(fallbackMethod = "fallback")
	public Collection<String> names() {
		return productClient.read()
				.stream()
				.map(n -> n.getName())
				.collect(Collectors.toList());
	}
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class Product {
	private Long id;
	private String name;
}