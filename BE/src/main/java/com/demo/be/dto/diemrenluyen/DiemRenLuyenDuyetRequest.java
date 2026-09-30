package com.demo.be.dto.diemrenluyen;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DiemRenLuyenDuyetRequest(
        @NotNull(message = "Trạng thái phê duyệt (true: Duyệt, false: Từ chối) không được để trống")
        Boolean chapNhan,

        @Min(value = 0, message = "Tiêu chí 1 tối thiểu là 0")
        @Max(value = 20, message = "Tiêu chí 1 tối đa là 20")
        Integer tieuChi1Admin,

        @Min(value = 0, message = "Tiêu chí 2 tối thiểu là 0")
        @Max(value = 25, message = "Tiêu chí 2 tối đa là 25")
        Integer tieuChi2Admin,

        @Min(value = 0, message = "Tiêu chí 3 tối thiểu là 0")
        @Max(value = 20, message = "Tiêu chí 3 tối đa là 20")
        Integer tieuChi3Admin,

        @Min(value = 0, message = "Tiêu chí 4 tối thiểu là 0")
        @Max(value = 25, message = "Tiêu chí 4 tối đa là 25")
        Integer tieuChi4Admin,

        @Min(value = 0, message = "Tiêu chí 5 tối thiểu là 0")
        @Max(value = 10, message = "Tiêu chí 5 tối đa là 10")
        Integer tieuChi5Admin,

        String nhanXetAdmin
) {}
