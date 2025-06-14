package br.edu.atitus.product_service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "currency-service", fallback = CurrencyClientFallback.class)
public interface CurrencyClient {
	
	@GetMapping("currency/{value}/{source}/{target}")
	public CurrencyResponse getCurrency(@PathVariable double value, @PathVariable String source, @PathVariable String target); 
}
