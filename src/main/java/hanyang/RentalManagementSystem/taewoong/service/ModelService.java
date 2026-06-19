package hanyang.RentalManagementSystem.taewoong.service;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.dto.CommonSearchRequest;
import hanyang.RentalManagementSystem.common.dto.Pagination;
import hanyang.RentalManagementSystem.common.entity.Model;
import hanyang.RentalManagementSystem.common.entity.ModelVersion;
import hanyang.RentalManagementSystem.common.exception.CustomException;
import hanyang.RentalManagementSystem.common.repository.DeviceRepository;
import hanyang.RentalManagementSystem.common.repository.ModelRepository;
import hanyang.RentalManagementSystem.common.repository.ModelVersionRepository;
import hanyang.RentalManagementSystem.taewoong.dto.FileUploadResponse;
import hanyang.RentalManagementSystem.taewoong.dto.ModelResponse;
import hanyang.RentalManagementSystem.taewoong.dto.ModelUpsertRequest;
import hanyang.RentalManagementSystem.taewoong.dto.ModelVersionResponse;
import hanyang.RentalManagementSystem.taewoong.dto.ModelVersionUpsertRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ModelService {

    private final ModelRepository modelRepository;
    private final ModelVersionRepository modelVersionRepository;
    private final DeviceRepository deviceRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 매뉴얼 업로드 허용 확장자 화이트리스트 (실행 가능 파일 .exe/.jsp/.sh 등 차단 — 보안)
    private static final Set<String> ALLOWED_MANUAL_EXT = Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "hwp", "txt",
            "png", "jpg", "jpeg", "gif", "zip");

    public CommonResponse<List<ModelResponse>> findAll(CommonSearchRequest request) {
        Page<Model> page = modelRepository.findAll(request.toPageable());
        return CommonResponse.success(page.getContent().stream().map(ModelResponse::from).toList(), Pagination.of(page));
    }

    public CommonResponse<ModelResponse> findById(Long id) {
        return CommonResponse.success(ModelResponse.from(getModel(id)));
    }

    @Transactional
    public CommonResponse<ModelResponse> create(ModelUpsertRequest req) {
        if (req.getModelName() == null || req.getModelName().isBlank()) {
            throw new CustomException("INVALID_REQUEST", "모델명은 필수입니다.", HttpStatus.BAD_REQUEST);
        }
        Model model = Model.builder()
                .modelName(req.getModelName())
                .manufacturer(req.getManufacturer())
                .description(req.getDescription())
                .build();
        modelRepository.save(model);
        return CommonResponse.created(ModelResponse.from(model));
    }

    @Transactional
    public CommonResponse<ModelResponse> update(Long id, ModelUpsertRequest req) {
        Model model = getModel(id);
        if (req.getModelName() != null) model.setModelName(req.getModelName());
        if (req.getManufacturer() != null) model.setManufacturer(req.getManufacturer());
        if (req.getDescription() != null) model.setDescription(req.getDescription());
        return CommonResponse.success(ModelResponse.from(model));
    }

    @Transactional
    public void delete(Long id) {
        // Cascade: 하위 버전도 soft delete
        getModel(id).getVersions().forEach(v -> v.setIsDeleted(true));
    }

    @Transactional
    public CommonResponse<ModelVersionResponse> createVersion(Long modelId, ModelVersionUpsertRequest req) {
        Model model = getModel(modelId);
        ModelVersion mv = ModelVersion.builder()
                .model(model)
                .version(req.getVersion())
                .spec(req.getSpec())
                .releaseDate(req.getReleaseDate() != null ? LocalDate.parse(req.getReleaseDate()) : null)
                .isDeleted(false)
                .build();
        modelVersionRepository.save(mv);
        return CommonResponse.created(ModelVersionResponse.from(mv));
    }

    @Transactional
    public CommonResponse<ModelVersionResponse> updateVersion(Long id, ModelVersionUpsertRequest req) {
        ModelVersion mv = getModelVersion(id);
        if (req.getVersion() != null) mv.setVersion(req.getVersion());
        if (req.getSpec() != null) mv.setSpec(req.getSpec());
        if (req.getReleaseDate() != null) mv.setReleaseDate(LocalDate.parse(req.getReleaseDate()));
        return CommonResponse.success(ModelVersionResponse.from(mv));
    }

    @Transactional
    public void deleteVersion(Long id) {
        ModelVersion mv = getModelVersion(id);
        // 교수님 피드백 #3: 전체 device findAll 대신 count 쿼리로 연결 여부 확인
        if (deviceRepository.countByModelVersionIdAndIsDeletedFalse(id) > 0) {
            throw new CustomException("MODEL_VERSION_HAS_DEVICES", "연결된 디바이스가 있어 삭제할 수 없습니다.");
        }
        mv.setIsDeleted(true);
    }

    @Transactional
    public CommonResponse<FileUploadResponse> uploadManual(Long id, MultipartFile file) {
        ModelVersion mv = getModelVersion(id);
        // Path traversal 방어: 파일명에서 디렉터리 경로 요소(../, 절대경로 등) 제거 → basename만 사용
        String originalName = file.getOriginalFilename();
        String safeName = (originalName == null) ? null : originalName.replaceAll("^.*[\\\\/]", "");
        validateUploadExtension(safeName);
        String savedName = UUID.randomUUID() + "_" + safeName;
        try {
            Path dir = Paths.get(uploadDir);
            if (!Files.exists(dir)) Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dir.resolve(savedName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new CustomException("FILE_UPLOAD_FAILED", "파일 업로드 실패: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        mv.setManualFileName(safeName);
        mv.setManualPath(savedName);
        return CommonResponse.created(FileUploadResponse.builder()
                .savedFileName(savedName).originalFileName(safeName).build());
    }

    public ResponseEntity<byte[]> downloadManual(Long id) {
        ModelVersion mv = getModelVersion(id);
        if (mv.getManualPath() == null) {
            throw new CustomException("FILE_NOT_FOUND", "매뉴얼 파일이 없습니다.", HttpStatus.NOT_FOUND);
        }
        try {
            // Path Traversal 방어: 해석된 경로가 업로드 디렉터리 밖이면 거부
            Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path path = base.resolve(mv.getManualPath()).normalize();
            if (!path.startsWith(base)) {
                throw new CustomException("FILE_NOT_FOUND", "매뉴얼 파일이 없습니다.", HttpStatus.NOT_FOUND);
            }
            byte[] data = Files.readAllBytes(path);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(ContentDisposition.attachment().filename(mv.getManualFileName()).build());
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        } catch (IOException e) {
            throw new CustomException("FILE_DOWNLOAD_FAILED", "파일 다운로드 실패", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Model getModel(Long id) {
        return modelRepository.findById(id)
                .orElseThrow(() -> new CustomException("MODEL_NOT_FOUND", "모델을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    private ModelVersion getModelVersion(Long id) {
        // soft-delete된 버전이 update/업로드로 부활하지 않도록 isDeleted 필터
        return modelVersionRepository.findById(id)
                .filter(mv -> !Boolean.TRUE.equals(mv.getIsDeleted()))
                .orElseThrow(() -> new CustomException("MODEL_VERSION_NOT_FOUND", "모델버전을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    // 업로드 파일 확장자 화이트리스트 검증 (실행 가능 파일 차단)
    private void validateUploadExtension(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            throw new CustomException("INVALID_FILE", "파일 이름이 올바르지 않습니다.", HttpStatus.BAD_REQUEST);
        }
        int dot = originalName.lastIndexOf('.');
        String ext = dot >= 0 ? originalName.substring(dot + 1).toLowerCase() : "";
        if (!ALLOWED_MANUAL_EXT.contains(ext)) {
            throw new CustomException("UNSUPPORTED_FILE_TYPE",
                    "허용되지 않은 파일 형식입니다. (허용: " + String.join(", ", ALLOWED_MANUAL_EXT) + ")",
                    HttpStatus.BAD_REQUEST);
        }
    }
}
