package com.sesac.solbid.controller;

import com.sesac.solbid.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductRegisterController {

    private final ProductService productService;

//    @PostMapping("/api/productRegister")
//    public ResponseEntity<Map<String, Object>> productRegister(@RequestPart List<MultipartFile> files, @RequestPart ProductRegisterDto dto) {
//        try {
//            productService.registerProduct(dto, files);
//            System.out.println("success");
//            return ResponseEntity.ok(Map.of("success", true));
//        } catch (IOException e) {
//            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
//        }
//    }
}
