package backend.dto;

import backend.domain.ResCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetAboutRespDto {
    private String str;
    private int code = ResCode.SUCCESS.value();
    private String message;
}