package uz.sdg.sos.dto.makeObjects;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Data
@Getter
@Setter
@NoArgsConstructor
public class JsonObjectMake  implements Serializable {
    private String key;
    private String value;
}