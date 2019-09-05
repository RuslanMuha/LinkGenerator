package com.hitecher.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.hitecher.service.LinksGeneratorService;

@RestController
public class Controller {
	@Autowired
	LinksGeneratorService service;

	@GetMapping("/links")
	public String urlGenerator() {
		try {
			return service.sendSensorData();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return e.getMessage();
		}
	}
}
