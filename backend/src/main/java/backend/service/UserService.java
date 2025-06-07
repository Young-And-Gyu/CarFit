package backend.service;

import backend.domain.User;
import backend.dto.LoginRequestDto;
import backend.dto.SignupRequestDto;
import backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public void registerUser(SignupRequestDto signupRequestDto) {
        //중복 아이디 검사
        String userId=signupRequestDto.getUserId();
        if(userRepository.findByUserId(userId).isPresent()){

            throw new IllegalArgumentException("동일한 아이디가 존재합니다.");
        }

        User user =signupRequestDto.toEntity();

        userRepository.save(user);
    }

    public boolean login(LoginRequestDto loginRequestDto) {
        User user=userRepository.findByUserId(loginRequestDto.getUserId())
                .orElseThrow(()->new IllegalArgumentException("해당 Id가 존재하지 않습니다."));
        if(!user.getPassword().equals(loginRequestDto.getPassword())){
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }
        return true;
    }
}
