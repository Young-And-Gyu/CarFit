package backend.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class InsuranceRequest {
    private int age;
    private String carType;
    private int accidentCount;
    private Options options;

    @Getter
    @Setter
    public static class Options {
        private boolean blackBox;
        private boolean mileage;
        private boolean driverScopeLimited;
        private boolean hasChild;
    }
}