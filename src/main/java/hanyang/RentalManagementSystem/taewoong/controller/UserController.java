package hanyang.RentalManagementSystem.taewoong.controller;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.dto.CommonSearchRequest;
import hanyang.RentalManagementSystem.taewoong.dto.UserResponse;
import hanyang.RentalManagementSystem.taewoong.dto.UserUpsertRequest;
import hanyang.RentalManagementSystem.taewoong.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<CommonResponse<List<UserResponse>>> list(CommonSearchRequest request) {
        return ResponseEntity.ok(userService.findAll(request));
    }

    @PostMapping
    public ResponseEntity<CommonResponse<UserResponse>> create(@RequestBody UserUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CommonResponse<UserResponse>> update(@PathVariable Long id, @RequestBody UserUpsertRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }
}
