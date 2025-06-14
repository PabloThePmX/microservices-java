package br.edu.atitus.currency_service.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.atitus.currency_service.clients.CurrencyBCClient;
import br.edu.atitus.currency_service.clients.CurrencyBCResponse;
import br.edu.atitus.currency_service.entities.CurrencyEntity;
import br.edu.atitus.currency_service.repositories.CurrencyRepository;

@RestController
@RequestMapping("currency")
public class CurrencyController {
	
	private final CurrencyBCClient currencyBcClient;
	private final CurrencyRepository repository; 
	private final CacheManager cacheManager;
	
	@Value("${server.port}")
	private int serverPort;
	
	private String conversionDate; 
	
	public CurrencyController(CurrencyRepository repository, CurrencyBCClient currencyBcClient, CacheManager cacheManager){
		this.repository = repository;
		this.currencyBcClient = currencyBcClient;
		this.cacheManager = cacheManager;
	}
	
	@GetMapping("/{value}/{source}/{target}")
	public ResponseEntity<CurrencyEntity> getCurrency(@PathVariable double value,
			@PathVariable String source, @PathVariable String target) throws Exception {
		
		source = source.toUpperCase();
		target = target.toUpperCase();
		String dataSource = "None";
		String nameCache = "Currency";
		String keyCache = source + target;
		
		CurrencyEntity currency = cacheManager.getCache(nameCache).get(keyCache, CurrencyEntity.class);
		
		if(currency != null) {
			setInformation("Cache", source, target, currency, value);
			return ResponseEntity.ok(currency);
		}
		
		currency = new CurrencyEntity();
		try {
			//divide pq precisa ver quanto o "target cabe no source"
			currency.setConversionRate(getConversionRateFromApi(source.toUpperCase())/getConversionRateFromApi(target.toUpperCase()));
			dataSource = "API BCB";
			//https://stackoverflow.com/a/54843150/15773090 //https://regexr.com/8es9b
			currency.setConversionDate(conversionDate.replaceFirst("(\\d{2})-(\\d{2})", "$2-$1"));
		} catch (Exception e) {
			currency = repository.findBySourceAndTarget(source, target).orElseThrow(() -> new Exception("Currency not supported!"));
			dataSource = "Local Database";
		}
		
		setInformation(dataSource, source, target, currency, value);
		cacheManager.getCache(nameCache).put(keyCache, currency);

		return ResponseEntity.ok(currency);
	}
	
	private double getConversionRateFromApi(String currency) throws Exception {
		
		//se for brl, não converte, mas envia 1 para pelo menos não precisar tratar nem condicionar na hora da divisão
		if(currency.equals("BRL"))
			return 1;
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM-dd-yyyy");
		conversionDate = dtf.format(LocalDate.now());
		CurrencyBCResponse response = currencyBcClient.getCurrency(currency, conversionDate);
		
		//caso não conseguir do dia, vai tentar pelo menos nos 3 dias anteriores antes de dar erro
		int daysAgo = 1;
		while(response.getValue().isEmpty() || response.getValue().size() <= 0) {
			conversionDate = dtf.format(LocalDate.now().minusDays(daysAgo));
			response = currencyBcClient.getCurrency(currency, conversionDate);
			
			if(daysAgo >= 3)
				throw new Exception("Unable to get conversion rate from BC.");
			
			daysAgo++;
		}
		
		//pega o último valor pois é o mais recente
		return response.getValue().get(response.getValue().size() - 1).getCotacaoVenda(); 
	}
	
	private void setInformation(String dataSource, String source, String target, CurrencyEntity currency, double value) {
		currency.setEnvironment("Currency-Service running on port: " + serverPort + " - DataSource: " + dataSource);
		currency.setSource(source);
		currency.setTarget(target); 
		currency.setConvertedValue(value * currency.getConversionRate());
	}
	
}
