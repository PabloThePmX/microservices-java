package br.edu.atitus.product_service.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.atitus.product_service.clients.CurrencyClient;
import br.edu.atitus.product_service.clients.CurrencyResponse;
import br.edu.atitus.product_service.entities.ProductEntity;
import br.edu.atitus.product_service.repositories.ProductRepository;

@RestController
@RequestMapping("products")
public class OpenProductController {
	
	private final ProductRepository repository;
	private final CurrencyClient currencyClient;
	private final CacheManager cacheManager;
	
	@Value("${server.port}")
	private int serverPort;
	
	public OpenProductController(ProductRepository repository, CurrencyClient currencyClient, CacheManager cacheManager) {
		this.repository = repository;
		this.currencyClient = currencyClient;
		this.cacheManager = cacheManager;
	}
	
	@GetMapping("/{idProduct}/{targetCurrency}")
	public ResponseEntity<ProductEntity> getProductById(@PathVariable Long idProduct, @PathVariable String targetCurrency) throws Exception {
		
		targetCurrency = targetCurrency.toUpperCase();
		String nameCache = "Product";
		String keyCache = idProduct + targetCurrency;
		
		ProductEntity product = cacheManager.getCache(nameCache).get(keyCache, ProductEntity.class);
		
		//se existe cache
		if(product != null) {
			setInformation(product.getConvertedPrice(), targetCurrency, "Cache", product);
			return ResponseEntity.ok(product); 
		}
		
		product = repository.findById(idProduct).orElseThrow(() -> new Exception("Product not found!"));
		
		//se existe no banco
		if(targetCurrency.equals(product.getCurrency())) {	
			setInformation(product.getPrice(), targetCurrency, "Local Database", product);
			cacheManager.getCache(nameCache).put(keyCache, product);
			return ResponseEntity.ok(product);
		}

		CurrencyResponse currency = currencyClient.getCurrency(product.getPrice(), product.getCurrency(), targetCurrency);
		
		//se caiu no fallback
		if(currency.getConvertedValue() == -1) {
			setInformation(currency.getConvertedValue(), "Unavailable", "ERROR", product);
			return ResponseEntity.ok(product); 
		}
		
		//se buscou do servico de currency e deu certo
		setInformation(currency.getConvertedValue(), targetCurrency, "API BCB", product);
		cacheManager.getCache(nameCache).put(keyCache, product);
			
		return ResponseEntity.ok(product);
	}
	
	private void setInformation(double convertedValue, String targetCurrency, String dataSource, ProductEntity product) {
		product.setConvertedPrice(convertedValue);
		product.setEnvironment("Product-Service running on port: " + serverPort + " - Currency: " + targetCurrency + " - Data Source: " + dataSource);
	}
}
