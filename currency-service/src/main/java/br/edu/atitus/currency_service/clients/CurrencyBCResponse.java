package br.edu.atitus.currency_service.clients;

import java.util.List;

import br.edu.atitus.currency_service.models.Currency;

public class CurrencyBCResponse {
    private List<Currency> value;
    
    public List<Currency> getValue() {
         return value;
    }
    
    public void setValue(List<Currency> value) {
         this.value = value;
    }
}
