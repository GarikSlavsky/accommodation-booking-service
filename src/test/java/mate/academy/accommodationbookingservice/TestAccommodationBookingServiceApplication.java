package mate.academy.accommodationbookingservice;

import org.springframework.boot.SpringApplication;

public class TestAccommodationBookingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(AccommodationBookingServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
