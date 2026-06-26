package uz.sdg.sos.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import uz.sdg.sos.dto.dmed.DmedPatientData;
import uz.sdg.sos.service.DmedMockService;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class DmedMockServiceImpl implements DmedMockService {

    private final Map<String, DmedPatientData> cache = new HashMap<>();

    public DmedMockServiceImpl(ResourceLoader resourceLoader) {
        try {
            Resource resource = resourceLoader.getResource("classpath:mock/dmed_patients.json");
            InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            JsonObject root = gson.fromJson(reader, JsonObject.class);
            JsonArray patients = root.getAsJsonArray("patients");
            for (JsonElement element : patients) {
                DmedPatientData patient = gson.fromJson(element, DmedPatientData.class);
                cache.put(patient.getJshshir(), patient);
            }
            log.info("DMED mock loaded: {} patients", cache.size());
        } catch (Exception e) {
            log.error("Failed to load DMED mock data: {}", e.getMessage());
        }
    }

    @Override
    public Optional<DmedPatientData> findByJshshir(String jshshir) {
        return Optional.ofNullable(cache.get(jshshir));
    }
}
