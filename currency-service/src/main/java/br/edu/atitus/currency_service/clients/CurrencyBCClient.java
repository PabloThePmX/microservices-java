package br.edu.atitus.currency_service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "bancoCentralClient", url = "https://olinda.bcb.gov.br/olinda/servico/PTAX/versao/v1/odata")
public interface CurrencyBCClient {
	
     @GetMapping("/CotacaoMoedaDia(moeda=@moeda,dataCotacao=@dataCotacao)?@moeda='{moeda}'&@dataCotacao='{dataCotacao}'&$format=json")
     CurrencyBCResponse getCotacaoMoedaDia(@PathVariable String moeda, @PathVariable String dataCotacao);
}

///CotacaoMoedaDia(moeda=@moeda,dataCotacao=@dataCotacao)?@moeda=%27EUR%27&@dataCotacao=%2705-06-2025%27&$format=json
