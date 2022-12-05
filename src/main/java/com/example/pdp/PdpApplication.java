package com.example.pdp;

import com.example.pdp.property.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.CrossOrigin;

@SpringBootApplication
@EnableConfigurationProperties({ FileStorageProperties.class })
public class PdpApplication {

    public static void main(String[] args) {
        SpringApplication.run(PdpApplication.class, args);
    }
}
