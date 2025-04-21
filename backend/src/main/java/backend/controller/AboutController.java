package backend.controller;

import backend.dto.GetAboutRespDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AboutController {
    @GetMapping("/api/about")
    public GetAboutRespDto hello() {
        GetAboutRespDto getAboutRespDto = new GetAboutRespDto();
        getAboutRespDto.setStr("Hello! 연동 테스트");
        return getAboutRespDto;
    }
}