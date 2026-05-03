package hanyang.RentalManagementSystem.taewoong.service;

import hanyang.RentalManagementSystem.common.dto.*;
import hanyang.RentalManagementSystem.common.entity.*;
import hanyang.RentalManagementSystem.common.exception.CustomException;
import hanyang.RentalManagementSystem.common.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ModelService {

    private final ModelRepository modelRepository;
    private final ModelVersionRepository modelVersionRepository;
    private final DeviceRepository deviceRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public CommonResponse<List<Map<String, Object>>> findAll(CommonSearchRequest request) {
        Page<hanyang.RentalManagementSystem.common.entity.Model> page = modelRepository.findAll(request.toPageable());
        List<Map<String, Object>> data = page.getContent().stream().map(this::toMap).collect(Collectors.toList());
        return CommonResponse.success(data, Pagination.of(page));
    }

    public CommonResponse<Map<String, Object>> findById(Long id) {
        return CommonResponse.success(toMap(getModel(id)));
    }

    @Transactional
    public CommonResponse<Map<String, Object>> create(Map<String, Object> body) {
        hanyang.RentalManagementSystem.common.entity.Model model = hanyang.RentalManagementSystem.common.entity.Model.builder()
                .modelName((String) body.get("modelName"))
                .manufacturer((String) body.get("manufacturer"))
                .description((String) body.get("description"))
                .build();
        modelRepository.save(model);
        return CommonResponse.created(toMap(model));
    }

    @Transactional
    public CommonResponse<Map<String, Object>> update(Long id, Map<String, Object> body) {
        hanyang.RentalManagementSystem.common.entity.Model model = getModel(id);
        if (body.containsKey("modelName")) model.setModelName((String) body.get("modelName"));
        if (body.containsKey("manufacturer")) model.setManufacturer((String) body.get("manufacturer"));
        if (body.containsKey("description")) model.setDescription((String) body.get("description"));
        return CommonResponse.success(toMap(model));
    }

    @Transactional
    public void delete(Long id) {
        hanyang.RentalManagementSystem.common.entity.Model model = getModel(id);
        // Cascade: 하위 버전도 soft delete
        model.getVersions().forEach(v -> v.setIsDeleted(true));
    }

    @Transactional
    public CommonResponse<Map<String, Object>> createVersion(Long modelId, Map<String, Object> body) {
        hanyang.RentalManagementSystem.common.entity.Model model = getModel(modelId);
        ModelVersion mv = ModelVersion.builder()
                .model(model)
                .version((String) body.get("version"))
                .spec((String) body.get("spec"))
                .releaseDate(body.get("releaseDate") != null ? LocalDate.parse((String) body.get("releaseDate")) : null)
                .isDeleted(false)
                .build();
        modelVersionRepository.save(mv);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", mv.getId());
        map.put("version", mv.getVersion());
        map.put("spec", mv.getSpec());
        return CommonResponse.created(map);
    }

    @Transactional
    public CommonResponse<Map<String, Object>> updateVersion(Long id, Map<String, Object> body) {
        ModelVersion mv = getModelVersion(id);
        if (body.containsKey("version")) mv.setVersion((String) body.get("version"));
        if (body.containsKey("spec")) mv.setSpec((String) body.get("spec"));
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", mv.getId());
        map.put("version", mv.getVersion());
        return CommonResponse.success(map);
    }

    @Transactional
    public void deleteVersion(Long id) {
        ModelVersion mv = getModelVersion(id);
        boolean hasDevices = deviceRepository.findAll().stream()
                .anyMatch(d -> d.getModelVersion().getId().equals(id) && !d.getIsDeleted());
        if (hasDevices) {
            throw new CustomException("MODEL_VERSION_HAS_DEVICES", "연결된 디바이스가 있어 삭제할 수 없습니다.");
        }
        mv.setIsDeleted(true);
    }

    @Transactional
    public CommonResponse<Map<String, Object>> uploadManual(Long id, MultipartFile file) {
        ModelVersion mv = getModelVersion(id);
        String originalName = file.getOriginalFilename();
        String savedName = UUID.randomUUID().toString() + "_" + originalName;
        try {
            Path dir = Paths.get(uploadDir);
            if (!Files.exists(dir)) Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dir.resolve(savedName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new CustomException("FILE_UPLOAD_FAILED", "파일 업로드 실패: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        mv.setManualFileName(originalName);
        mv.setManualPath(savedName);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("savedFileName", savedName);
        map.put("originalFileName", originalName);
        return CommonResponse.created(map);
    }

    public ResponseEntity<byte[]> downloadManual(Long id) {
        ModelVersion mv = getModelVersion(id);
        if (mv.getManualPath() == null) {
            throw new CustomException("FILE_NOT_FOUND", "매뉴얼 파일이 없습니다.", HttpStatus.NOT_FOUND);
        }
        try {
            Path path = Paths.get(uploadDir).resolve(mv.getManualPath());
            byte[] data = Files.readAllBytes(path);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(ContentDisposition.attachment().filename(mv.getManualFileName()).build());
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        } catch (IOException e) {
            throw new CustomException("FILE_DOWNLOAD_FAILED", "파일 다운로드 실패", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private hanyang.RentalManagementSystem.common.entity.Model getModel(Long id) {
        return modelRepository.findById(id)
                .orElseThrow(() -> new CustomException("MODEL_NOT_FOUND", "모델을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    private ModelVersion getModelVersion(Long id) {
        return modelVersionRepository.findById(id)
                .orElseThrow(() -> new CustomException("MODEL_VERSION_NOT_FOUND", "모델버전을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    private Map<String, Object> toMap(hanyang.RentalManagementSystem.common.entity.Model m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", m.getId());
        map.put("modelName", m.getModelName());
        map.put("manufacturer", m.getManufacturer());
        map.put("description", m.getDescription());
        map.put("versions", m.getVersions().stream().filter(v -> !v.getIsDeleted()).map(v -> {
            Map<String, Object> vm = new LinkedHashMap<>();
            vm.put("id", v.getId());
            vm.put("version", v.getVersion());
            vm.put("spec", v.getSpec());
            vm.put("releaseDate", v.getReleaseDate());
            vm.put("manualFileName", v.getManualFileName());
            return vm;
        }).collect(Collectors.toList()));
        return map;
    }
}
