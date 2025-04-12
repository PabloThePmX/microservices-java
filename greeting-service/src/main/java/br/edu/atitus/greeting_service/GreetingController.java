package br.edu.atitus.greeting_service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.atitus.greeting_service.configs.GreetingConfig;

@RestController
@RequestMapping("/greeting-service")
public class GreetingController {
	
	/*
	@Value("${greeting-service.greeting}")
	private String greeting;
	
	@Value("${greeting-service.default-name}")
	private String defaultName;
	*/
	
	private final GreetingConfig greetingConfig;
	
	public GreetingController(GreetingConfig greetingConfig){
		this.greetingConfig = greetingConfig;
	}
	
	@GetMapping({"","/","/{namePath}"})
	public ResponseEntity<String> getGreeting(@PathVariable(required = false) String namePath, @RequestParam(required = false) String name){
		name = namePath != null ? namePath : name;
		
		return name != null 
				? ResponseEntity.ok(String.format("Hello Sir %s!", name)) 
				: ResponseEntity.ok(String.format("%s %s!", greetingConfig.getGreeting(), greetingConfig.getDefaultName()));
	} 
}
