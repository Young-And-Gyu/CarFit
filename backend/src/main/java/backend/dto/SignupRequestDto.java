package backend.dto;

import backend.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor //기본생성자
@AllArgsConstructor //모든 필드에 대한 생성자
@Builder
public class SignupRequestDto {
    private String userId;
    private String password;
    private String name;
    private String email;
    private String address;
    private String birthDate;
    private boolean termsAgreed;
    private boolean privacyAgreed;
    private boolean marketingAgreed;

    public User toEntity(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate parsedBirthDate = LocalDate.parse(this.birthDate, formatter);

        return new User(
                userId,
                password,
                name,
                email,
                address,
                parsedBirthDate,
                termsAgreed,
                privacyAgreed,
                marketingAgreed
        );
    }
}
