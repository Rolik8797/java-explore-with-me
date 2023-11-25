package ru.practicum;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"ru.practicum"})
public class Ewm {

    public static void main(String[] args) {
        SpringApplication.run(Ewm.class, args);
    }
}