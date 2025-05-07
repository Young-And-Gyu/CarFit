package backend.dto;

public class InsuranceResponse {
    private int insuranceFee;

    public InsuranceResponse(int insuranceFee){
        this.insuranceFee=insuranceFee;
    }

    public int getInsuranceFee() {
        return insuranceFee;
    }
}
