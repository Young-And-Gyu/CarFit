package backend.service;

import backend.domain.User;
import backend.dto.LoginRequestDto;
import backend.dto.SignupRequestDto;
import backend.repository.UserRepository;
import backend.util.AESUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void registerUser(SignupRequestDto signupRequestDto) throws Exception {
        //중복 아이디 검사
        String userId=signupRequestDto.getUserId();
        if(userRepository.findByUserId(userId).isPresent()){

            throw new IllegalArgumentException("동일한 아이디가 존재합니다.");
        }
        String encodedPassword=passwordEncoder.encode(signupRequestDto.getPassword());
        String encodedAddress= AESUtil.encrypt(signupRequestDto.getAddress());
        String encodedEmail= AESUtil.encrypt(signupRequestDto.getEmail());
        User user =signupRequestDto.toEntity(encodedPassword,encodedAddress, encodedEmail);

        userRepository.save(user);
    }

    public boolean login(LoginRequestDto loginRequestDto) {
        User user=userRepository.findByUserId(loginRequestDto.getUserId())
                .orElseThrow(()->new IllegalArgumentException("해당 Id가 존재하지 않습니다."));
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }
        return true;
    }
}
