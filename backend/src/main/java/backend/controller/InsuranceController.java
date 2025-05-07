package backend.controller;

import backend.dto.InsuranceRequest;
import backend.dto.InsuranceResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class InsuranceController {

    @PostMapping("/calculate-insurance")
    public ResponseEntity<InsuranceResponse>calculateInsurance(@RequestBody InsuranceRequest request){
        int finalPrice = calculatePrice(request);
        InsuranceResponse response = new InsuranceResponse(finalPrice);
        return ResponseEntity.ok(response);
    }

    private int calculatePrice(InsuranceRequest req) {
        int basePrice = 500000;
        double discountRate = 0;
        int extraCharge = 0;

        if (req.getAge() < 25) basePrice += 200000;
        else if (req.getAge() > 60) basePrice += 150_000;

        if ("SUV".equals(req.getCarType())) basePrice += 100000;
        else if ("Sports".equals(req.getCarType())) basePrice += 300000;

        if (req.getAccidentCount() > 0) {
            extraCharge += (int) (basePrice * 0.05 * req.getAccidentCount());
        }

        if (req.getOptions().isBlackBox()) discountRate += 0.05;
        if (req.getOptions().isMileage()) discountRate += 0.1;
        if (req.getOptions().isDriverScopeLimited()) discountRate += 0.07;
        if (req.getOptions().isHasChild()) discountRate += 0.08;

        if (req.getAge() >= 70) {
            extraCharge += basePrice * 0.1;
        }

        double discountAmount = basePrice * discountRate;
        double totalPrice = basePrice - discountAmount + extraCharge;

        return (int)Math.round(totalPrice);
    }
}
