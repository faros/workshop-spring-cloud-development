package com.example.productservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Collection;
import java.util.stream.Stream;

@SpringBootApplication
public class ProductServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductServiceApplication.class, args);
	}
}

@Component
class initializer implements ApplicationRunner {

	private final ProductRepository productRepository;

	public initializer(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		Stream.of("Lego 21317: Steamboat Willie",
				"Lego 10265: Ford Mustang",
				"Lego 42096: Porsche 911 RSR",
				"Lego 10260: Downtown Diner",
				"Lego 10261: Roller Coaster",
				"Lego 10262: James Bond Aston Martin DB5",
				"Lego 41620: Stormtrooper")
				.forEach(name -> productRepository.save(new Product(null, name)));

		productRepository.findAll().forEach(System.out::println);
	}
}

interface ProductRepository extends JpaRepository<Product, Long> {
	// select * from products where product_name = name;
	Collection<Product> findByName(String name);
}

@RestController
class ProductRestController {

	private final ProductRepository productRepository;

	public ProductRestController(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	@GetMapping("/products")
	Collection<Product> products() {
		return this.productRepository.findAll();
	}
}

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
class Product {

	@Id
	@GeneratedValue
	private Long id;

	private String name;

}