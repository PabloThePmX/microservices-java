package br.edu.atitus.product_service.clients;

import org.springframework.stereotype.Component;

@Component
public class CurrencyClientFallback implements CurrencyClient {

	@Override
	public CurrencyResponse getCurrency(double value, String source, String target) {
		var fallback = new CurrencyResponse();
		fallback.setConvertedValue(-1);
		return fallback;
	}

}
