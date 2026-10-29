package top.yixz.toolkit.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.yixz.toolkit.common.Result;
import top.yixz.toolkit.dto.InspectRequest;
import top.yixz.toolkit.dto.InspectSummary;
import top.yixz.toolkit.service.InspectService;

/**
 * 案例 11：接口巡检
 *
 * @author mqxu
 * @date 2026-10-21
 */
@RestController
@RequestMapping("/toolkit/inspect")
@RequiredArgsConstructor
public class InspectController {

    private final InspectService inspectService;

    @PostMapping
    public Result<InspectSummary> inspect(@RequestBody InspectRequest request) {
        return Result.ok(inspectService.inspect(request.urls()));
    }
}
