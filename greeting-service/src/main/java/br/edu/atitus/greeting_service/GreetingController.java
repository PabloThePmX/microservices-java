package br.edu.atitus.greeting_service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/greeting-service")
public class GreetingController {
	
	@GetMapping
	public String getGreeting(){
		return "Hello Sir!";
	}
}
