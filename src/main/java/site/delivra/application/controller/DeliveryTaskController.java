package site.delivra.application.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.delivra.application.model.constants.ApiLogMassage;
import site.delivra.application.model.dto.DeliveryTaskDTO;
import site.delivra.application.model.response.DelivraResponse;
import site.delivra.application.service.DeliveryTaskService;
import site.delivra.application.utils.ApiUtils;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/tasks")
public class DeliveryTaskController {

    private final DeliveryTaskService deliveryTaskService;

    @GetMapping("/{id}")
    public ResponseEntity<DelivraResponse<DeliveryTaskDTO>> getById(@PathVariable(name = "id") Integer id) {
        log.trace(ApiLogMassage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        DelivraResponse<DeliveryTaskDTO> byId = deliveryTaskService.getById(id);

        return ResponseEntity.ok(byId);
    }
}
