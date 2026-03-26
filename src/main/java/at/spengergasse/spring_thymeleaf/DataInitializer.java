package at.spengergasse.spring_thymeleaf;

import at.spengergasse.spring_thymeleaf.entities.Device;
import at.spengergasse.spring_thymeleaf.entities.DeviceRepository;
import at.spengergasse.spring_thymeleaf.entities.DeviceType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner loadDevices(DeviceRepository deviceRepository) {
        return args -> {
            if (deviceRepository.count() == 0) {
                deviceRepository.saveAll(List.of(
                        new Device("MR-1", DeviceType.MR, "R101"),
                        new Device("CT-1", DeviceType.CT, "R102"),
                        new Device("XR-1", DeviceType.ROENTGEN, "R103"),
                        new Device("US-1", DeviceType.ULTRASCHALL, "R104")
                ));
            }
        };
    }
}
