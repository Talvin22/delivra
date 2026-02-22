package site.delivra.application.model.request.task;

import lombok.Data;
import site.delivra.application.model.enums.DeliveryTaskSortField;

@Data
public class SearchDeliveryTaskRequest {
    private String address;
    private String status;
    private String createdBy;
    private Integer userId;

    private Boolean deleted;
    private String keyword;
    private DeliveryTaskSortField sortField;


}
